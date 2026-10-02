package com.example.vitalhome.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.MedicalRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para presentar los registros del historial médico en un RecyclerView.
 */
public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.RecordViewHolder> {

    // --- Lista inmutable de registros médicos ---
    private final List<MedicalRecord> items;

    /**
     * Constructor del adaptador de registros médicos.
     *
     * @param items Lista de registros a mostrar.
     */
    public MedicalRecordAdapter(List<MedicalRecord> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        MedicalRecord record = items.get(position);
        holder.tvFecha.setText(record.getFecha());
        holder.tvTitulo.setText(record.getTitulo());
        holder.tvDoctor.setText(record.getDoctor());
        holder.tvDetalle.setText(record.getDetalle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder para el ítem del historial clínico.
     */
    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        final TextView tvFecha;
        final TextView tvTitulo;
        final TextView tvDoctor;
        final TextView tvDetalle;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvRecordDate);
            tvTitulo = itemView.findViewById(R.id.tvRecordTitle);
            tvDoctor = itemView.findViewById(R.id.tvRecordDoctor);
            tvDetalle = itemView.findViewById(R.id.tvRecordDetail);
        }
    }
}
