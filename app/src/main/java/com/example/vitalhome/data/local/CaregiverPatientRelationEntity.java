package com.example.vitalhome.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vinculos_cuidado")
public class CaregiverPatientRelationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int cuidadorId;
    public int pacienteId;
    public String parentesco;
    public boolean esPrincipal;
}
