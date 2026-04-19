package com.example.spacecrewgame.engine;

import android.content.Context;

import com.example.spacecrewgame.enums.CrewState;
import com.example.spacecrewgame.enums.TimePhase;
import com.example.spacecrewgame.enums.ThreatType;
import com.example.spacecrewgame.model.ActionResult;
import com.example.spacecrewgame.model.CombatAction;
import com.example.spacecrewgame.model.CombatContext;
import com.example.spacecrewgame.model.CrewMember;
import com.example.spacecrewgame.model.DiplomatCrewMember;
import com.example.spacecrewgame.model.EngineerCrewMember;
import com.example.spacecrewgame.model.GameState;
import com.example.spacecrewgame.model.MarineCrewMember;
import com.example.spacecrewgame.model.PilotCrewMember;
import com.example.spacecrewgame.model.ScientistCrewMember;
import com.example.spacecrewgame.model.Threat;
import com.example.spacecrewgame.util.GameConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The central orchestrator for game logic and state transitions.
 * Implements the Singleton pattern to provide a consistent state across activities.
 */
public class GameEngine {

    private static GameEngine instance;
    private final GameState state;
    private final Random rng;

    private GameEngine() {
        this.state = new GameState();
        this.rng = new Random();
        setupStarterCrew();
    }

    /**
     * Returns the singleton instance of the GameEngine.
     *
     * @return The active GameEngine instance.
     */
    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    /**
     * Resets the game engine to a fresh state, effectively starting a new game.
     */
    public static synchronized void reset() {
        instance = new GameEngine();
    }

    /**
     * Attempts to recruit a new crew member of a specific class.
     *
     * @param name      The name of the new crew member.
     * @param classType The class type string (e.g., "PILOT", "MARINE").
     * @return A status message describing the result of the recruitment.
     */
    public String recruitCrew(String name, String classType) {
        if (!state.hasActionsLeft()) return "No ship actions remaining this turn.";
        if (!state.canRecruit()) return "Crew quarters are full.";
        if (name == null || name.trim().isEmpty()) return "Please enter a name.";

        CrewMember member;
        switch (classType.toUpperCase().trim()) {
            case "PILOT":     member = new PilotCrewMember(name);     break;
            case "ENGINEER":  member = new EngineerCrewMember(name);  break;
            case "MARINE":    member = new MarineCrewMember(name);    break;
            case "SCIENTIST": member = new ScientistCrewMember(name); break;
            case "DIPLOMAT":  member = new DiplomatCrewMember(name);  break;
            default: return "Unknown class: " + classType;
        }
        state.addCrew(member);
        state.recordAction();
        return name + " joined the crew.";
    }

    /**
     * Removes a crew member from the roster.
     *
     * @param crewId The unique ID of the crew member to dismiss.
     * @return A status message describing the result.
     */
    public String disbandCrew(String crewId) {
        CrewMember member = findById(crewId);
        if (member == null) return "Crew member not found.";
        if (state.getCrew().size() <= 1) return "Cannot disband the last crew member.";
        state.removeCrew(member);
        state.recordCrewLost();
        return member.getName() + " dismissed.";
    }

    /**
     * Assigns a crew member to training to gain XP at the end of the turn.
     *
     * @param crewId The unique ID of the crew member.
     * @return A status message describing the result.
     */
    public String assignTraining(String crewId) {
        if (!state.hasActionsLeft()) return "No ship actions remaining this turn.";
        CrewMember member = findById(crewId);
        if (member == null) return "Crew member not found.";
        if (!member.isAvailableForCombat()) return member.getName() + " is busy or incapacitated.";
        member.setState(CrewState.IN_TRAINING);
        state.recordAction();
        return member.getName() + " assigned to training.";
    }

