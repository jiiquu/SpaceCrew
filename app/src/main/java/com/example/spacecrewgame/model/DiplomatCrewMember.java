package com.example.spacecrewgame.model;

import android.content.Context;
import com.example.spacecrewgame.R;
import com.example.spacecrewgame.enums.ActionType;
import com.example.spacecrewgame.enums.ThreatType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Diplomat — avoids bloodshed, tactical coordination buffs allies against human threats.
 */
public class DiplomatCrewMember extends CrewMember {

    private static final Random rng = new Random();

    public DiplomatCrewMember(String name) {
        super(name, 14);
    }

    @Override
    public List<CombatAction> getActions() {
        return Arrays.asList(
            new NegotiateAction(),
            new IntimidateAction(),
            new TacticalCoordinationAction()
        );
    }

    @Override
    public ActionResult performAction(String actionId, CombatContext ctx) {
        for (CombatAction action : getActions()) {
            if (action.getId().equals(actionId)) return action.execute(this, ctx);
        }
        throw new IllegalArgumentException("Unknown action for Diplomat: " + actionId);
    }

    // ── Concrete Actions ─────────────────────────────────────────────────────

    private static class NegotiateAction extends CombatAction {
        NegotiateAction() { super("negotiate", "Negotiate", ActionType.UTILITY, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            boolean canNegotiate = (type == ThreatType.FACTION_ENCOUNTER || 
                                   type == ThreatType.PIRATE_INTERCEPT);

            if (!canNegotiate) {
                return new ActionResult(0, 
                        ctx.getContext().getString(R.string.log_negotiate_impossible, actor.getName(), ctx.getThreat().getName()));
            }

            int chance = 25 + actor.getLevel() * 10;
            if (type == ThreatType.PIRATE_INTERCEPT) chance -= 10;
            chance = Math.min(85, Math.max(5, chance));

            boolean win = rng.nextInt(100) < chance;
            if (win) {
                return new ActionResult(ctx.getThreat().getHp(),
                        ctx.getContext().getString(R.string.log_negotiate_success, actor.getName()));
            }
            return new ActionResult(0,
                    ctx.getContext().getString(R.string.log_negotiate_fail, actor.getName()));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_negotiate_faction);
            ThreatType type = threat.getType();
            if (type == ThreatType.FACTION_ENCOUNTER) return context.getString(R.string.desc_negotiate_faction);
            if (type == ThreatType.PIRATE_INTERCEPT) return context.getString(R.string.desc_negotiate_pirate);
            return context.getString(R.string.desc_negotiate_none, threat.getName());
        }
    }

    private static class IntimidateAction extends CombatAction {
        IntimidateAction() { super("intimidate", "Intimidate", ActionType.ATTACK, 2); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            Threat     threat = ctx.getThreat();
            ThreatType type   = threat.getType();
            boolean canIntimidate = (type == ThreatType.FACTION_ENCOUNTER || 
                                    type == ThreatType.PIRATE_INTERCEPT ||
                                    type == ThreatType.BOARDING);

            if (!canIntimidate) {
                return new ActionResult(0, 
                        ctx.getContext().getString(R.string.log_intimidate_impossible, actor.getName(), threat.getName()));
            }
            
            int stunChance = 40 + actor.getLevel() * 5;
            if (rng.nextInt(100) < stunChance) {
                threat.setStunned(true);
                return new ActionResult(0, ctx.getContext().getString(R.string.log_intimidate_stun, actor.getName(), threat.getName()));
            }
            return new ActionResult(0, ctx.getContext().getString(R.string.log_intimidate_fail, actor.getName(), threat.getName()));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_intimidate_human);
            ThreatType type = threat.getType();
            if (type == ThreatType.FACTION_ENCOUNTER || type == ThreatType.PIRATE_INTERCEPT || type == ThreatType.BOARDING)
                return context.getString(R.string.desc_intimidate_human);
            return context.getString(R.string.desc_intimidate_none, threat.getName());
        }
    }

    private static class TacticalCoordinationAction extends CombatAction {
        TacticalCoordinationAction() { super("tactical_coordination", "Tactical Coordination", ActionType.UTILITY, 0); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            boolean canTactical = (type == ThreatType.FACTION_ENCOUNTER || 
                                   type == ThreatType.PIRATE_INTERCEPT ||
                                   type == ThreatType.BOARDING);

            if (!canTactical) {
                return new ActionResult(0, 
                        ctx.getContext().getString(R.string.log_tactical_impossible, ctx.getThreat().getName()));
            }

            int bonus = 8 + actor.getLevel() * 3;
            ctx.getAlly().setNextActionBonus(bonus);
            return new ActionResult(0, 0, 0, 0, 0,
                    ctx.getContext().getString(R.string.log_tactical_coordination, actor.getName(), bonus));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_tactical_human);
            ThreatType type = threat.getType();
            if (type == ThreatType.FACTION_ENCOUNTER || type == ThreatType.PIRATE_INTERCEPT || type == ThreatType.BOARDING)
                return context.getString(R.string.desc_tactical_human);
            return context.getString(R.string.desc_tactical_none, threat.getName());
        }
    }
}
