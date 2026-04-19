package com.example.spacecrewgame.model;

import com.example.spacecrewgame.enums.ThreatType;

/**
 * Represents an enemy or environmental hazard encountered during patrol.
 * Tracks health, attack power, and status effects.
 */
public class Threat {

    private final String    id;
    private final String    name;
    private final ThreatType type;
    private int             hp;
    private final int       maxHp;
    private final int       attack;
    private boolean         isStunned;

    /**
     * Constructs a new Threat.
     * @param id Unique identifier.
     * @param name Display name.
     * @param type The category of the threat.
     * @param hp Initial health.
     * @param attack Damage dealt per turn.
     */
    public Threat(String id, String name, ThreatType type,
                  int hp, int attack) {
        this.id      = id;
        this.name    = name;
        this.type    = type;
        this.hp      = hp;
        this.maxHp   = hp;
        this.attack  = attack;
        this.isStunned = false;
    }

    /** @return true if HP is zero or less. */
    public boolean isNeutralized() { return hp <= 0; }

    /** Reduces HP by the specified amount, down to a minimum of 0. */
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    public String     getId()      { return id; }
    public String     getName()    { return name; }
    public ThreatType getType()    { return type; }
    public int        getHp()      { return hp; }
    public int        getMaxHp()   { return maxHp; }
    public int        getAttack()  { return attack; }

    public boolean isStunned() { return isStunned; }
    public void setStunned(boolean stunned) { isStunned = stunned; }
}
