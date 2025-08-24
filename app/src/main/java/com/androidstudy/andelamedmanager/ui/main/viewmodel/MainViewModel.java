package com.androidstudy.andelamedmanager.ui.main.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.androidstudy.andelamedmanager.data.AppDatabase;
import com.androidstudy.andelamedmanager.data.dao.MedicineDao;
import com.androidstudy.andelamedmanager.data.dao.UserDao;
import com.androidstudy.andelamedmanager.data.model.User;

public class MainViewModel extends AndroidViewModel {

    private User userLiveData;
    private AppDatabase appDatabase;
    private UserDao mUserDao;
    private MedicineDao mMedDao;

    public MainViewModel(@NonNull Application application) {
        super(application);
        appDatabase = AppDatabase.getDatabase(application.getApplicationContext());
        mUserDao = appDatabase.userDao();
        mMedDao = appDatabase.medicineDao();
        userLiveData = appDatabase.userDao().getUserById("1");
    }

    public User getUserLiveData() {
        return userLiveData;
    }

    public void deleteAll(){
        mMedDao.deleteAll();
        mUserDao.deleteALl();
    }
}
