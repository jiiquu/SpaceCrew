package com.example.spacecrewgame.model;

import com.example.spacecrewgame.enums.CrewState;
import com.example.spacecrewgame.enums.ThreatType;
import com.example.spacecrewgame.util.GameConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base abstract class representing a member of the spaceship crew.
 * Manages stats, leveling, health, and combat action cooldowns.
 */
public abstract class CrewMember {

    private final String    id;
    private final String    name;
    private int             hp;
    private int             maxHp;
    private int             level;
    private int             xp;
    private CrewState       state;
    private int             roundDamageReduction;
    private int             nextActionBonus;
    
    /** Tracks remaining cooldown rounds for each combat action by its ID. */
    private final Map<String, Integer> actionCooldowns = new HashMap<>();

    /**
     * Constructs a new CrewMember.
     * @param name The display name of the member.
     * @param maxHp The initial maximum health capacity.
     */
    protected CrewMember(String name, int maxHp) {
        this.id                   = UUID.randomUUID().toString();
        this.name                 = name;
        this.hp                   = maxHp;
        this.maxHp                = maxHp;
        this.level                = 1;
        this.xp                   = 0;
        this.state                = CrewState.IN_QUARTERS;
        this.roundDamageReduction = 0;
        this.nextActionBonus      = 0;
    }

    /** @return The list of unique combat actions available to this class. */
    public abstract List<CombatAction> getActions();

    /**
     * Executes a specific action.
     * @param actionId The ID of the action to perform.
     * @param ctx Current combat context.
     * @return The result of the action execution.
     */
    public abstract ActionResult performAction(String actionId, CombatContext ctx);

    /**
     * Calculates effectiveness against a specific threat type for UI guidance.
     * @param threat The threat to evaluate.
     * @return A rating score from 0-10.
     */
    public int getEffectivenessRating(Threat threat) {
        if (threat == null) return 5;
        
        ThreatType type = threat.getType();
        String className = this.getClass().getSimpleName();
        
        switch (type) {
            case BOARDING:
                if (className.contains("Marine")) return 10;
                if (className.contains("Engineer")) return 8;
                if (className.contains("Pilot")) return 7;
                return 3;
            case SHIP_HAZARD:
                if (className.contains("Engineer")) return 10;
                if (className.contains("Scientist")) return 8;
                if (className.contains("Marine")) return 6;
                return 2;
            case DEBRIS_FIELD:
                if (className.contains("Pilot")) return 10;
                if (className.contains("Engineer")) return 8;
                if (className.contains("Scientist")) return 7;
                return 4;
            case PIRATE_INTERCEPT:
                if (className.contains("Marine")) return 10;
                if (className.contains("Pilot")) return 8;
                if (className.contains("Diplomat")) return 7;
                return 5;
            case FACTION_ENCOUNTER:
                if (className.contains("Diplomat")) return 10;
                if (className.contains("Pilot")) return 9;
                if (className.contains("Engineer")) return 7;
                return 5;
            default:
                return 5;
        }
    }

    public void startCooldown(String actionId, int rounds) {
        if (rounds > 0) actionCooldowns.put(actionId, rounds);
    }

    public int getCooldownRemaining(String actionId) {
        return actionCooldowns.getOrDefault(actionId, 0);
    }

    /** Decrements all active cooldowns by one round. */
    public void tickCooldowns() {
        for (String id : new java.util.HashSet<>(actionCooldowns.keySet())) {
            int current = actionCooldowns.get(id);
            if (current > 0) actionCooldowns.put(id, current - 1);
        }
    }

    public void resetCooldowns() {
        actionCooldowns.clear();
    }

    /** @return true if the member is in Quarters and has positive HP. */
    public boolean isAvailableForCombat() {
        return state == CrewState.IN_QUARTERS && hp > 0;
    }

    /** Reduces HP and updates state to INCAPACITATED if HP reaches 0. */
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
        if (hp == 0) state = CrewState.INCAPACITATED;
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    /** 
     * Applies a permanent penalty to Max HP after being downed. 
     * @return true if the member still has enough vitality to serve.
     */
    public boolean applyPermanentInjury() {
        this.maxHp -= GameConfig.PERMANENT_INJURY_HP_LOSS;
        if (this.hp > this.maxHp) this.hp = this.maxHp;
        return this.maxHp > GameConfig.MIN_VIABLE_MAX_HP;
    }

    public void gainXp(int amount) { xp += amount; }
    public boolean canLevelUp() { return xp >= GameConfig.XP_PER_LEVEL; }

    /** Increases level, boosts Max HP, and restores full health. */
    public void levelUp() {
        if (canLevelUp()) {
            xp -= GameConfig.XP_PER_LEVEL;
            level++;
            this.maxHp += GameConfig.HP_GROWTH_PER_LEVEL;
            this.hp = this.maxHp;
        }
    }

    public void  setRoundDamageReduction(int v) { roundDamageReduction = v; }
    public int   getRoundDamageReduction()       { return roundDamageReduction; }
    public void  resetRoundDamageReduction()     { roundDamageReduction = 0; }

    public int  getNextActionBonus() { return nextActionBonus; }
    public void setNextActionBonus(int v) { nextActionBonus = v; }
    public void clearNextActionBonus() { nextActionBonus = 0; }

    public String    getId()      { return id; }
    public String    getName()    { return name; }
    public int       getHp()      { return hp; }
    public int       getMaxHp()   { return maxHp; }
    public int       getLevel()   { return level; }
    public int       getXp()      { return xp; }
    public CrewState getState()   { return state; }
    public void      setState(CrewState state) { this.state = state; }
}
