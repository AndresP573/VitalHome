package com.example.vitalhome.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.modelo.AlertEvent;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<AlertEvent> lista;

    public AlertAdapter(List<AlertEvent> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int pos) {
        AlertEvent ev = lista.get(pos);
        
        holder.title.setText(ev.getTipo());
        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryDark));
        
        holder.time.setText(ev.getHora());
        holder.time.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        
        String txt = (ev.getData() != null ? ev.getData() : "") + 
                     (ev.getLoc() != null ? " 📍 " + ev.getLoc() : "");
        holder.body.setText(txt);
        holder.body.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_main));

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
        
        holder.card.setStrokeWidth(4);
        holder.card.setStrokeColor(col);
        holder.card.setCardBackgroundColor(Color.WHITE);
    }

    @Override
    public int getItemCount() { return lista.size(); }

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