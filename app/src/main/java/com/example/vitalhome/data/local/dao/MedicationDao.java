package com.example.vitalhome.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.vitalhome.data.local.MedicationEntity;

import java.util.List;

@Dao
public interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MedicationEntity medication);

    @Update
    void update(MedicationEntity medication);

    @Query("SELECT * FROM medicamentos WHERE pacienteId = :pacienteId AND activo = 1")
    List<MedicationEntity> getMedicationsByPatient(int pacienteId);
}
