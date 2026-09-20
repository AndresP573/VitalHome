package com.example.vitalhome.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vitalhome.R;
import com.example.vitalhome.data.modelo.MedicalRecord;
import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.RecordViewHolder> {

    private List<MedicalRecord> items;

    public MedicalRecordAdapter(List<MedicalRecord> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int pos) {
        MedicalRecord r = items.get(pos);
        holder.t.setText(r.getTitulo());
        holder.f.setText(r.getFecha());
        holder.d.setText(r.getDoctor());
        holder.det.setText(r.getDetalle());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView t, f, d, det;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            t = itemView.findViewById(R.id.tvRecordTitle);
            f = itemView.findViewById(R.id.tvRecordDate);
            d = itemView.findViewById(R.id.tvRecordDoctor);
            det = itemView.findViewById(R.id.tvRecordDetail);
        }
    }
}