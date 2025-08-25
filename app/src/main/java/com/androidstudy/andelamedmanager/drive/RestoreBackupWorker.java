package com.androidstudy.andelamedmanager.drive;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;

import timber.log.Timber;
public class RestoreBackupWorker extends Worker {

    public RestoreBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
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

            boolean dbRestoreSuccess = restoreDatabase(driveService);
            boolean prefsRestoreSuccess = restoreSharedPreferences(driveService);

            if (dbRestoreSuccess && prefsRestoreSuccess) {
                Timber.tag("RestoreWorker").d("✅ Complete restore successful!");
                return Result.success();
            } else {
                return Result.failure();
            }

        } catch (Exception e) {
            Timber.tag("RestoreWorker").e(e, "❌ Restore failed");
            return Result.failure();
        }
    }

    private Drive setupDriveService() throws Exception {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) return null;

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Collections.singletonList(DriveScopes.DRIVE_FILE)
        );
        credential.setSelectedAccount(account.getAccount());

        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("Andela-Med-Manager").build();
    }

    private boolean restoreDatabase(Drive driveService) {
        return true;
    }

    private boolean restoreSharedPreferences(Drive driveService) {
        return true;
    }
}
