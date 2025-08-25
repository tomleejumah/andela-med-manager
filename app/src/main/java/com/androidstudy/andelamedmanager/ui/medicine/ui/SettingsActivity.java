package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.drive.DriveBackupWorker;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences prefs;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch backUpSwitch;
    private static final int AUTH_REQUEST_CODE = 1001;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        backUpSwitch = findViewById(R.id.backUpSwitch);

//        backUpSwitch.setChecked(prefs.getBoolean("auto_backup", false));
//
//        backUpSwitch.setOnClickListener(view -> {
//            prefs.edit().putBoolean("auto_backup", backUpSwitch.isChecked()).apply();
//        });

        prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("drive_scope_granted", false).apply(); //todo remove this
        backUpSwitch.setChecked(prefs.getBoolean("auto_backup", false));

        backUpSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_backup", isChecked).apply();
            //todo a logic to prevent auto request each timw
            if (isChecked) {
                requestDrivePermissions();
            } else {
                WorkManager.getInstance(this).cancelUniqueWork("periodic_backup");
                Timber.d("Backup cancelled");
            }
        });

//        // Check for pending authentication from previous backup failures
//        checkAndHandleAuthentication();
    }

    @SuppressLint("StaticFieldLeak")
   private void requestDrivePermissions() {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    if (account == null) {
        Timber.e("No signed-in Google account");
        backUpSwitch.setChecked(false);
        prefs.edit().putBoolean("auto_backup", false).apply();
        return;
    }

    new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        SettingsActivity.this, Collections.singletonList(DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(account.getAccount());
                return credential.getToken();
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), AUTH_REQUEST_CODE);
                return null;
            } catch (Exception e) {
                Timber.e(e, "Failed to get token");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String token) {
            if (token != null) {
                prefs.edit().putBoolean("drive_scope_granted", true).apply();
                scheduleBackup(SettingsActivity.this);
            } else {
                // Revert switch if token retrieval fails (except for consent prompt)
                if (!isConsentScreenLaunched()) {
                    backUpSwitch.setChecked(false);
                    prefs.edit().putBoolean("auto_backup", false).apply();
                }
            }
        }

        private boolean isConsentScreenLaunched() {
            // Check if startActivityForResult was called (simplified check)
            return prefs.getString("auth_intent", null) != null;
        }
    }.execute();
}

    private void scheduleBackup(Context context) {
        // Same as previous scheduleBackup method
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        OneTimeWorkRequest backupRequest = new OneTimeWorkRequest.Builder(DriveBackupWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .setId(UUID.randomUUID())
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork("drive_backup",
                ExistingWorkPolicy.REPLACE, backupRequest);
        Timber.d("Backup scheduled");
    }

    private void checkAndHandleAuthentication() {
        String pendingAuth = prefs.getString("auth_intent", null);
        if (pendingAuth != null) {
            prefs.edit().remove("auth_intent").apply();
            requestDrivePermissions();
        }

        // Monitor backup work status
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("drive_backup")
                .observe(this, workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Timber.e("Backup failed, checking for auth issues");
                            checkAndHandleAuthentication();
                        } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Timber.d("Backup completed successfully");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.d("User granted Drive permissions");
                scheduleBackup(this);
            } else {
                Timber.e("User denied Drive permissions");
                backUpSwitch.setChecked(false); // Revert switch if user denies
                prefs.edit().putBoolean("auto_backup", false).apply();
            }
        }
    }
}