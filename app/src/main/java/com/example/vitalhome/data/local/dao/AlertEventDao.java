package com.example.vitalhome.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.vitalhome.data.local.AlertEventEntity;

import java.util.List;

@Dao
public interface AlertEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlertEventEntity alert);

    @Query("SELECT * FROM eventos_alertas WHERE pacienteId = :pacienteId ORDER BY timestamp DESC")
    List<AlertEventEntity> getAlertsByPatient(int pacienteId);
}
