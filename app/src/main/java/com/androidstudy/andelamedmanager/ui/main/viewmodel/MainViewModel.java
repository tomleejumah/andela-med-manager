package com.androidstudy.andelamedmanager.ui.main.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.androidstudy.andelamedmanager.data.AppDatabase;
import com.androidstudy.andelamedmanager.data.dao.MedicineDao;
import com.androidstudy.andelamedmanager.data.dao.UserDao;
import com.androidstudy.andelamedmanager.data.model.User;

public class MainViewModel extends AndroidViewModel {

    private final LiveData<User> userLiveData;
    private final AppDatabase appDatabase;
    private final UserDao mUserDao;
    private final MedicineDao mMedDao;

    public MainViewModel(@NonNull Application application) {
        super(application);
        appDatabase = AppDatabase.getDatabase(application.getApplicationContext());
        mUserDao = appDatabase.userDao();
        mMedDao = appDatabase.medicineDao();
        userLiveData = mUserDao.getUserById("1");
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void deleteAll(){
        mMedDao.deleteAll();
        mUserDao.deleteALl();
    }
}
