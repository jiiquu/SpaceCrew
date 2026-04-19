package com.example.spacecrewgame.enums;

/**
 * Defines the current location and availability of a crew member.
 */
public enum CrewState {
    /** Available for combat or assignment. */
    IN_QUARTERS,
    /** Gaining XP, unavailable for combat. */
    IN_TRAINING,
    /** Healing HP, unavailable for combat. */
    IN_MEDBAY,
    /** HP is 0; must be moved to medbay to recover. */
    INCAPACITATED
}
