package com.example.vitalhome.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.vitalhome.data.local.MedicalRecordEntity;

import java.util.List;

@Dao
public interface MedicalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MedicalRecordEntity record);

    @Query("SELECT * FROM historial_medico WHERE pacienteId = :pacienteId ORDER BY id DESC")
    List<MedicalRecordEntity> getRecordsByPatient(int pacienteId);
}
