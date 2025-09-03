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
import com.google.api.services.drive.model.FileList;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import timber.log.Timber;

public class DriveBackupWorker extends Worker {

    public DriveBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //init Drive service
            Drive driveService = setupDriveService();
            if (driveService == null) {
                return Result.failure();
            }

            // 1. Backup Database
            boolean dbBackupSuccess = backupDatabase(driveService);

            // 2. Backup SharedPreferences
            boolean prefsBackupSuccess = backupSharedPreferences(driveService);

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
            Timber.tag("BackupWorker").e(e, "Backup failed");
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

    private boolean backupDatabase(Drive driveService) {
        try {
            File dbFile = getApplicationContext().getDatabasePath("medmanager_db");
            if (!dbFile.exists()) {
                Timber.tag("BackupWorker").e("Database file not found");
                return false;
            }

            FileContent mediaContent = new FileContent("application/x-sqlite3", dbFile);

            // check if file exists in drive so we update instead f creating-new file everytime
            FileList result = driveService.files().list()
                    .setSpaces("appDataFolder")
                    .setQ("name = 'medmanager_db.db'")
                    .setFields("files(id, name)")
                    .execute();

            if (!result.getFiles().isEmpty()) {
                // updating
                String fileId = result.getFiles().get(0).getId();
                driveService.files().update(fileId, null, mediaContent).execute();
                Timber.tag("BackupWorker").d("Database backup updated");
            } else {
                // creating new
                com.google.api.services.drive.model.File fileMetadata =
                        new com.google.api.services.drive.model.File();
                fileMetadata.setName("medmanager_db.db");
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));

                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id,name,size")
                        .execute();
                Timber.tag("BackupWorker").d("Database backup created");
            }

            return true;

        } catch (Exception e) {
            Timber.tag("BackupWorker").e(e, "Database backup failed");
            return false;
        }
    }

    private boolean backupSharedPreferences(Drive driveService) {
        try {
            String[] prefFiles = {"backup_prefs,andela,loggedin"};
            JSONObject allPrefs = new JSONObject();

            for (String prefName : prefFiles) {
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences(prefName, Context.MODE_PRIVATE);

                JSONObject prefData = new JSONObject();
                Map<String, ?> allEntries = prefs.getAll();

                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    prefData.put(entry.getKey(), entry.getValue());
                }

                if (prefData.length() > 0) {
                    allPrefs.put(prefName, prefData);
                }
            }

            if (allPrefs.length() == 0) {
                Timber.tag("BackupWorker").d("No SharedPreferences to backup");
                return true;
            }

            byte[] jsonBytes = allPrefs.toString(2).getBytes("UTF-8");
            ByteArrayContent mediaContent = new ByteArrayContent("application/json", jsonBytes);

            // check if prefs backup exists
            FileList result = driveService.files().list()
                    .setSpaces("appDataFolder")
                    .setQ("name = 'medmanager_prefs.json'")
                    .setFields("files(id, name)")
                    .execute();

            if (!result.getFiles().isEmpty()) {
                // updating
                String fileId = result.getFiles().get(0).getId();
                driveService.files().update(fileId, null, mediaContent).execute();
                Timber.tag("BackupWorker").d("Preferences backup updated");
            } else {
                // creating new
                com.google.api.services.drive.model.File fileMetadata =
                        new com.google.api.services.drive.model.File();
                fileMetadata.setName("medmanager_prefs.json");
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));

                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id,name,size")
                        .execute();
                Timber.tag("BackupWorker").d("Preferences backup created");
            }

            return true;

        } catch (Exception e) {
            Timber.tag("BackupWorker").e(e, "Preferences backup failed");
            return false;
        }
    }
}
