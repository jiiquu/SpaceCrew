package com.example.spacecrewgame.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spacecrewgame.R;
import com.example.spacecrewgame.adapter.CrewRosterAdapter;
import com.example.spacecrewgame.engine.GameEngine;
import com.example.spacecrewgame.enums.TimePhase;
import com.example.spacecrewgame.model.CrewMember;
import com.example.spacecrewgame.model.GameState;
import com.example.spacecrewgame.model.Threat;
import com.example.spacecrewgame.util.GameConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the strategic ship phase where the player can recruit, train, and heal crew members.
 * Acts as the primary hub between tactical combat encounters.
 */
public class ShipPhaseActivity extends AppCompatActivity {

    private TextView   tvTurnInfo;
    private TextView   tvCurrentScore;
    private TextView   tvLog;

    private CrewRosterAdapter adapter;

    private Button btnRecruit;
    private Button btnTrain;
    private Button btnMedbay;
    private Button btnDisband;
    private Button btnPatrol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ship_phase);

        tvTurnInfo     = findViewById(R.id.tvTurnInfo);
        tvCurrentScore = findViewById(R.id.tvCurrentScore);
        tvLog          = findViewById(R.id.tvLog);

        RecyclerView rvCrew = findViewById(R.id.rvCrew);
        rvCrew.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CrewRosterAdapter();
        rvCrew.setAdapter(adapter);

        btnRecruit = findViewById(R.id.btnRecruit);
        btnTrain   = findViewById(R.id.btnTrain);
        btnMedbay  = findViewById(R.id.btnMedbay);
        btnDisband = findViewById(R.id.btnDisband);
        btnPatrol  = findViewById(R.id.btnPatrol);

        btnRecruit.setOnClickListener(v -> showRecruitDialog());
        btnTrain  .setOnClickListener(v -> showCrewPickerDialog(getString(R.string.btn_train), this::assignTraining));
        btnMedbay .setOnClickListener(v -> showCrewPickerDialog(getString(R.string.btn_medbay), this::assignMedbay));
        btnDisband.setOnClickListener(v -> showCrewPickerDialog(getString(R.string.confirm_disband_title), this::disbandCrew));
        btnPatrol .setOnClickListener(v -> doPatrol());

        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    /**
     * Executes the patrol action. If an encounter is triggered, switches phase to COMBAT_TIME.
     */
    private void doPatrol() {
        GameEngine engine = GameEngine.getInstance();
        String result = engine.endShipTurn();
        tvLog.setText(result);
        refreshUI();

        if (engine.getState().getPhase() == TimePhase.COMBAT_TIME) {
            handleCombatTransition();
        }
    }

    /**
     * Identifies available crew and prepares for tactical engagement.
     */
    private void handleCombatTransition() {
        GameEngine engine = GameEngine.getInstance();
        List<CrewMember> available = engine.getState().getAvailableCrewForCombat();

        if (available.isEmpty()) {
            refreshUI();
            return;
        }

        if (available.size() <= 2) {
            engine.getState().setCombatCrew(available);
            startActivity(new Intent(this, CombatActivity.class));
        } else {
            showCombatSelectionDialog(available);
        }
    }

    /**
     * Displays a multi-choice dialog to select exactly 2 crew members for an encounter.
     * @param available The roster members eligible for combat.
     */
    private void showCombatSelectionDialog(List<CrewMember> available) {
        GameEngine engine = GameEngine.getInstance();
        Threat currentThreat = engine.getState().getCurrentThreat();
        String threatTitle;
        if (currentThreat != null) {
            threatTitle = getString(R.string.threat_announcement, 
                    currentThreat.getName(), currentThreat.getHp(), currentThreat.getAttack());
        } else {
            threatTitle = getString(R.string.crew_selection_title);
        }

        String[] names = available.stream()
                .map(c -> {
                    int rating = c.getEffectivenessRating(currentThreat);
                    String star = "★";
                    String ratingStr = star.repeat(Math.max(0, rating / 2));
                    return c.getName() + " LV:" + c.getLevel() + " (EFF: " + ratingStr + ")";
                })
                .toArray(String[]::new);

        boolean[] checked = new boolean[available.size()];
        List<Integer> selectedIndices = new java.util.ArrayList<>();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(threatTitle)
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked) {
                        selectedIndices.add(which);
                    } else {
                        selectedIndices.remove((Integer) which);
                    }
                })
                .setPositiveButton(R.string.btn_deploy, null)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(d -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(v -> {
                if (selectedIndices.size() == 2) {
                    List<CrewMember> selected = new java.util.ArrayList<>();
                    selected.add(available.get(selectedIndices.get(0)));
                    selected.add(available.get(selectedIndices.get(1)));
                    engine.getState().setCombatCrew(selected);
                    dialog.dismiss();
                    startActivity(new Intent(this, CombatActivity.class));
                } else {
                    dialog.setMessage(getString(R.string.select_exactly_2));
                }
            });
        });
        dialog.show();
    }

    private void assignTraining(CrewMember member) {
        String msg = GameEngine.getInstance().assignTraining(member.getId());
        tvLog.setText(msg);
        refreshUI();
    }

    private void assignMedbay(CrewMember member) {
        String msg = GameEngine.getInstance().assignMedbay(member.getId());
        tvLog.setText(msg);
        refreshUI();
    }

    private void disbandCrew(CrewMember member) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_disband_title)
                .setMessage(getString(R.string.confirm_disband_msg, member.getName()))
                .setPositiveButton(R.string.btn_dismiss, (d, w) -> {
                    String msg = GameEngine.getInstance().disbandCrew(member.getId());
                    tvLog.setText(msg);
                    refreshUI();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Displays recruitment interface for adding new specialists to the roster.
     */
    private void showRecruitDialog() {
        GameEngine engine = GameEngine.getInstance();
        if (!engine.getState().canRecruit()) {
            tvLog.setText(R.string.crew_full_msg);
            return;
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_recruit, null);
        EditText etName = view.findViewById(R.id.etCrewName);
        Spinner spinnerClass = view.findViewById(R.id.spinnerClass);
        String[] classes = {"PILOT", "ENGINEER", "MARINE", "SCIENTIST", "DIPLOMAT"};
        spinnerClass.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes));

        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_recruit)
                .setView(view)
                .setPositiveButton(R.string.btn_recruit, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String classType = spinnerClass.getSelectedItem().toString();
                    tvLog.setText(engine.recruitCrew(name, classType));
                    refreshUI();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    interface CrewPickCallback { void onPicked(CrewMember member); }

    /**
     * Utility dialog to select a target for individual ship actions.
     */
    private void showCrewPickerDialog(String title, CrewPickCallback callback) {
        List<CrewMember> roster = GameEngine.getInstance().getState().getCrew();
        if (roster.isEmpty()) return;
        String[] names = roster.stream()
                .map(c -> c.getName() + " LV:" + c.getLevel() + " " + c.getState().name())
                .toArray(String[]::new);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(names, (d, which) -> callback.onPicked(roster.get(which)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Synchronizes all UI components with the persistent game state.
     * Transitions to GameOverActivity if mission goal or failure is detected.
     */
    private void refreshUI() {
        GameEngine engine = GameEngine.getInstance();
        if (engine.getState().isVictory() || engine.getState().isGameOver()) {
            startActivity(new Intent(this, GameOverActivity.class));
            finish();
            return;
        }

        GameState state = engine.getState();
        int actions = state.getActionsRemaining();
        tvTurnInfo.setText(getString(R.string.status_header, 
                state.getShipTurn(), GameConfig.TARGET_TURN, 
                actions, GameConfig.MAX_ACTIONS_PER_TURN));
        
        if (tvCurrentScore != null) {
            tvCurrentScore.setText(getString(R.string.current_score_label, state.calculateScore()));
        }

        boolean hasActions = state.hasActionsLeft();
        btnRecruit.setEnabled(hasActions);
        btnTrain.setEnabled(hasActions);
        btnMedbay.setEnabled(hasActions);
        btnDisband.setEnabled(hasActions);

        adapter.setCrewList(new ArrayList<>(state.getCrew()));
    }

    private String shortClass(CrewMember c) {
        return c.getClass().getSimpleName().replace("CrewMember", "").toUpperCase();
    }
}
