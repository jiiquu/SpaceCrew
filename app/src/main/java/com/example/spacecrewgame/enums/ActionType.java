package com.example.spacecrewgame.enums;

/**
 * Categorizes the intent of a combat action.
 */
public enum ActionType {
    /** Deals direct damage to the threat. */
    ATTACK,
    /** Reduces incoming damage for the crew. */
    DEFEND,
    /** Provides support effects like healing or buffs. */
    UTILITY
}
