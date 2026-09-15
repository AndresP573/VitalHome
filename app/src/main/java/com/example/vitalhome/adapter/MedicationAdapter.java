package com.example.vitalhome.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.modelo.Medication;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedViewHolder> {

    private List<Medication> plan;
    private boolean esAbuelo;
    private AccionesMed listener;

    public interface AccionesMed {
        void tomar(Medication m);
    }

    public MedicationAdapter(List<Medication> plan, boolean esAbuelo, AccionesMed listener) {
        this.plan = plan;
        this.esAbuelo = esAbuelo;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int pos) {
        Medication m = plan.get(pos);
        holder.txt.setText(m.getNombre());
        holder.txt.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryDark));
        
        if (m.isTaken()) {
            holder.badge.setText("Tomado");
            holder.badge.setBackgroundColor(Color.parseColor("#E6FFFA"));
            holder.badge.setTextColor(Color.parseColor("#059669"));
            holder.btn.setVisibility(View.GONE);
        } else {
            holder.badge.setText(m.getHora());
            holder.badge.setBackgroundColor(Color.parseColor("#FFF9E6"));
            holder.badge.setTextColor(Color.parseColor("#D97706"));
            
            if (esAbuelo) {
                holder.btn.setVisibility(View.VISIBLE);
                holder.btn.setOnClickListener(v -> {
                    if (listener != null) listener.tomar(m);
                });
            } else {
                holder.btn.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() { return plan.size(); }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView txt, badge;
        Button btn;
        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.tvMedName);
            badge = itemView.findViewById(R.id.tvMedBadge);
            btn = itemView.findViewById(R.id.btnTakeMed);
        }
    }
}