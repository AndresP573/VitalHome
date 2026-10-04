package com.example.vitalhome.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.vitalhome.data.local.VitalSignsEntity;

import java.util.List;

@Dao
public interface VitalSignsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(VitalSignsEntity vitalSigns);

    @Query("SELECT * FROM signos_vitales WHERE pacienteId = :pacienteId ORDER BY timestamp DESC")
    List<VitalSignsEntity> getVitalSignsByPatient(int pacienteId);
}
