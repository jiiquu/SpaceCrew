package com.example.spacecrewgame.model;

import android.content.Context;
import com.example.spacecrewgame.R;
import com.example.spacecrewgame.enums.ActionType;
import com.example.spacecrewgame.enums.ThreatType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * specialized crew member responsible for ship navigation and tactical evasion.
 * Best used against Debris Fields and Faction Encounters.
 */
public class PilotCrewMember extends CrewMember {

    private static final Random rng = new Random();

    /**
     * Constructs a new Pilot.
     * @param name Display name of the pilot.
     */
    public PilotCrewMember(String name) {
        super(name, 18);
    }

    @Override
    public List<CombatAction> getActions() {
        return Arrays.asList(
            new EvasiveAction(),
            new RammingApproachAction(),
            new HighGManeuverAction()
        );
    }

    @Override
    public ActionResult performAction(String actionId, CombatContext ctx) {
        for (CombatAction action : getActions()) {
            if (action.getId().equals(actionId)) return action.execute(this, ctx);
        }
        throw new IllegalArgumentException("Unknown action for Pilot: " + actionId);
    }

    /**
     * Reduces incoming damage for both crew members.
     */
    private static class EvasiveAction extends CombatAction {
        EvasiveAction() { super("evasive_action", "Evasive Action", ActionType.DEFEND, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            if (type == ThreatType.SHIP_HAZARD || type == ThreatType.BOARDING) {
                return new ActionResult(0, 
                        ctx.getContext().getString(R.string.log_evasive_impossible, actor.getName(), ctx.getThreat().getName()));
            }
            int reduction = 8 + actor.getLevel() * 2;
            return new ActionResult(0, 0, 0, reduction, reduction,
                    ctx.getContext().getString(R.string.log_evasive_action, actor.getName(), reduction));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_evasive_standard, "TARGET");
            ThreatType type = threat.getType();
            if (type == ThreatType.SHIP_HAZARD || type == ThreatType.BOARDING)
                return context.getString(R.string.desc_evasive_none, threat.getName());
            return context.getString(R.string.desc_evasive_standard, threat.getName());
        }
    }

    /**
     * high-risk attack that deals massive damage to ships but can damage self against debris.
     */
    private static class RammingApproachAction extends CombatAction {
        RammingApproachAction() { super("ramming_approach", "Ramming Approach", ActionType.ATTACK, 2); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            if (type == ThreatType.FACTION_ENCOUNTER) {
                int dmg = 12 + actor.getLevel() * 3;
                return new ActionResult(dmg, ctx.getContext().getString(R.string.log_ramming_faction, actor.getName(), dmg));
            } else if (type == ThreatType.PIRATE_INTERCEPT) {
                int dmg = 8 + actor.getLevel() * 2;
                return new ActionResult(dmg, ctx.getContext().getString(R.string.log_ramming_pirate, actor.getName(), dmg));
            } else if (type == ThreatType.DEBRIS_FIELD) {
                int selfDmg = -8, allyDmg = -8;
                return new ActionResult(0, selfDmg, allyDmg, 0, 0,
                        ctx.getContext().getString(R.string.log_ramming_debris, actor.getName(), ctx.getAlly().getName()));
            } else {
                return new ActionResult(0, ctx.getContext().getString(R.string.log_ramming_fail, actor.getName(), ctx.getThreat().getName()));
            }
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_ramming_none);
            ThreatType type = threat.getType();
            if (type == ThreatType.FACTION_ENCOUNTER) return context.getString(R.string.desc_ramming_faction);
            if (type == ThreatType.PIRATE_INTERCEPT) return context.getString(R.string.desc_ramming_pirate);
            if (type == ThreatType.DEBRIS_FIELD) return context.getString(R.string.desc_ramming_debris);
            return context.getString(R.string.desc_ramming_none);
        }
    }

    /**
     * Extreme flight pattern used to shake off debris or boarding parties.
     */
    private static class HighGManeuverAction extends CombatAction {
        HighGManeuverAction() { super("high_g_maneuver", "High-G Maneuver", ActionType.UTILITY, 2); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            Threat threat = ctx.getThreat();
            ThreatType type = threat.getType();
            if (type == ThreatType.DEBRIS_FIELD || type == ThreatType.BOARDING) {
                int chance = Math.min(75, 25 + actor.getLevel() * 5);
                if (rng.nextInt(100) < chance) {
                    int resId = type == ThreatType.DEBRIS_FIELD ? R.string.log_high_g_debris : R.string.log_high_g_boarding;
                    return new ActionResult(threat.getHp(), ctx.getContext().getString(resId, actor.getName()));
                } else {
                    return new ActionResult(0, ctx.getContext().getString(R.string.log_high_g_fail, actor.getName(), threat.getName()));
                }
            } else {
                return new ActionResult(0, ctx.getContext().getString(R.string.log_high_g_impossible, actor.getName(), threat.getName()));
            }
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_high_g_none, "TARGET");
            ThreatType type = threat.getType();
            if (type == ThreatType.DEBRIS_FIELD) return context.getString(R.string.desc_high_g_debris);
            if (type == ThreatType.BOARDING) return context.getString(R.string.desc_high_g_boarding);
            return context.getString(R.string.desc_high_g_none, threat.getName());
        }
    }
}
