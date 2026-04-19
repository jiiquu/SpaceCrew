package com.example.spacecrewgame.util;

public class GameConfig {

    // ── Turn Configuration ───────────────────────────────────────────────────
    public static final int TARGET_TURN           = 30;
    public static final int MAX_ACTIONS_PER_TURN  = 2;
    public static final double ENCOUNTER_CHANCE   = 0.66;

    // ── Crew Configuration ───────────────────────────────────────────────────
    public static final int MAX_CREW_SIZE         = 6;
    public static final int MEDBAY_CAPACITY       = 2;
    public static final int XP_PER_LEVEL          = 100;
    public static final int HP_GROWTH_PER_LEVEL   = 4;
    public static final int PERMANENT_INJURY_HP_LOSS = 3;
    public static final int MIN_VIABLE_MAX_HP     = 5;

    // ── Scoring Configuration ────────────────────────────────────────────────
    public static final int SCORE_PER_TURN        = 100;
    public static final int SCORE_PER_THREAT      = 250;
    public static final int SCORE_PER_CREW_LEVEL  = 150;
    public static final int PENALTY_PER_HP_LOST   = 5;
    public static final int PENALTY_PER_CREW_LOST = 1000;

    // ── Recovery Configuration ───────────────────────────────────────────────
    public static final int MEDBAY_HEAL_AMOUNT    = 6;
    public static final int QUARTERS_HEAL_AMOUNT  = 2;

    // ── Threat Scaling Configuration ─────────────────────────────────────────
    public static final int THREAT_BASE_HP        = 12;
    public static final int THREAT_HP_PER_TURN    = 2;
    public static final int THREAT_BASE_ATK       = 4;
    public static final int THREAT_ATK_PER_TURN   = 1;
    public static final double ELITE_CHANCE       = 0.15;
    public static final double ELITE_STAT_BOOST   = 1.3;
}
