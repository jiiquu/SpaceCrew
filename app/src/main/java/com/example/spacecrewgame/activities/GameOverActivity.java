package com.example.spacecrewgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spacecrewgame.R;
import com.example.spacecrewgame.engine.GameEngine;
import com.example.spacecrewgame.model.GameState;
import com.example.spacecrewgame.util.GameConfig;

/**
 * Summarizes the final results of a mission, displaying the score and detailed statistics.
 * Provides a path to restart the game.
 */
public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        GameState state = GameEngine.getInstance().getState();

        TextView tvStatus = findViewById(R.id.tvGameStatus);
        TextView tvScore  = findViewById(R.id.tvScoreSummary);
        TextView tvStats  = findViewById(R.id.tvStats);
        Button   btnRestart = findViewById(R.id.btnRestart);

        if (state.isVictory()) {
            tvStatus.setText(R.string.mission_complete_title);
            tvStatus.setTextColor(getResources().getColor(R.color.text_primary));
        } else {
            tvStatus.setText(R.string.mission_failed_title);
            tvStatus.setTextColor(getResources().getColor(R.color.threat_red));
        }

        tvScore.setText(getString(R.string.final_score, state.calculateScore()));

        String stats = getString(R.string.log_summary_header) + "\n"
                + getString(R.string.stats_turns, state.getShipTurn(), GameConfig.TARGET_TURN) + "\n"
                + getString(R.string.stats_threats, state.getThreatsNeutralized()) + "\n"
                + getString(R.string.stats_damage, state.getTotalDamageTaken()) + "\n"
                + getString(R.string.stats_losses, state.getCrewMembersLost());
        
        tvStats.setText(stats);

        btnRestart.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
