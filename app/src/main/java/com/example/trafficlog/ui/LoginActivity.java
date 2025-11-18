package com.example.trafficlog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trafficlog.R;
import com.example.trafficlog.data.AppDatabase;
import com.example.trafficlog.data.UserDao;
import com.example.trafficlog.model.User;
import com.example.trafficlog.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnSignIn;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignInLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    private void handleLogin() {
        final String username = etUsername.getText() != null
                ? etUsername.getText().toString().trim()
                : "";
        final String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim()
                : "";

        boolean hasError = false;

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            hasError = true;
        } else {
            tilUsername.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            hasError = true;
        } else {
            tilPassword.setError(null);
        }

        if (hasError) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserDao userDao = db.userDao();

                final User user = userDao.getUserByUsernameAndPassword(username, password);

                if (user == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tilPassword.setError("Invalid username or password");
                            Toast.makeText(LoginActivity.this,
                                    "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Save session
                    SessionManager.saveLoggedInUser(getApplicationContext(), user.id, user.username);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tilPassword.setError(null);
                            Toast.makeText(LoginActivity.this,
                                    "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        }).start();
    }

    private void showForgotPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("This is a local-only app, so passwords cannot be recovered remotely.\n\n" +
                        "If you've forgotten your password, you'll need to create a new account.\n\n" +
                        "In a production app with a backend server, you would receive a password reset link via email.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Create New Account", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        // Navigate to Sign up screen
                        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
    }
}
