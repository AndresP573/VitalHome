package com.example.vitalhome.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.AlertEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar el historial cronológico de alertas y eventos en un RecyclerView.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    // --- Atributos de la lista de eventos ---
    private List<AlertEvent> items;

    /**
     * Constructor del adaptador.
     *
     * @param items Lista inicial de eventos de alerta a presentar.
     */
    public AlertAdapter(List<AlertEvent> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    /**
     * Actualiza la lista de elementos mostrados en el RecyclerView.
     *
     * @param newItems Lista actualizada de eventos.
     */
    public void updateList(List<AlertEvent> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertEvent ev = items.get(position);
        holder.tvTitle.setText(ev.getTipo());
        holder.tvTime.setText(ev.getHora());
        holder.tvBody.setText(ev.getData());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder interno para los elementos del RecyclerView de alertas.
     */
    public static class AlertViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvTime;
        final TextView tvBody;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
            tvBody = itemView.findViewById(R.id.tvAlertBody);
        }
    }
}
