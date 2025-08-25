package com.androidstudy.andelamedmanager.drive;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import timber.log.Timber;

public class DriveBackupWorker extends Worker {

    private static final String BACKUP_FOLDER = "MedManager_Backups";

    public DriveBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Drive driveService = setupDriveService();
            if (driveService == null) {
                return Result.failure();
            }

            String timestamp = String.valueOf(System.currentTimeMillis());

            // 1. Backup Room Database
            boolean dbBackupSuccess = backupDatabase(driveService, timestamp);

            // 2. Backup SharedPreferences
            boolean prefsBackupSuccess = backupSharedPreferences(driveService, timestamp);

            if (dbBackupSuccess && prefsBackupSuccess) {
                Timber.tag("BackupWorker").d("Complete backup successful!");
                return Result.success();
            } else {
                Timber.tag("BackupWorker").e("Partial backup failure");
                return Result.retry();
            }

        } catch (UserRecoverableAuthIOException e) {
            Timber.tag("BackupWorker").e(e, "Authentication required");
            return Result.failure();
        } catch (Exception e) {
            Timber.tag("BackupWorker").e(e, "❌ Backup failed");
            return Result.retry();
        }
    }

    private Drive setupDriveService() throws Exception {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) {
            Timber.tag("BackupWorker").e("No signed-in account found");
            return null;
        }

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Collections.singletonList(DriveScopes.DRIVE_APPDATA)
        );
        credential.setSelectedAccount(account.getAccount());

        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Andela-Med-Manager").build();
    }

    private boolean backupDatabase(Drive driveService, String timestamp) {
        try {
            // Get the database file
            File dbFile = getApplicationContext().getDatabasePath("medmanager_db");
            if (!dbFile.exists()) {
                Timber.tag("BackupWorker").e("Database file not found");
                return false;
            }

            // Creating Drive file metadata
            com.google.api.services.drive.model.File fileMetadata =
                    new com.google.api.services.drive.model.File();
            fileMetadata.setName("medmanager_db_" + timestamp + ".db");
            fileMetadata.setParents(Collections.singletonList("appDataFolder"));

            // Uploading the database
            FileContent mediaContent = new FileContent("application/x-sqlite3", dbFile);
            com.google.api.services.drive.model.File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id,name,size")
                    .execute();

            Timber.tag("BackupWorker").d("📊 Database backup uploaded: %s (%s bytes)",
                    uploadedFile.getName(), uploadedFile.getSize());
            return true;

        } catch (Exception e) {
            Timber.tag("BackupWorker").e(e, "Database backup failed");
            return false;
        }
    }

    private boolean backupSharedPreferences(Drive driveService, String timestamp) {
        try {
            // Getting all SharedPreferences
            String[] prefFiles = {
                    "backup_prefs"
            };

            JSONObject allPrefs = new JSONObject();

            for (String prefName : prefFiles) {
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences(prefName, Context.MODE_PRIVATE);

                JSONObject prefData = new JSONObject();
                Map<String, ?> allEntries = prefs.getAll();

                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Object value = entry.getValue();
                    prefData.put(entry.getKey(), value);
                }

                if (prefData.length() > 0) {
                    allPrefs.put(prefName, prefData);
                }
            }

            if (allPrefs.length() == 0) {
                Timber.tag("BackupWorker").d("No SharedPreferences to backup");
                return true;
            }

            // Create Drive file for preferences
            com.google.api.services.drive.model.File fileMetadata =
                    new com.google.api.services.drive.model.File();
            fileMetadata.setName("medmanager_prefs_" + timestamp + ".json");
            fileMetadata.setParents(Collections.singletonList("appDataFolder"));

            // Uploading preferences as JSON
            byte[] jsonBytes = allPrefs.toString(2).getBytes("UTF-8");
            ByteArrayContent mediaContent = new ByteArrayContent("application/json", jsonBytes);

            com.google.api.services.drive.model.File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id,name,size")
                    .execute();

            Timber.tag("BackupWorker").d("Preferences backup uploaded: %s (%s bytes)",
                    uploadedFile.getName(), uploadedFile.getSize());
            return true;

        } catch (Exception e) {
            Timber.tag("BackupWorker").e(e, "Preferences backup failed");
            return false;
        }
    }
}