package com.example.spacecrewgame.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spacecrewgame.R;
import com.example.spacecrewgame.adapter.CombatActionAdapter;
import com.example.spacecrewgame.engine.GameEngine;
import com.example.spacecrewgame.enums.CrewState;
import com.example.spacecrewgame.model.ActionResult;
import com.example.spacecrewgame.model.CombatAction;
import com.example.spacecrewgame.model.CrewMember;
import com.example.spacecrewgame.model.Threat;

import java.util.List;

/**
 * Tactical Combat Activity.
 * Manages action card selection for crew members, round resolution logic,
 * and displays the narrative combat log.
 */
public class CombatActivity extends AppCompatActivity {

    private GameEngine          engine;
    private List<CrewMember>    activeCrew;
    private boolean             isCombatFinished = false;

    private TextView tvThreat;
    private TextView tvLog;
    private TextView tvCrewAName;
    private TextView tvCrewBName;
    private View     layoutCrewB;

    private CombatActionAdapter adapterA;
    private CombatActionAdapter adapterB;

    private CombatAction selectedActionA;
    private CombatAction selectedActionB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combat);

        engine         = GameEngine.getInstance();
        tvThreat       = findViewById(R.id.tvThreatInfo);
        tvLog          = findViewById(R.id.tvCombatLog);
        tvCrewAName    = findViewById(R.id.tvCrewAName); 
        tvCrewBName    = findViewById(R.id.tvCrewBName);
        layoutCrewB    = findViewById(R.id.layoutCrewB); 

        RecyclerView rvActionsA = findViewById(R.id.rvActionsA);
        RecyclerView rvActionsB = findViewById(R.id.rvActionsB);
        
        rvActionsA.setLayoutManager(new LinearLayoutManager(this));
        rvActionsB.setLayoutManager(new LinearLayoutManager(this));
        
        adapterA = new CombatActionAdapter();
        adapterB = new CombatActionAdapter();
        
        rvActionsA.setAdapter(adapterA);
        rvActionsB.setAdapter(adapterB);

        Button btnResolve = findViewById(R.id.btnResolve);
        btnResolve.setOnClickListener(v -> resolveRound());

        refreshUI();
    }

    /**
     * Executes the logic for a single combat round.
     * Submits selected actions to the engine and processes the threat's turn.
     * Updates the UI and checks for mission completion or failure.
     */
    private void resolveRound() {
        if (isCombatFinished) return;
        if (activeCrew == null || activeCrew.isEmpty()) return;

        String idA = activeCrew.get(0).getId();
        boolean crewBExists = activeCrew.size() > 1;
        String idB = crewBExists ? activeCrew.get(1).getId() : idA;

        ActionResult resA = new ActionResult(0, "");
        if (activeCrew.get(0).getState() != CrewState.INCAPACITATED) {
            if (selectedActionA != null) {
                resA = engine.submitCombatAction(this, idA, selectedActionA.getId(), idB);
            } else {
                resA = new ActionResult(0, getString(R.string.action_wait_msg, activeCrew.get(0).getName()));
            }
        }

        ActionResult resB = new ActionResult(0, "");
        if (crewBExists && activeCrew.get(1).getState() != CrewState.INCAPACITATED && engine.getState().getCurrentThreat() != null) {
            if (selectedActionB != null) {
                resB = engine.submitCombatAction(this, idB, selectedActionB.getId(), idA);
            } else {
                resB = new ActionResult(0, getString(R.string.action_wait_msg, activeCrew.get(1).getName()));
            }
        }

        ActionResult resThreat = new ActionResult(0, "");
        if (engine.getState().getCurrentThreat() != null) {
            resThreat = engine.resolveThreatTurn(idA, idB);
        }

        StringBuilder log = new StringBuilder();
        if (!resA.getLogText().isEmpty())      log.append(resA.getLogText()).append("\n");
        if (!resB.getLogText().isEmpty())      log.append(resB.getLogText()).append("\n");
        if (!resThreat.getLogText().isEmpty()) log.append(resThreat.getLogText());
        tvLog.setText(log.toString().trim());

        if (engine.getState().getCurrentThreat() == null || areAllIncapacitated()) {
            isCombatFinished = true;
        }

        refreshUI();

        if (isCombatFinished) {
            boolean lost = areAllIncapacitated();
            String finalMsg = lost ? getString(R.string.mission_failed_log) : getString(R.string.threat_neutralized_log);
            tvLog.append(finalMsg);
            btnSetupReturn();
        }
    }

    /**
     * Refreshes UI elements based on the latest state from the GameEngine.
     * Resets action selections and locks/unlocks the interface based on combat status.
     */
    private void refreshUI() {
        Threat threat = engine.getState().getCurrentThreat();
        if (threat == null && !isCombatFinished) { finish(); return; }

        if (activeCrew == null || !isCombatFinished) {
            activeCrew = engine.getState().getActiveCombatCrew();
        }

        if (threat != null) {
            tvThreat.setText(getString(R.string.threat_announcement, threat.getName(), threat.getHp(), threat.getAttack()));
        } else {
            tvThreat.setText(getString(R.string.combat_resolved_header));
        }

        if (!isCombatFinished) {
            selectedActionA = null;
            selectedActionB = null;
        }

        if (!activeCrew.isEmpty()) {
            CrewMember cA = activeCrew.get(0);
            tvCrewAName.setText(getString(R.string.crew_status_display, cA.getName(), cA.getHp()));
            adapterA.setActions(cA.getActions(), threat, cA, action -> selectedActionA = action);
            adapterA.setLocked(isCombatFinished || cA.getState() == CrewState.INCAPACITATED);
        }

        if (activeCrew.size() > 1) {
            layoutCrewB.setVisibility(View.VISIBLE);
            CrewMember cB = activeCrew.get(1);
            tvCrewBName.setText(getString(R.string.crew_status_display, cB.getName(), cB.getHp()));
            adapterB.setActions(cB.getActions(), threat, cB, action -> selectedActionB = action);
            adapterB.setLocked(isCombatFinished || cB.getState() == CrewState.INCAPACITATED);
        } else {
            layoutCrewB.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the main resolve button to allow the player to return to the ship view.
     */
    private void btnSetupReturn() {
        Button btn = findViewById(R.id.btnResolve);
        btn.setText(R.string.btn_return);
        btn.setEnabled(true);
        btn.setOnClickListener(v -> {
            engine.startNextTurn();
            engine.getState().clearCombatCrew();
            finish();
        });
    }

    /**
     * Determines if all deployed crew members are currently incapacitated.
     *
     * @return true if all active members have 0 HP.
     */
    private boolean areAllIncapacitated() {
        if (activeCrew == null || activeCrew.isEmpty()) return false;
        for (CrewMember c : activeCrew) {
            if (c.getState() != CrewState.INCAPACITATED) return false;
        }
        return true;
    }
}
