package com.example.spacecrewgame.model;

import android.content.Context;
import com.example.spacecrewgame.R;
import com.example.spacecrewgame.enums.ActionType;
import com.example.spacecrewgame.enums.ThreatType;

import java.util.Arrays;
import java.util.List;

/**
 * Scientist — hacks enemy systems and heals allies.
 */
public class ScientistCrewMember extends CrewMember {

    public ScientistCrewMember(String name) {
        super(name, 14);
    }

    @Override
    public List<CombatAction> getActions() {
        return Arrays.asList(
            new HackSystemsAction(),
            new FieldMedicineAction(),
            new PatternAnalysisAction()
        );
    }

    @Override
    public ActionResult performAction(String actionId, CombatContext ctx) {
        for (CombatAction action : getActions()) {
            if (action.getId().equals(actionId)) return action.execute(this, ctx);
        }
        throw new IllegalArgumentException("Unknown action for Scientist: " + actionId);
    }

    // ── Concrete Actions ─────────────────────────────────────────────────────

    private static class HackSystemsAction extends CombatAction {
        HackSystemsAction() { super("hack_systems", "Hack Systems", ActionType.ATTACK, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            boolean canHack = (type == ThreatType.FACTION_ENCOUNTER || 
                               type == ThreatType.PIRATE_INTERCEPT ||
                               type == ThreatType.BOARDING);

            if (!canHack) {
                return new ActionResult(0, 
                        ctx.getContext().getString(R.string.log_hack_impossible, actor.getName(), ctx.getThreat().getName()));
            }

            int dmg = 5 + actor.getLevel() * 3;
            return new ActionResult(dmg,
                    ctx.getContext().getString(R.string.log_hack_success, actor.getName(), dmg));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_hack_human);
            ThreatType type = threat.getType();
            if (type == ThreatType.FACTION_ENCOUNTER || type == ThreatType.PIRATE_INTERCEPT || type == ThreatType.BOARDING)
                return context.getString(R.string.desc_hack_human);
            return context.getString(R.string.desc_hack_none, threat.getName());
        }
    }

    private static class FieldMedicineAction extends CombatAction {
        FieldMedicineAction() { super("field_medicine", "Field Medicine", ActionType.UTILITY, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            int healAmt = 6 + actor.getLevel() * 2;
            return new ActionResult(0, 0, healAmt, 0, 0,
                    ctx.getContext().getString(R.string.log_field_medicine, actor.getName(), ctx.getAlly().getName(), healAmt));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            return context.getString(R.string.desc_field_medicine);
        }
    }

    private static class PatternAnalysisAction extends CombatAction {
        PatternAnalysisAction() { super("pattern_analysis", "Pattern Analysis", ActionType.UTILITY, 1); }

        @Override
        public ActionResult execute(CrewMember actor, CombatContext ctx) {
            ThreatType type = ctx.getThreat().getType();
            boolean canAnalyze = (type == ThreatType.DEBRIS_FIELD || type == ThreatType.SHIP_HAZARD);

            if (!canAnalyze) {
                return new ActionResult(0, 
                        ctx.getContext().getString(R.string.log_pattern_impossible, actor.getName(), ctx.getThreat().getName()));
            }

            int bonus = 6 + actor.getLevel() * 2;
            ctx.getAlly().setNextActionBonus(bonus);

            int dmg = 2 + actor.getLevel();
            return new ActionResult(dmg, 0, 0, 0, 0,
                    ctx.getContext().getString(R.string.log_pattern_analysis, actor.getName(), ctx.getThreat().getName(), ctx.getAlly().getName(), bonus));
        }

        @Override
        public String getDescription(Context context, Threat threat) {
            if (threat == null) return context.getString(R.string.desc_pattern_analysis);
            ThreatType type = threat.getType();
            if (type == ThreatType.DEBRIS_FIELD || type == ThreatType.SHIP_HAZARD)
                return context.getString(R.string.desc_pattern_analysis);
            return context.getString(R.string.desc_pattern_none, threat.getName());
        }
    }
}
