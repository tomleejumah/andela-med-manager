package com.androidstudy.andelamedmanager.ui.auth.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.androidstudy.andelamedmanager.data.AppDatabase;
import com.androidstudy.andelamedmanager.data.model.User;

public class AddUserViewModel extends AndroidViewModel {
    private AppDatabase appDatabase;

    public AddUserViewModel(@NonNull Application application) {
        super(application);

        appDatabase = AppDatabase.getDatabase(application.getApplicationContext());

    }

    public void addUser(final User user) {
        new AddUserViewModel.addAsyncTask(appDatabase).execute(user);
    }

    private static class addAsyncTask extends AsyncTask<User, Void, Void> {

        private AppDatabase db;

        addAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final User... params) {
            db.userDao().insertData(params[0]);
            return null;
        }

    }
}
