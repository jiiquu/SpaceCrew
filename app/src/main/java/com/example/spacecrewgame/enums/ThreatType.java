package com.example.spacecrewgame.enums;

/**
 * Identifies the nature of an encounter, determining class effectiveness.
 */
public enum ThreatType {
    /** Internal threat; vulnerable to Engineers and Marines. */
    BOARDING,
    /** Internal malfunction; vulnerable to Engineers and Scientists. */
    SHIP_HAZARD,
    /** Environmental hazard; vulnerable to Pilots and Scientists. */
    DEBRIS_FIELD,
    /** External hostile; vulnerable to Marines and Pilots. */
    PIRATE_INTERCEPT,
    /** Diplomatic standoff; vulnerable to Diplomats and Pilots. */
    FACTION_ENCOUNTER
}
