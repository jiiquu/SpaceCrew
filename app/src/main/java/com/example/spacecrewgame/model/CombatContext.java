package com.example.spacecrewgame.model;

import android.content.Context;

/**
 * A snapshot object that aggregates all entities involved in a single combat round.
 * Passed to CombatActions to provide data needed for logic execution.
 */
public class CombatContext {

    private final Threat threat;
    private final CrewMember actor;
    private final CrewMember ally;
    private final int round;
    private final Context context;

    /**
     * Constructs a combat context.
     * @param context Application context for resource access.
     * @param threat The entity being fought.
     * @param actor The crew member currently acting.
     * @param ally The partner crew member.
     * @param round The current round number.
     */
    public CombatContext(Context context, Threat threat, CrewMember actor, CrewMember ally, int round) {
        this.context = context;
        this.threat = threat;
        this.actor  = actor;
        this.ally   = ally;
        this.round  = round;
    }

    public Threat     getThreat() { return threat; }
    public CrewMember getActor()  { return actor; }
    public CrewMember getAlly()   { return ally; }
    public int        getRound()  { return round; }
    public Context    getContext() { return context; }
}
