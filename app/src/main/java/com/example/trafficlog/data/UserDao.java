package com.example.trafficlog.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.trafficlog.model.User;

@Dao
public interface UserDao {

    @Insert
    long insertUser(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User getUserByUsernameAndPassword(String username, String password);
}
