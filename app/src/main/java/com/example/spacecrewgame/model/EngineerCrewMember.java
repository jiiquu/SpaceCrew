package com.example.spacecrewgame.model;

import android.content.Context;
import com.example.spacecrewgame.R;
import com.example.spacecrewgame.enums.ActionType;
import com.example.spacecrewgame.enums.ThreatType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Engineer — tunes power output to shields and propulsion.
 */
public class EngineerCrewMember extends CrewMember {

    private static final Random rng = new Random();

    public EngineerCrewMember(String name) {
        super(name, 16);
    }

    @Override
    public List<CombatAction> getActions() {
        return Arrays.asList(
            new BoostShieldsAction(),
            new OverloadBurstAction(),
            new IsolateSystemsAction()
        );
    }

    @Override
    public ActionResult performAction(String actionId, CombatContext ctx) {
        for (CombatAction action : getActions()) {
            if (action.getId().equals(actionId)) return action.execute(this, ctx);
        }
        throw new IllegalArgumentException("Unknown action for Engineer: " + actionId);
    }

    // ── Concrete Actions ─────────────────────────────────────────────────────

    private static class BoostShieldsAction extends CombatAction {
        BoostShieldsAction() { super("boost_shields", "Boost Shields", ActionType.DEFEND, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            if (type == ThreatType.SHIP_HAZARD || type == ThreatType.BOARDING) {
                return new ActionResult(0, 0, 0, 0, 0,
                        ctx.getContext().getString(R.string.log_shields_impossible, actor.getName(), ctx.getThreat().getName()));
            }
            int reduction = 10 + actor.getLevel() * 3;
            return new ActionResult(0, 0, 0, reduction, reduction,
                    ctx.getContext().getString(R.string.log_shields_standard, actor.getName(), reduction));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_shields_standard);
            if (threat.getType() == ThreatType.SHIP_HAZARD || threat.getType() == ThreatType.BOARDING)
                return context.getString(R.string.desc_shields_none, threat.getName());
            return context.getString(R.string.desc_shields_standard);
        }
    }

    private static class OverloadBurstAction extends CombatAction {
        OverloadBurstAction() { super("overload_burst", "Overload Burst", ActionType.ATTACK, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            if (type == ThreatType.SHIP_HAZARD || type == ThreatType.BOARDING) {
                return new ActionResult(0, ctx.getContext().getString(R.string.log_overload_impossible, actor.getName(), ctx.getThreat().getName()));
            }
            int dmg   = 5 + actor.getLevel() * 2;
            return new ActionResult(dmg, ctx.getContext().getString(R.string.log_overload_standard, actor.getName(), dmg));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_overload_standard);
            if (threat.getType() == ThreatType.SHIP_HAZARD || threat.getType() == ThreatType.BOARDING)
                return context.getString(R.string.desc_overload_none, threat.getName());
            return context.getString(R.string.desc_overload_standard);
        }
    }

    private static class IsolateSystemsAction extends CombatAction {
        IsolateSystemsAction() { super("isolate_systems", "Isolate Systems", ActionType.UTILITY, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            Threat threat = ctx.getThreat();
            ThreatType type = threat.getType();
            if (type == ThreatType.BOARDING) {
                threat.setStunned(true);
                return new ActionResult(0, ctx.getContext().getString(R.string.log_isolate_boarding, actor.getName()));
            } else if (type == ThreatType.SHIP_HAZARD) {
                int chance = Math.min(90, 30 + actor.getLevel() * 10);
                if (rng.nextInt(100) < chance) {
                    return new ActionResult(threat.getHp(), ctx.getContext().getString(R.string.log_isolate_hazard_success, actor.getName()));
                }
                return new ActionResult(0, ctx.getContext().getString(R.string.log_isolate_hazard_fail, actor.getName()));
            }
            return new ActionResult(0, ctx.getContext().getString(R.string.log_isolate_impossible, actor.getName(), threat.getName()));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_isolate_hazard);
            if (threat.getType() == ThreatType.BOARDING) return context.getString(R.string.desc_isolate_boarding);
            if (threat.getType() == ThreatType.SHIP_HAZARD) return context.getString(R.string.desc_isolate_hazard);
            return context.getString(R.string.desc_isolate_none);
        }
    }
}
