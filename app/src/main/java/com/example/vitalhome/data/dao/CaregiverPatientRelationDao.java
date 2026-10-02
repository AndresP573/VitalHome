package com.example.vitalhome.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.vitalhome.data.local.CaregiverPatientRelationEntity;

import java.util.List;

@Dao
public interface CaregiverPatientRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CaregiverPatientRelationEntity relation);

    @Query("SELECT * FROM vinculos_cuidado WHERE cuidadorId = :cuidadorId")
    List<CaregiverPatientRelationEntity> getPatientsForCaregiver(int cuidadorId);
}
