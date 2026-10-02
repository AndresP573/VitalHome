package com.example.vitalhome.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "eventos_alertas")
public class AlertEventEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int pacienteId;
    public String tipoAlerta; // 'SOS', 'CAIDA', 'HUMO'
    public String descripcion;
    public long timestamp;
    public boolean resuelto;
}
