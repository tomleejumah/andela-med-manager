package com.androidstudy.andelamedmanager.ui.medicine.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.androidstudy.andelamedmanager.data.AppDatabase;
import com.androidstudy.andelamedmanager.data.model.Medicine;

import java.util.List;

public class MedicineViewModel extends AndroidViewModel {

    private final LiveData<List<Medicine>> medicineList;
    private AppDatabase appDatabase;

    public MedicineViewModel(@NonNull Application application) {
        super(application);
        appDatabase = AppDatabase.getDatabase(application.getApplicationContext());
        medicineList = appDatabase.medicineDao().getAllMedicine();
    }

    public LiveData<List<Medicine>> getMedicineList() {
        return medicineList;
    }

    public void deleteItem(Medicine medicine) {
        new deleteAsyncTask(appDatabase).execute(medicine);
    }

    private static class deleteAsyncTask extends AsyncTask<Medicine, Void, Void> {

        private AppDatabase db;

        deleteAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final Medicine... params) {
            db.medicineDao().deleteData(params[0]);
            return null;
        }

    }
}
