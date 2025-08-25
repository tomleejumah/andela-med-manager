package com.androidstudy.andelamedmanager.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class AppLifecycleObserver implements LifecycleObserver {
    private Context context;

    public AppLifecycleObserver(Context context) {
        this.context = context;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        Timber.tag("AppLifecycleObserver").d("App in background");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("auto_backup", false)) {
//            enqueueBackupWork(context);
            enqueuePeriodicBackup(context);
        }
    }

    private void enqueueBackupWork(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        OneTimeWorkRequest backupWork =
                new OneTimeWorkRequest.Builder(DriveBackupWorker.class)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(context).enqueue(backupWork);
    }

    private void enqueuePeriodicBackup(Context context) {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        PeriodicWorkRequest periodicBackup = new PeriodicWorkRequest.Builder(
                DriveBackupWorker.class,
                1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "periodic_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicBackup);
    }
}
