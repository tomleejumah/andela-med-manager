package com.androidstudy.andelamedmanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.androidstudy.andelamedmanager.data.dao.MedicineDao;
import com.androidstudy.andelamedmanager.data.dao.UserDao;
import com.androidstudy.andelamedmanager.data.model.Medicine;
import com.androidstudy.andelamedmanager.data.model.User;

@Database(entities = {Medicine.class, User.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "medmanager_db")
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public abstract MedicineDao medicineDao();

    public abstract UserDao userDao();
}