    /**
     * Moves a crew member to the medbay for accelerated healing.
     *
     * @param crewId The unique ID of the crew member.
     * @return A status message describing the result.
     */
    public String assignMedbay(String crewId) {
        if (!state.hasActionsLeft()) return "No ship actions remaining this turn.";
        CrewMember member = findById(crewId);
        if (member == null) return "Crew member not found.";
        if (member.getState() == CrewState.INCAPACITATED)
            return member.getName() + " is incapacitated — they will be moved automatically.";
        if (!state.medbayHasSpace()) return "Medbay is full.";
        member.setState(CrewState.IN_MEDBAY);
        state.recordAction();
        return member.getName() + " moved to medbay.";
    }

    /**
     * Ends the current ship turn and initiates a patrol, which may trigger an encounter.
     *
     * @return A summary log of the patrol results.
     */
    public String endShipTurn() {
        if (state.getPhase() != TimePhase.SHIP_TIME)
            return "Cannot end ship turn while an encounter is active.";

        String patrolLog = patrol();
        if (state.getPhase() == TimePhase.SHIP_TIME) {
            startNextTurn();
            patrolLog = "Patrol uneventful. Ship turn " + state.getShipTurn() + " begins.";
        }
        return patrolLog;
    }

    /**
     * Advances the game state to the next turn, processing recovery and cooldowns.
     */
    public void startNextTurn() {
        state.incrementShipTurn();
        processTrainingCompletion();
        processHealing();
        for (CrewMember c : state.getCrew()) {
            c.tickCooldowns();
            c.resetCooldowns();
        }
    }

    /**
     * Processes a single combat action performed by a crew member.
     *
     * @param context  The Android context for resource access.
     * @param actorId  The ID of the crew member performing the action.
     * @param actionId The unique ID of the action to execute.
     * @param allyId   The ID of the partner crew member for targeted effects.
     * @return An ActionResult describing the outcome of the action.
     */
    public ActionResult submitCombatAction(Context context, String actorId, String actionId, String allyId) {
        if (state.getPhase() != TimePhase.COMBAT_TIME)
            return new ActionResult(0, "No active encounter.");

        CrewMember actor  = findById(actorId);
        CrewMember ally   = findById(allyId);
        Threat     threat = state.getCurrentThreat();

        if (actor == null || ally == null || threat == null)
            return new ActionResult(0, "Invalid combat state.");

        if (actor.getState() == CrewState.INCAPACITATED) {
            return new ActionResult(0, actor.getName() + " is incapacitated and cannot act!");
        }

        int currentCooldown = actor.getCooldownRemaining(actionId);
        if (currentCooldown > 0) {
            return new ActionResult(0, actor.getName() + " is still preparing that action!");
        }

        CombatContext ctx = new CombatContext(context, threat, actor, ally, 0);
        int bonus = actor.getNextActionBonus();
        actor.clearNextActionBonus();

        ActionResult result = actor.performAction(actionId, ctx);

        for (CombatAction a : actor.getActions()) {
            if (a.getId().equals(actionId)) {
                if (a.getCooldown() > 0) {
                    actor.startCooldown(actionId, a.getCooldown() + 1);
                }
                break;
            }
        }

        int finalDamage = result.getDamageToThreat();
        if (finalDamage > 0) finalDamage += bonus;
        threat.takeDamage(Math.max(0, finalDamage));

        int healToActor = result.getHealToActor();
        if (healToActor > 0) actor.heal(healToActor);
        else if (healToActor < 0) {
            actor.takeDamage(-healToActor);
            state.recordDamageTaken(-healToActor);
        }

        int healToAlly = result.getHealToAlly();
        if (healToAlly > 0) ally.heal(healToAlly);
        else if (healToAlly < 0) {
            ally.takeDamage(-healToAlly);
            state.recordDamageTaken(-healToAlly);
        }

        if (result.getActorDamageReduction() > 0) actor.setRoundDamageReduction(result.getActorDamageReduction());
        if (result.getAllyDamageReduction() > 0) ally.setRoundDamageReduction(result.getAllyDamageReduction());

        if (threat.isNeutralized()) {
            state.recordThreatNeutralized();
            awardCombatXp(actor, ally);
            endEncounter();
        }

        if (bonus > 0 && finalDamage > 0) {
            return new ActionResult(finalDamage, 
                    result.getHealToActor(), result.getHealToAlly(),
                    result.getActorDamageReduction(), result.getAllyDamageReduction(),
                    result.getLogText() + " (Tactical Bonus +" + bonus + " dmg!)");
        }
        return result;
    }

