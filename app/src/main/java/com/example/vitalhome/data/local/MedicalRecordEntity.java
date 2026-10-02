package com.example.vitalhome.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "historial_medico")
public class MedicalRecordEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int pacienteId;
    public String titulo;
    public String doctor;
    public String fecha;
    public String detalle;
}
