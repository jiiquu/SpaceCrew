package com.example.spacecrewgame.model;

import android.content.Context;
import com.example.spacecrewgame.R;
import com.example.spacecrewgame.enums.ActionType;
import com.example.spacecrewgame.enums.ThreatType;

import java.util.Arrays;
import java.util.List;

/**
 * Marine — operates weapon systems and fights boarding parties.
 */
public class MarineCrewMember extends CrewMember {

    public MarineCrewMember(String name) {
        super(name, 22);
    }

    @Override
    public List<CombatAction> getActions() {
        return Arrays.asList(
            new WeaponSystemsAction(),
            new FortifyPositionAction(),
            new CloseCombatAction()
        );
    }

    @Override
    public ActionResult performAction(String actionId, CombatContext ctx) {
        for (CombatAction action : getActions()) {
            if (action.getId().equals(actionId)) return action.execute(this, ctx);
        }
        throw new IllegalArgumentException("Unknown action for Marine: " + actionId);
    }

    // ── Concrete Actions ─────────────────────────────────────────────────────

    private static class WeaponSystemsAction extends CombatAction {
        WeaponSystemsAction() { super("weapon_systems", "Weapon Systems", ActionType.ATTACK, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type  = ctx.getThreat().getType();
            int dmg;
            
            if (type == ThreatType.PIRATE_INTERCEPT) {
                dmg = 14 + actor.getLevel() * 3;
                return new ActionResult(dmg, ctx.getContext().getString(R.string.log_weapon_pirate, actor.getName(), dmg));
            } else if (type == ThreatType.DEBRIS_FIELD || type == ThreatType.FACTION_ENCOUNTER) {
                dmg = 8 + actor.getLevel() * 2;
                return new ActionResult(dmg, ctx.getContext().getString(R.string.log_weapon_standard, actor.getName(), ctx.getThreat().getName(), dmg));
            } else {
                return new ActionResult(0, ctx.getContext().getString(R.string.log_weapon_impossible, actor.getName(), ctx.getThreat().getName()));
            }
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_weapon_standard);
            ThreatType type = threat.getType();
            if (type == ThreatType.PIRATE_INTERCEPT) return context.getString(R.string.desc_weapon_pirate);
            if (type == ThreatType.DEBRIS_FIELD || type == ThreatType.FACTION_ENCOUNTER) return context.getString(R.string.desc_weapon_standard);
            return context.getString(R.string.desc_weapon_none, threat.getName());
        }
    }

    private static class FortifyPositionAction extends CombatAction {
        FortifyPositionAction() { super("fortify_position", "Fortify Position", ActionType.DEFEND, 2); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type  = ctx.getThreat().getType();
            int reduction;

            if (type == ThreatType.BOARDING) {
                reduction = 12 + actor.getLevel() * 3;
                return new ActionResult(0, 0, 0, reduction, reduction, 
                        ctx.getContext().getString(R.string.log_fortify_boarding, actor.getName(), reduction));
            } else if (type == ThreatType.PIRATE_INTERCEPT || type == ThreatType.FACTION_ENCOUNTER || type == ThreatType.DEBRIS_FIELD) {
                reduction = 7 + actor.getLevel() * 2;
                return new ActionResult(0, 0, 0, reduction, reduction, 
                        ctx.getContext().getString(R.string.log_fortify_standard, actor.getName(), reduction));
            } else {
                return new ActionResult(0, 0, 0, 0, 0, 
                        ctx.getContext().getString(R.string.log_fortify_impossible, actor.getName(), ctx.getThreat().getName()));
            }
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_fortify_standard);
            ThreatType type = threat.getType();
            if (type == ThreatType.BOARDING) return context.getString(R.string.desc_fortify_boarding);
            if (type == ThreatType.SHIP_HAZARD) return context.getString(R.string.desc_fortify_hazard);
            return context.getString(R.string.desc_fortify_standard);
        }
    }

    private static class CloseCombatAction extends CombatAction {
        CloseCombatAction() { super("close_combat", "Close Combat", ActionType.ATTACK, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            int dmg;
            int selfDmg = 0;

            if (type == ThreatType.BOARDING) {
                dmg = 16 + actor.getLevel() * 4;
                selfDmg = 5;
                return new ActionResult(dmg, -selfDmg, 0, 0, 0, 
                        ctx.getContext().getString(R.string.log_close_combat_boarding, actor.getName(), dmg, selfDmg));
            } else if (type == ThreatType.SHIP_HAZARD) {
                dmg = 10 + actor.getLevel() * 2;
                selfDmg = 3;
                return new ActionResult(dmg, -selfDmg, 0, 0, 0, 
                        ctx.getContext().getString(R.string.log_close_combat_hazard, actor.getName(), dmg, selfDmg));
            } else {
                return new ActionResult(0, 0, 0, 0, 0, 
                        ctx.getContext().getString(R.string.log_close_combat_fail, actor.getName(), ctx.getThreat().getName()));
            }
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_close_combat_boarding);
            ThreatType type = threat.getType();
            if (type == ThreatType.BOARDING) return context.getString(R.string.desc_close_combat_boarding);
            if (type == ThreatType.SHIP_HAZARD) return context.getString(R.string.desc_close_combat_hazard);
            return context.getString(R.string.desc_close_combat_none);
        }
    }
}
