package com.example.vitalhome.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.Medication;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adaptador de RecyclerView para administrar la visualización y acciones del plan diario de medicamentos.
 * Muestra el horario del medicamento en verde al ser tomado sin texto adicional.
 */
public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedViewHolder> {

    /**
     * Interfaz de comunicación para gestionar las acciones de marcado de medicamentos.
     */
    public interface AccionesMed {
        void tomar(Medication m);
    }

    // --- Atributos del adaptador ---
    private List<Medication> items;
    private final boolean esAbuelo;
    private final AccionesMed listener;

    /**
     * Constructor del adaptador de medicamentos.
     *
     * @param items Lista inicial de medicamentos.
     * @param esAbuelo Indica si la vista la utiliza el perfil cliente/abuelo para habilitar el botón de toma.
     * @param listener Interfaz de callback para ejecutar las acciones.
     */
    public MedicationAdapter(List<Medication> items, boolean esAbuelo, AccionesMed listener) {
        this.items = Objects.requireNonNullElseGet(items, ArrayList::new);
        this.esAbuelo = esAbuelo;
        this.listener = listener;
    }

    /**
     * Actualiza la lista interna de medicamentos.
     *
     * @param newItems Lista actualizada de remedios.
     */
    public void updateList(List<Medication> newItems) {
        this.items = Objects.requireNonNullElseGet(newItems, ArrayList::new);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medication m = items.get(position);

        holder.tvNombre.setText(m.getNombre());
        
        // Muestra exclusivamente la hora cargada (ej. "10:49") sin ticks ni palabras agregadas
        holder.tvBadge.setText(m.getHora());

        if (m.isTaken()) {
            // Remedio tomado: Texto de la hora en color verde con fondo verde suave
            holder.tvBadge.setBackgroundColor(Color.parseColor("#E6F4EA"));
            holder.tvBadge.setTextColor(Color.parseColor("#10B981"));
            holder.btnTomar.setVisibility(View.GONE);
        } else {
            // Remedio pendiente: Texto de la hora en color naranja/ámbar
            holder.tvBadge.setBackgroundColor(Color.parseColor("#FFF9E6"));
            holder.tvBadge.setTextColor(Color.parseColor("#D97706"));

            if (esAbuelo) {
                holder.btnTomar.setVisibility(View.VISIBLE);
                holder.btnTomar.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.tomar(m);
                    }
                });
            } else {
                holder.btnTomar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder público para la representación visual de cada medicamento.
     */
    public static class MedViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre;
        final TextView tvBadge;
        final Button btnTomar;

        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvMedName);
            tvBadge = itemView.findViewById(R.id.tvMedBadge);
            btnTomar = itemView.findViewById(R.id.btnTakeMed);
        }
    }
}
