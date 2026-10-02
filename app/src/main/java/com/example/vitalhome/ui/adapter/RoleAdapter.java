package com.example.vitalhome.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Adaptador de RecyclerView para seleccionar interactivamente el Rol (Adulto Mayor o Cuidador) en el Registro.
 */
public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.RoleViewHolder> {

    public interface OnRoleSelectedListener {
        void onRoleSelected(boolean isAdulto);
    }

    public static class RoleItem {
        public final String title;
        public final String sub;
        public final boolean isAdulto;

        public RoleItem(String title, String sub, boolean isAdulto) {
            this.title = title;
            this.sub = sub;
            this.isAdulto = isAdulto;
        }
    }

    private final RoleItem[] items;
    private int selectedPosition = 0; // 0 = Adulto Mayor por defecto
    private final OnRoleSelectedListener listener;

    public RoleAdapter(RoleItem[] items, OnRoleSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_role_card, parent, false);
        return new RoleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, int position) {
        RoleItem item = items[position];
        holder.tvTitle.setText(item.title);
        holder.tvSub.setText(item.sub);

        boolean isSelected = (position == selectedPosition);
        holder.rbCheck.setChecked(isSelected);

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
                    listener.onRoleSelected(items[selectedPosition].isAdulto);
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

    public boolean isSelectedAdulto() {
        return items != null && selectedPosition >= 0 && selectedPosition < items.length && items[selectedPosition].isAdulto;
    }

    public static class RoleViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final TextView tvTitle;
        final TextView tvSub;
        final RadioButton rbCheck;

        public RoleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardRole);
            tvTitle = itemView.findViewById(R.id.tvRoleTitle);
            tvSub = itemView.findViewById(R.id.tvRoleSub);
            rbCheck = itemView.findViewById(R.id.rbRoleCheck);
        }
    }
}
