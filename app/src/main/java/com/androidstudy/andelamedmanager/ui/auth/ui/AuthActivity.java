package com.androidstudy.andelamedmanager.ui.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.androidstudy.andelamedmanager.R;
import com.androidstudy.andelamedmanager.data.model.User;
import com.androidstudy.andelamedmanager.settings.Settings;
import com.androidstudy.andelamedmanager.ui.auth.viewmodel.AddUserViewModel;
import com.androidstudy.andelamedmanager.ui.main.ui.MainActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import timber.log.Timber;

public class AuthActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 121;

    GoogleApiClient mGoogleApiClient;
    private AddUserViewModel addUserViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_auth);

        addUserViewModel = ViewModelProviders.of(this).get(AddUserViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In Api and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivitgit y */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.loginButton).setOnClickListener(v -> {
            //Check Internet Connection
            //  if (NetworkUtil.isConnected(this))
            googleSignIn();

        });
    }

    private void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

// Launch sign-in intent
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSignInResult(account);
            } catch (ApiException e) {
                Log.d("SignIn", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }


    private void handleSignInResult(GoogleSignInAccount acct) {
        if (acct != null) {
            Timber.d("handleSignInResult: success for " + acct.getEmail());

            String name = acct.getDisplayName();
            String imageUrl = acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : null;

            // Save to Room DB
            addUserViewModel.addUser(new User(
                    "1",
                    name,
                    imageUrl
            ));

            // Set the Logged in status to true
            Settings.setLoggedInSharedPref(true);

            // Navigate user to MainActivity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Timber.w("handleSignInResult: account is null");
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Settings.isLoggedIn()) {
            Intent auth = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(auth);
            finish();
        }
    }
}
