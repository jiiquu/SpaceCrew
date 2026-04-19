package com.example.spacecrewgame.model;

/**
 * Data container representing the multi-faceted outcome of a combat action.
 * Includes damage dealt, healing received, and defensive buffs.
 */
public class ActionResult {

    private final int damageToThreat;
    private final int healToActor;
    private final int healToAlly;
    private final int actorDamageReduction;
    private final int allyDamageReduction;
    private final String logText;

    /**
     * Constructs a full ActionResult.
     * @param damageToThreat Total damage dealt to the enemy.
     * @param healToActor HP restored to the performer.
     * @param healToAlly HP restored to the partner.
     * @param actorDamageReduction Flat damage reduction for the performer this round.
     * @param allyDamageReduction Flat damage reduction for the partner this round.
     * @param logText The narrative description of the result.
     */
    public ActionResult(int damageToThreat,
                        int healToActor,
                        int healToAlly,
                        int actorDamageReduction,
                        int allyDamageReduction,
                        String logText) {
        this.damageToThreat = damageToThreat;
        this.healToActor = healToActor;
        this.healToAlly = healToAlly;
        this.actorDamageReduction = actorDamageReduction;
        this.allyDamageReduction = allyDamageReduction;
        this.logText = logText;
    }

    /**
     * Convenience constructor for simple offensive actions.
     * @param damageToThreat Damage dealt.
     * @param logText Narrative text.
     */
    public ActionResult(int damageToThreat, String logText) {
        this(damageToThreat, 0, 0, 0, 0, logText);
    }

    public int getDamageToThreat()      { return damageToThreat; }
    public int getHealToActor()         { return healToActor; }
    public int getHealToAlly()          { return healToAlly; }
    public int getActorDamageReduction(){ return actorDamageReduction; }
    public int getAllyDamageReduction()  { return allyDamageReduction; }
    public String getLogText()          { return logText; }
}
