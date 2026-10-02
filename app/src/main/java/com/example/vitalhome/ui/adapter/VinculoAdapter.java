package com.example.vitalhome.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Adaptador de RecyclerView para seleccionar interactivamente el vínculo con el adulto mayor en el Registro.
 */
public class VinculoAdapter extends RecyclerView.Adapter<VinculoAdapter.VinculoViewHolder> {

    public interface OnVinculoSelectedListener {
        void onVinculoSelected(String vinculo, boolean isOtro);
    }

    public static class VinculoItem {
        public final String title;

        public VinculoItem(String title) {
            this.title = title;
        }
    }

    private final VinculoItem[] items;
    private int selectedPosition = 0;
    private final OnVinculoSelectedListener listener;

    public VinculoAdapter(VinculoItem[] items, OnVinculoSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VinculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vinculo_card, parent, false);
        return new VinculoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VinculoViewHolder holder, int position) {
        VinculoItem item = items[position];
        holder.tvTitle.setText(item.title);

        boolean isSelected = (position == selectedPosition);
        if (isSelected) {
            holder.cardView.setStrokeColor(Color.parseColor("#1457D9"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#EBF3FF"));
        } else {
            holder.cardView.setStrokeColor(Color.parseColor("#D8E3F5"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        View.OnClickListener clickListener = v -> {
            int prevPos = selectedPosition;
            int newPos = holder.getBindingAdapterPosition();
            if (newPos != RecyclerView.NO_POSITION) {
                selectedPosition = newPos;
                notifyItemChanged(prevPos);
                notifyItemChanged(selectedPosition);

                if (listener != null && selectedPosition >= 0 && selectedPosition < items.length) {
                    boolean isOtro = (selectedPosition == items.length - 1) || "Otro...".equals(items[selectedPosition].title);
                    listener.onVinculoSelected(items[selectedPosition].title, isOtro);
                }
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.cardView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.length : 0;
    }

    public String getSelectedVinculo() {
        if (items != null && selectedPosition >= 0 && selectedPosition < items.length) {
            return items[selectedPosition].title;
        }
        return "Cuidador / Cuidadora";
    }

    public boolean isSelectedOtro() {
        return items != null && selectedPosition == (items.length - 1);
    }

    public static class VinculoViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView tvTitle;

        public VinculoViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardVinculo);
            tvTitle = itemView.findViewById(R.id.tvVinculoTitle);
        }
    }
}
