package com.example.vitalhome.ui.adapter;

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
import com.example.vitalhome.data.modelo.Medication;
import java.util.List;

/**
 * Adaptador de RecyclerView para la lista de medicamentos (Medication).
 * Muestra el estado de cada medicamento y gestiona el evento de acción al marcarlo como tomado.
 */
public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedViewHolder> {

    // --- Bloque 1: Atributos de la clase e interfaz ---
    private List<Medication> plan;
    private boolean esAbuelo;
    private AccionesMed listener;

    /**
     * Interfaz para notificar acciones de toma de medicamento.
     */
    public interface AccionesMed {
        void tomar(Medication m);
    }

    // --- Bloque 2: Constructor ---
    public MedicationAdapter(List<Medication> plan, boolean esAbuelo, AccionesMed listener) {
        this.plan = plan;
        this.esAbuelo = esAbuelo;
        this.listener = listener;
    }

    // --- Bloque 3: Creación de ViewHolder ---
    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedViewHolder(v);
    }

    // --- Bloque 4: Vinculación de datos y configuración de estado ---
    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int pos) {
        Medication m = plan.get(pos);
        holder.txt.setText(m.getNombre());
        holder.txt.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryDark));
        
        // Configuración visual según si la medicina ya fue tomada o está pendiente
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

    // --- Bloque 5: Conteo de elementos y actualización ---
    @Override
    public int getItemCount() { return plan != null ? plan.size() : 0; }

    public void updateList(List<Medication> newPlan) {
        this.plan = newPlan;
        notifyDataSetChanged();
    }

    // --- Bloque 6: Clase interna ViewHolder ---
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
