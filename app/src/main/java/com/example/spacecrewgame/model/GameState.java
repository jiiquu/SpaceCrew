package com.example.spacecrewgame.model;

import com.example.spacecrewgame.enums.CrewState;
import com.example.spacecrewgame.enums.TimePhase;
import com.example.spacecrewgame.util.GameConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * The single source of truth for a game session's data.
 * Persists turn progress, roster state, and cumulative scoring metrics.
 */
public class GameState {

    private int              shipTurn;
    private int              actionsPerformed;
    private TimePhase        phase;
    private List<CrewMember> crew;
    private Threat           currentThreat;
    private List<String>     combatCrewIds;

    // Score Tracking
    private int threatsNeutralized = 0;
    private int totalDamageTaken = 0;
    private int crewMembersLost = 0;

    public GameState() {
        this.shipTurn         = 1;
        this.actionsPerformed = 0;
        this.phase            = TimePhase.SHIP_TIME;
        this.crew             = new ArrayList<>();
        this.currentThreat    = null;
        this.combatCrewIds    = new ArrayList<>();
    }

    /**
     * DEV ONLY: Seeds the state with data to test end-game scenarios.
     * @param win If true, seeds for victory; if false, seeds for loss.
     */
    public void setupMockData(boolean win) {
        this.shipTurn = win ? GameConfig.TARGET_TURN : 12;
        this.threatsNeutralized = 15;
        this.totalDamageTaken = 42;
        this.crewMembersLost = win ? 0 : 4;
        if (!win) {
            this.crew.clear(); 
        }
    }

    /** @return Crew members eligible for deployment (healthy and in quarters). */
    public List<CrewMember> getAvailableCrewForCombat() {
        List<CrewMember> available = new ArrayList<>();
        for (CrewMember c : crew) {
            if (c.getState() == CrewState.IN_QUARTERS && c.getHp() > 0) {
                available.add(c);
            }
        }
        return available;
    }

    /** @return The list of crew members deployed in the current active combat. */
    public List<CrewMember> getActiveCombatCrew() {
        List<CrewMember> active = new ArrayList<>();
        for (String id : combatCrewIds) {
            for (CrewMember c : crew) {
                if (c.getId().equals(id)) {
                    active.add(c);
                    break;
                }
            }
        }
        return active;
    }

    /** Locks in the selected crew members for the duration of an encounter. */
    public void setCombatCrew(List<CrewMember> selected) {
        this.combatCrewIds.clear();
        for (CrewMember c : selected) {
            this.combatCrewIds.add(c.getId());
        }
    }

    /** @return true if the turn limit has been reached. */
    public boolean isVictory() {
        return shipTurn >= GameConfig.TARGET_TURN;
    }

    /** 
     * Analyzes failure conditions.
     * @return true if the roster is empty or no deployed crew can continue.
     */
    public boolean isGameOver() {
        if (crew.isEmpty()) return true;

        if (phase == TimePhase.COMBAT_TIME) {
            List<CrewMember> active = getActiveCombatCrew();
            if (active.isEmpty()) {
                return getAvailableCrewForCombat().isEmpty();
            }
            for (CrewMember c : active) {
                if (c.getState() != CrewState.INCAPACITATED) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Performs final score calculation based on performance variables.
     * @return The calculated score.
     */
    public int calculateScore() {
        int score = 0;
        score += shipTurn * GameConfig.SCORE_PER_TURN;
        score += threatsNeutralized * GameConfig.SCORE_PER_THREAT;
        
        for (CrewMember c : crew) {
            score += c.getLevel() * GameConfig.SCORE_PER_CREW_LEVEL;
        }

        score -= totalDamageTaken * GameConfig.PENALTY_PER_HP_LOST;
        score -= crewMembersLost * GameConfig.PENALTY_PER_CREW_LOST;

        return Math.max(0, score);
    }

    public void recordThreatNeutralized() { threatsNeutralized++; }
    public void recordDamageTaken(int dmg) { totalDamageTaken += dmg; }
    public void recordCrewLost() { crewMembersLost++; }

    public int getThreatsNeutralized() { return threatsNeutralized; }
    public int getTotalDamageTaken()   { return totalDamageTaken; }
    public int getCrewMembersLost()    { return crewMembersLost; }

    public boolean canRecruit() { return crew.size() < GameConfig.MAX_CREW_SIZE; }

    public int getMedbayCount() {
        int count = 0;
        for (CrewMember c : crew) {
            if (c.getState() == CrewState.IN_MEDBAY) count++;
        }
        return count;
    }

    public int getTrainingCount() {
        int count = 0;
        for (CrewMember c : crew) {
            if (c.getState() == CrewState.IN_TRAINING) count++;
        }
        return count;
    }

    public boolean medbayHasSpace() { return getMedbayCount() < GameConfig.MEDBAY_CAPACITY; }

    public void addCrew(CrewMember member)    { crew.add(member); }
    public void removeCrew(CrewMember member) { crew.remove(member); }

    public int        getShipTurn()   { return shipTurn; }
    public void       incrementShipTurn() {
        shipTurn++;
        actionsPerformed = 0;
    }

    public int getActionsRemaining() {
        return GameConfig.MAX_ACTIONS_PER_TURN - actionsPerformed;
    }

    public boolean hasActionsLeft() {
        return actionsPerformed < GameConfig.MAX_ACTIONS_PER_TURN;
    }

    public void recordAction() {
        actionsPerformed++;
    }

    public TimePhase  getPhase()      { return phase; }
    public void       setPhase(TimePhase phase) { this.phase = phase; }

    public void clearCombatCrew() { this.combatCrewIds.clear(); }

    public List<CrewMember> getCrew() { return crew; }

    public Threat     getCurrentThreat()           { return currentThreat; }
    public void       setCurrentThreat(Threat t)   { this.currentThreat = t; }
}
