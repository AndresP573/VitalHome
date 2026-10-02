package com.example.vitalhome.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "signos_vitales")
public class VitalSignsEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int pacienteId;
    public int pulso;
    public int oxigeno;
    public int presionSistolica;
    public int presionDiastolica;
    public long timestamp;
}