    /**
     * Resolves the threat's turn in a combat round, distributing damage to the crew.
     *
     * @param crewAId The ID of the first participating crew member.
     * @param crewBId The ID of the second participating crew member.
     * @return An ActionResult containing the threat's attack log.
     */
    public ActionResult resolveThreatTurn(String crewAId, String crewBId) {
        Threat     threat = state.getCurrentThreat();
        CrewMember crewA  = findById(crewAId);
        CrewMember crewB  = findById(crewBId);

        if (threat == null || threat.isNeutralized()) return new ActionResult(0, "Threat neutralized.");
        if (crewA == null || crewB == null) return new ActionResult(0, "Crew missing.");

        crewA.tickCooldowns();
        crewB.tickCooldowns();

        if (threat.isStunned()) {
            threat.setStunned(false);
            return new ActionResult(0, threat.getName() + " is staggered and skips its turn!");
        }

        int totalAttack = threat.getAttack();
        int splitA = totalAttack / 2;
        int splitB = totalAttack - splitA;

        int dmgToA = 0, dmgToB = 0;
        if (crewA.getState() != CrewState.INCAPACITATED) {
            dmgToA = Math.max(0, splitA - crewA.getRoundDamageReduction());
            crewA.takeDamage(dmgToA);
            state.recordDamageTaken(dmgToA);
        }
        if (crewB != crewA && crewB.getState() != CrewState.INCAPACITATED) {
            dmgToB = Math.max(0, splitB - crewB.getRoundDamageReduction());
            crewB.takeDamage(dmgToB);
            state.recordDamageTaken(dmgToB);
        } else if (crewB == crewA && crewA.getState() != CrewState.INCAPACITATED) {
            dmgToA = Math.max(0, totalAttack - crewA.getRoundDamageReduction());
            crewA.takeDamage(dmgToA);
            state.recordDamageTaken(dmgToA);
        }

        crewA.resetRoundDamageReduction();
        crewB.resetRoundDamageReduction();

        String log = threat.getName() + " attacks! ";
        if (crewA.getState() != CrewState.INCAPACITATED) log += crewA.getName() + " takes " + dmgToA + " dmg. ";
        if (crewB != crewA && crewB.getState() != CrewState.INCAPACITATED) log += crewB.getName() + " takes " + dmgToB + " dmg.";

        if (state.isGameOver()) endEncounter();
        return new ActionResult(0, log);
    }

    /**
     * Initializes the roster with a basic starter crew.
     */
    private void setupStarterCrew() {
        state.addCrew(new PilotCrewMember("Alex"));
        state.addCrew(new EngineerCrewMember("Sam"));
    }

    /**
     * Calculates the probability of a random encounter and generates a threat if successful.
     *
     * @return A status message describing the patrol outcome.
     */
    private String patrol() {
        if (rng.nextDouble() < GameConfig.ENCOUNTER_CHANCE) {
            Threat threat = generateThreat();
            state.setCurrentThreat(threat);
            state.setPhase(TimePhase.COMBAT_TIME);
            return "⚠ Threat encountered: " + threat.getName()
                    + " (HP " + threat.getMaxHp() + ", ATK " + threat.getAttack() + ")";
        }
        return "Patrol uneventful.";
    }

