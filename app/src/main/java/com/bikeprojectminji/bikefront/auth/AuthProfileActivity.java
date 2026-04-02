package com.bikeprojectminji.bikefront.auth;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bikeprojectminji.bikefront.R;

public class AuthProfileActivity extends AppCompatActivity {

    public static final String EXTRA_REASON = "extra_reason";

    private AuthSessionStore authSessionStore;
    private AuthLoginGateway authLoginGateway;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_profile);

        authSessionStore = new AuthSessionStore(this);
        authLoginGateway = new HttpAuthLoginGateway();

        TextView reasonTextView = findViewById(R.id.authReasonTextView);
        TextView statusTextView = findViewById(R.id.authStatusTextView);
        EditText emailEditText = findViewById(R.id.authEmailEditText);
        EditText passwordEditText = findViewById(R.id.authPasswordEditText);
        EditText displayNameEditText = findViewById(R.id.authDisplayNameEditText);
        ProgressBar progressBar = findViewById(R.id.authProgressBar);
        Button registerButton = findViewById(R.id.authContinueButton);
        Button loginButton = findViewById(R.id.authLoginButton);
        Button laterButton = findViewById(R.id.authLaterButton);

        reasonTextView.setText(getIntent().getStringExtra(EXTRA_REASON));
        displayNameEditText.setText(authSessionStore.getDisplayName());

        View.OnClickListener submitListener = v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String displayName = displayNameEditText.getText().toString().trim();

            if (email.isBlank()) {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.auth_profile_email_required_message);
                return;
            }
            if (password.isBlank()) {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.auth_profile_password_required_message);
                return;
            }
            if (v == registerButton && displayName.isBlank()) {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.auth_profile_name_required_message);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(R.string.auth_profile_signing_in_message);
            registerButton.setEnabled(false);
            loginButton.setEnabled(false);
            AuthLoginGateway.Callback callback = new AuthLoginGateway.Callback() {
                @Override
                public void onSuccess(AuthLoginGateway.LoginResult result) {
                    progressBar.setVisibility(View.GONE);
                    authSessionStore.saveSession(result.getDisplayName(), "", result.getAccessToken());
                    setResult(Activity.RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    loginButton.setEnabled(true);
                    statusTextView.setVisibility(View.VISIBLE);
                    statusTextView.setText(message);
                }
            };

            if (v == registerButton) {
                authLoginGateway.register(email, password, displayName, callback);
            } else {
                authLoginGateway.login(email, password, callback);
            }
        };

        registerButton.setOnClickListener(submitListener);
        loginButton.setOnClickListener(submitListener);

        laterButton.setOnClickListener(v -> finish());
    }
}
