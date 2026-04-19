package com.example.spacecrewgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spacecrewgame.R;
import com.example.spacecrewgame.engine.GameEngine;

/**
 * Entry point Activity for the application.
 * Provides access to start a new game or trigger debug scenarios.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart     = findViewById(R.id.btnNewGame);
        Button btnDebugWin   = findViewById(R.id.btnDebugWin);
        Button btnDebugLoss  = findViewById(R.id.btnDebugLoss);

        btnStart.setOnClickListener(v -> {
            GameEngine.reset();
            startActivity(new Intent(this, ShipPhaseActivity.class));
        });

        // DEV DEBUG: Instantly go to Victory Screen
        btnDebugWin.setOnClickListener(v -> {
            GameEngine.reset();
            GameEngine.getInstance().getState().setupMockData(true);
            startActivity(new Intent(this, GameOverActivity.class));
        });

        // DEV DEBUG: Instantly go to Loss Screen
        btnDebugLoss.setOnClickListener(v -> {
            GameEngine.reset();
            GameEngine.getInstance().getState().setupMockData(false);
            startActivity(new Intent(this, GameOverActivity.class));
        });
    }
}
