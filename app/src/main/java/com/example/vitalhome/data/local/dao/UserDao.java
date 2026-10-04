package com.example.vitalhome.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.vitalhome.data.local.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    UserEntity getUserById(int id);

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM usuarios")
    List<UserEntity> getAllUsers();
}
