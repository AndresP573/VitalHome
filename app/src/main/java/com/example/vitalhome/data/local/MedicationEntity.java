package com.example.vitalhome.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicamentos")
public class MedicationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int pacienteId;
    public String nombre;
    public String dosis;
    public String horario;
    public boolean tomado;
    public boolean activo;
}
