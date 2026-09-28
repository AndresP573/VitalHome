package com.example.vitalhome.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.AlertEvent;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/**
 * Adaptador de RecyclerView para mostrar la lista de alertas (AlertEvent) en la interfaz.
 * Gestiona el inflado de vistas y la vinculación de datos de cada alerta con su representación visual.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    // --- Bloque 1: Atributos del adaptador ---
    private List<AlertEvent> lista;

    // --- Bloque 2: Constructor ---
    public AlertAdapter(List<AlertEvent> lista) {
        this.lista = lista;
    }

    // --- Bloque 3: Creación de ViewHolder ---
    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(v);
    }

    // --- Bloque 4: Vinculación de datos con las vistas ---
    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int pos) {
        AlertEvent ev = lista.get(pos);
        
        // Configuración de título y texto de hora
        holder.title.setText(ev.getTipo());
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryDark));
        
        holder.time.setText(ev.getHora());
        holder.time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        
        // Construcción del texto descriptivo con ubicación
        String txt = (ev.getData() != null ? ev.getData() : "") + 
                     (ev.getLoc() != null ? " 📍 " + ev.getLoc() : "");
        holder.body.setText(txt);
        holder.body.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_main));

        // Determinar el color del borde según el tipo de alerta
        int col;
        if ("SOS".equals(ev.getTipo())) {
            col = Color.RED;
        } else if ("SALUD".equals(ev.getTipo())) {
            col = Color.parseColor("#EF4444");
        } else if ("MEDICINA".equals(ev.getTipo())) {
            col = Color.parseColor("#10B981");
        } else {
            col = Color.parseColor("#2F7CF6");
        }
        
        // Aplicación del estilo a la tarjeta
        holder.card.setStrokeWidth(4);
        holder.card.setStrokeColor(col);
        holder.card.setCardBackgroundColor(Color.WHITE);
    }

    // --- Bloque 5: Conteo de elementos y actualización de lista ---
    @Override
    public int getItemCount() { 
        return lista != null ? lista.size() : 0; 
    }

    public void updateList(List<AlertEvent> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    // --- Bloque 6: Clase interna ViewHolder ---
    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, time;
        MaterialCardView card;
        
        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvAlertTitle);
            body = itemView.findViewById(R.id.tvAlertBody);
            time = itemView.findViewById(R.id.tvAlertTime);
            card = itemView.findViewById(R.id.cardAlert);
        }
    }
}
