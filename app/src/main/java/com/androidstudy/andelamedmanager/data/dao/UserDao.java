package com.androidstudy.andelamedmanager.data.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.androidstudy.andelamedmanager.data.model.User;

@Dao
public abstract class UserDao implements BaseDao<User> {

    @Query("SELECT * FROM User WHERE id = :id")
    public abstract User getUserById(String id);

    @Query("DELETE FROM User")
    public abstract void deleteALl();
}
