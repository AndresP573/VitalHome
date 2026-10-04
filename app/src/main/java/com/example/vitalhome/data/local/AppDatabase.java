package com.example.vitalhome.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.vitalhome.data.local.dao.AlertEventDao;
import com.example.vitalhome.data.local.dao.CaregiverPatientRelationDao;
import com.example.vitalhome.data.local.dao.MedicalRecordDao;
import com.example.vitalhome.data.local.dao.MedicationDao;
import com.example.vitalhome.data.local.dao.UserDao;
import com.example.vitalhome.data.local.dao.VitalSignsDao;

@Database(
    entities = {
        UserEntity.class,
        CaregiverPatientRelationEntity.class,
        MedicationEntity.class,
        VitalSignsEntity.class,
        AlertEventEntity.class,
        MedicalRecordEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract CaregiverPatientRelationDao caregiverPatientRelationDao();
    public abstract MedicationDao medicationDao();
    public abstract VitalSignsDao vitalSignsDao();
    public abstract AlertEventDao alertEventDao();
    public abstract MedicalRecordDao medicalRecordDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "vitalhome_db"
                    ).fallbackToDestructiveMigration()
                     .build();
                }
            }
        }
        return INSTANCE;
    }
}
