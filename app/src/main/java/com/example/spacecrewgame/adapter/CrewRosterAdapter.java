package com.example.spacecrewgame.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spacecrewgame.R;
import com.example.spacecrewgame.model.CrewMember;
import com.example.spacecrewgame.util.GameConfig;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrewRosterAdapter extends RecyclerView.Adapter<CrewRosterAdapter.ViewHolder> {

    private List<CrewMember> crewList = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();
    private boolean selectionMode = false;
    private final int maxSelection = 2;

    public void setCrewList(List<CrewMember> newList) {
        this.crewList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        this.selectedIds.clear();
        notifyDataSetChanged();
    }

    public List<CrewMember> getSelectedCrew() {
        List<CrewMember> selected = new ArrayList<>();
        for (CrewMember c : crewList) {
            if (selectedIds.contains(c.getId())) {
                selected.add(c);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crew_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CrewMember member = crewList.get(position);
        
        holder.tvName.setText("> " + member.getName().toUpperCase());
        holder.tvClass.setText("CLASS: " + shortClass(member));
        holder.tvLevel.setText("LV: " + String.format("%02d", member.getLevel()));
        holder.tvState.setText("STATUS: " + member.getState().name());

        holder.pbHp.setMax(member.getMaxHp());
        holder.pbHp.setProgress(member.getHp());

        holder.pbXp.setMax(GameConfig.XP_PER_LEVEL);
        holder.pbXp.setProgress(member.getXp());

        if (selectionMode) {
            boolean isSelected = selectedIds.contains(member.getId());
            holder.cardView.setStrokeWidth(isSelected ? 6 : 1);
            holder.cardView.setStrokeColor(isSelected ? Color.YELLOW : Color.GREEN);
            
            holder.itemView.setOnClickListener(v -> {
                if (selectedIds.contains(member.getId())) {
                    selectedIds.remove(member.getId());
                } else if (selectedIds.size() < maxSelection) {
                    selectedIds.add(member.getId());
                }
                notifyItemChanged(holder.getAdapterPosition());
            });
        } else {
            holder.cardView.setStrokeWidth(1);
            holder.cardView.setStrokeColor(Color.GREEN);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return crewList.size();
    }

    private String shortClass(CrewMember c) {
        return c.getClass().getSimpleName().replace("CrewMember", "").toUpperCase();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvName, tvClass, tvLevel, tvState;
        ProgressBar pbHp, pbXp;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tvName);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvState = itemView.findViewById(R.id.tvState);
            pbHp = itemView.findViewById(R.id.pbHp);
            pbXp = itemView.findViewById(R.id.pbXp);
        }
    }
}
