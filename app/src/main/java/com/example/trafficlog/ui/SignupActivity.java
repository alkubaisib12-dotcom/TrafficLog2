package com.example.trafficlog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trafficlog.R;
import com.example.trafficlog.data.AppDatabase;
import com.example.trafficlog.data.UserDao;
import com.example.trafficlog.model.User;
import com.example.trafficlog.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignup();
            }
        });
    }

    private void handleSignup() {
        final String username = etUsername.getText() != null
                ? etUsername.getText().toString().trim()
                : "";
        final String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim()
                : "";
        final String confirmPassword = etConfirmPassword.getText() != null
                ? etConfirmPassword.getText().toString().trim()
                : "";

        boolean hasError = false;

        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            hasError = true;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            hasError = true;
        } else {
            tilUsername.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            hasError = true;
        } else if (password.length() < 4) {
            tilPassword.setError("Password must be at least 4 characters");
            hasError = true;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Please confirm your password");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            hasError = true;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (hasError) {
            return;
        }

        // Room DB operations must be off the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserDao userDao = db.userDao();

                // Check if username already exists
                User existing = userDao.getUserByUsername(username);
                if (existing != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tilUsername.setError("Username already exists");
                            Toast.makeText(SignupActivity.this,
                                    "Choose a different username", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                // Create new user
                User user = new User();
                user.username = username;
                user.password = password; // For the assignment; in real apps use hashing.

                long newId = userDao.insertUser(user);
                final int userId = (int) newId;

                // Save session
                SessionManager.saveLoggedInUser(getApplicationContext(), userId, username);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignupActivity.this,
                                "Account created successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).start();
    }
}