    /**
     * Randomly generates a new threat based on current mission progress.
     *
     * @return A newly constructed Threat object.
     */
    private Threat generateThreat() {
        ThreatType[] types  = ThreatType.values();
        ThreatType   type   = types[rng.nextInt(types.length)];
        int          turn   = state.getShipTurn();
        int          hp     = GameConfig.THREAT_BASE_HP + turn * GameConfig.THREAT_HP_PER_TURN; 
        int          attack = GameConfig.THREAT_BASE_ATK + turn * GameConfig.THREAT_ATK_PER_TURN;

        String name;
        switch (type) {
            case BOARDING:          name = "Boarding Party";     break;
            case SHIP_HAZARD:       name = "Ship Hazard";        break;
            case DEBRIS_FIELD:      name = "Debris Field";       break;
            case PIRATE_INTERCEPT:  name = "Pirate Raider";      break;
            case FACTION_ENCOUNTER: name = "Faction Standoff";   break;
            default:                name = "Unknown Threat";
        }
        
        if (rng.nextDouble() < GameConfig.ELITE_CHANCE) {
            hp *= GameConfig.ELITE_STAT_BOOST;
            attack *= GameConfig.ELITE_STAT_BOOST;
            name = "Elite " + name;
        }
        return new Threat(UUID.randomUUID().toString(), name, type, hp, attack);
    }

    /**
     * Clears the current encounter state.
     */
    private void endEncounter() {
        state.setCurrentThreat(null);
        state.setPhase(TimePhase.SHIP_TIME);
    }

    /**
     * Finalizes training for all crew members currently in the training state.
     */
    private void processTrainingCompletion() {
        for (CrewMember c : state.getCrew()) {
            if (c.getState() == CrewState.IN_TRAINING) {
                c.gainXp(50);
                if (c.canLevelUp()) c.levelUp();
                c.setState(CrewState.IN_QUARTERS);
            }
        }
    }

    /**
     * Handles HP recovery and medical triage for all crew members.
     */
    private void processHealing() {
        for (CrewMember c : state.getCrew()) {
            if (c.getState() == CrewState.IN_MEDBAY) {
                c.heal(GameConfig.MEDBAY_HEAL_AMOUNT);
                if (c.getHp() >= c.getMaxHp()) c.setState(CrewState.IN_QUARTERS);
            } else if (c.getState() == CrewState.IN_QUARTERS && state.getPhase() == TimePhase.SHIP_TIME && c.getHp() > 0) {
                c.heal(GameConfig.QUARTERS_HEAL_AMOUNT);
            }
        }

        List<CrewMember> incapacitated = state.getCrew().stream()
                .filter(c -> c.getState() == CrewState.INCAPACITATED)
                .collect(Collectors.toList());

        List<CrewMember> lostToday = new ArrayList<>();
        for (CrewMember target : incapacitated) {
            boolean viable = target.applyPermanentInjury();
            if (!viable) {
                lostToday.add(target);
                continue;
            }

            if (state.medbayHasSpace()) {
                target.setState(CrewState.IN_MEDBAY);
            } else {
                state.getCrew().stream()
                        .filter(c -> c.getState() == CrewState.IN_MEDBAY)
                        .max(Comparator.comparingInt(CrewMember::getHp))
                        .ifPresentOrElse(healthiest -> {
                            healthiest.setState(CrewState.IN_QUARTERS);
                            target.setState(CrewState.IN_MEDBAY);
                        }, () -> {
                            if (rng.nextBoolean()) lostToday.add(target);
                        });
            }
        }

        for (CrewMember lost : lostToday) {
            state.removeCrew(lost);
            state.recordCrewLost();
        }
    }

    /**
     * Awards XP to crew members who participated in combat.
     */
    private void awardCombatXp(CrewMember a, CrewMember b) {
        a.gainXp(25);
        if (a != b) b.gainXp(25);
        if (a.canLevelUp()) a.levelUp();
        if (b.canLevelUp()) b.levelUp();
    }

    /**
     * Utility to find a crew member by their unique ID.
     */
    private CrewMember findById(String id) {
        for (CrewMember c : state.getCrew()) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    /**
     * Provides access to the current game state.
     *
     * @return The GameState object.
     */
    public GameState getState() { return state; }
}
