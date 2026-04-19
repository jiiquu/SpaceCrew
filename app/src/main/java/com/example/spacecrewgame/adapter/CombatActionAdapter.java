package com.example.spacecrewgame.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spacecrewgame.R;
import com.example.spacecrewgame.model.CombatAction;
import com.example.spacecrewgame.model.CrewMember;
import com.example.spacecrewgame.model.Threat;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CombatActionAdapter extends RecyclerView.Adapter<CombatActionAdapter.ViewHolder> {

    public interface OnActionSelected { void onSelected(CombatAction action); }

    private List<CombatAction> actions = new ArrayList<>();
    private Threat currentThreat;
    private CrewMember currentCrew;
    private OnActionSelected listener;
    private String selectedId = "";
    private boolean locked = false;

    public void setActions(List<CombatAction> actions, Threat threat, CrewMember crew, OnActionSelected listener) {
        this.actions = actions;
        this.currentThreat = threat;
        this.currentCrew = crew;
        this.listener = listener;
        this.selectedId = "";
        this.locked = false;
        notifyDataSetChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_combat_action, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CombatAction action = actions.get(position);
        holder.tvName.setText(action.getDisplayName());
        holder.tvDesc.setText(action.getDescription(holder.itemView.getContext(), currentThreat));

        // Color coding by type
        int colorRes;
        switch (action.getType()) {
            case ATTACK:  colorRes = R.color.action_attack;  break;
            case DEFEND:  colorRes = R.color.action_defend;  break;
            default:      colorRes = R.color.action_utility; break;
        }
        int color = ContextCompat.getColor(holder.itemView.getContext(), colorRes);
        holder.card.setStrokeColor(color);

        // Cooldown check
        int cooldown = currentCrew != null ? currentCrew.getCooldownRemaining(action.getId()) : 0;
        boolean isOnCooldown = cooldown > 0;

        // Selection highlight
        boolean isSelected = action.getId().equals(selectedId);
        holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), 
                isSelected ? R.color.accent_blue : R.color.bg_dark));
        
        if (locked || isOnCooldown) {
            holder.itemView.setAlpha(isSelected ? 1.0f : 0.4f);
            holder.itemView.setOnClickListener(null);
            if (isOnCooldown) {
                holder.tvDesc.setText(holder.itemView.getContext().getString(R.string.cooldown_msg, cooldown));
            }
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setOnClickListener(v -> {
                selectedId = action.getId();
                notifyDataSetChanged();
                if (listener != null) listener.onSelected(action);
            });
        }
    }

    @Override
    public int getItemCount() { return actions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvName, tvDesc;
        ViewHolder(View v) {
            super(v);
            card = (MaterialCardView) v;
            tvName = v.findViewById(R.id.tvActionName);
            tvDesc = v.findViewById(R.id.tvActionDescription);
        }
    }
}
