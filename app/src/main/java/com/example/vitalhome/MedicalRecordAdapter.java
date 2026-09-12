package com.example.vitalhome;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.RecordViewHolder> {

    private List<MedicalRecord> list;

    public MedicalRecordAdapter(List<MedicalRecord> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        MedicalRecord r = list.get(position);
        holder.title.setText(r.getTitulo());
        holder.date.setText(r.getFecha());
        holder.doctor.setText(r.getDoctor());
        holder.detail.setText(r.getDetalle());
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, doctor, detail;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvRecordTitle);
            date = itemView.findViewById(R.id.tvRecordDate);
            doctor = itemView.findViewById(R.id.tvRecordDoctor);
            detail = itemView.findViewById(R.id.tvRecordDetail);
        }
    }
}