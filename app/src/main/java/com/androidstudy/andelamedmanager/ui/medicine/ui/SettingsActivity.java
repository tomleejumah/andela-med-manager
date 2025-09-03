package com.androidstudy.andelamedmanager.ui.medicine.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.drive.DriveBackupWorker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;
//todo handle the other settings actions like logout and dark mode theme

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences prefs, authPrefs;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch backUpSwitch;

    private GoogleSignInClient googleSignInClient;

    // Activity Result API launcher
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backUpSwitch = findViewById(R.id.backUpSwitch);

        prefs = getSharedPreferences("backup_prefs", MODE_PRIVATE);
        authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        backUpSwitch.setChecked(prefs.getBoolean("auto_backup", false));

        // Init Google Sign-In with Drive scope
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // Register ActivityResultLauncher
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                        if (account != null) {
                            Timber.d("Drive scope granted.");
                            authPrefs.edit().putBoolean("drive_scope_granted", true).apply();
                            scheduleBackup(this);
                        }
                    } else {
                        Timber.e("User denied Drive permissions.");
                        backUpSwitch.setChecked(false);
                        prefs.edit().putBoolean("auto_backup", false).apply();
                    }
                }
        );

        backUpSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_backup", isChecked).apply();
            if (isChecked) {
                requestDrivePermissions();
            } else {
                WorkManager.getInstance(this).cancelUniqueWork("periodic_backup");
                Timber.d("Backup cancelled");
            }
        });

        checkAndHandleAuthentication();
    }

    private void requestDrivePermissions() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null && GoogleSignIn.hasPermissions(account, new Scope(DriveScopes.DRIVE_APPDATA))) {
            Timber.d("Already signed in with Drive scope.");
            scheduleBackup(this);
        } else {
            Timber.d("Requesting Drive scope via Google Sign-In.");
            signInLauncher.launch(googleSignInClient.getSignInIntent());
        }
    }

    private void checkAndHandleAuthentication() {
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("drive_backup")
                .observe(this, workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo workInfo = workInfos.get(0);
                        if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Timber.e("Backup failed, checking for auth issues");
                            requestDrivePermissions();
                        } else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Timber.d("Backup completed successfully");
                        }
                    }
                });
    }

    private void scheduleBackup(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
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

        Timber.d("Backup scheduled");
    }
}
