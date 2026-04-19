package com.example.spacecrewgame.model;

import android.content.Context;
import com.example.spacecrewgame.enums.ActionType;

/**
 * Abstract definition of a tactical maneuver a crew member can perform.
 * Actions are decoupled from characters to allow for dynamic state checks.
 */
public abstract class CombatAction {

    private final String id;
    private final String displayName;
    private final ActionType type;
    private final int cooldown;

    /**
     * Constructs a new CombatAction.
     * @param id System identifier.
     * @param displayName Human-readable name.
     * @param type The action category (ATTACK/DEFEND/UTILITY).
     * @param cooldown Number of rounds to skip after usage.
     */
    public CombatAction(String id, String displayName, ActionType type, int cooldown) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.cooldown = cooldown;
    }

    /**
     * Secondary constructor for actions with no cooldown.
     */
    public CombatAction(String id, String displayName, ActionType type) {
        this(id, displayName, type, 0);
    }

    /**
     * Defines the logic for executing this action.
     * @param actor The member performing the action.
     * @param ctx The current environmental state of the battle.
     * @return The resulting changes to health and state.
     */
    public abstract ActionResult execute(CrewMember actor, CombatContext ctx);

    /**
     * Provides a localized, dynamic description of the action's effect.
     * @param context Android context for string resolution.
     * @param threat The active threat to evaluate effectiveness against.
     * @return A formatted string for UI display.
     */
    public abstract String getDescription(Context context, Threat threat);

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public ActionType getType()    { return type; }
    public int getCooldown()       { return cooldown; }
}
