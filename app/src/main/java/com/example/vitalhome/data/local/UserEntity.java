package com.example.vitalhome.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String email;
    public String password;
    public String nombreCompleto;
    public String rol; // 'cliente' o 'cuidador'
    public String telefono;
    public String vinculo;
    public Integer edad;
    public String genero;
    public long createdAt;
}
