package com.androidstudy.andelamedmanager.data.dao;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Delete;
import androidx.room.Insert;

/**
 * This is a base DAO that will hold common SQL Queries mapped to methods.
 * @param <T> The data type of the entity you want to work with.
 */
public interface BaseDao<T> {

    @Insert(onConflict = REPLACE)
    void insertData(T data);

    @Delete
    void deleteData(T data);
}
