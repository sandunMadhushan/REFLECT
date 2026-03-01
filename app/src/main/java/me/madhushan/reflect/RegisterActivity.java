package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.User;
import me.madhushan.reflect.database.UserDao;
import me.madhushan.reflect.utils.PasswordUtils;
import me.madhushan.reflect.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;
    private TextView tvLogin, tvTermsLink;

    private AppDatabase db;
    private SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db             = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tilFullName        = findViewById(R.id.til_full_name);
        tilEmail           = findViewById(R.id.til_email);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etFullName         = findViewById(R.id.et_full_name);
        etEmail            = findViewById(R.id.et_email);
        etPassword         = findViewById(R.id.et_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        cbTerms            = findViewById(R.id.cb_terms);
        btnRegister        = findViewById(R.id.btn_register);
        tvLogin            = findViewById(R.id.tv_login);
        tvTermsLink        = findViewById(R.id.tv_terms_link);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        tvTermsLink.setOnClickListener(v ->
                Toast.makeText(this, "Terms & Conditions — coming soon", Toast.LENGTH_SHORT).show());
    }

    private void handleRegister() {
        String fullName        = text(etFullName);
        String email           = text(etEmail);
        String password        = text(etPassword);
        String confirmPassword = text(etConfirmPassword);

        // ── Validation ──────────────────────────────────────────────────────
        boolean valid = true;

        if (fullName.isEmpty()) {
            tilFullName.setError("Full name is required");
            valid = false;
        } else {
            tilFullName.setError(null);
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email address is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            tilPassword.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password");
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) return;

        // ── Disable button while processing ────────────────────────────────
        btnRegister.setEnabled(false);

        final String finalFullName = fullName;
        final String finalEmail    = email;
        final String finalPassword = password;

        // ── Room DB insert on background thread ─────────────────────────────
        executor.execute(() -> {
            UserDao dao = db.userDao();

            // Check if email already registered
            if (dao.emailExists(finalEmail) > 0) {
                runOnUiThread(() -> {
                    tilEmail.setError("This email is already registered");
                    btnRegister.setEnabled(true);
                });
                return;
            }

            // Build and insert new user
            User newUser       = new User();
            newUser.fullName   = finalFullName;
            newUser.email      = finalEmail;
            newUser.passwordHash = PasswordUtils.hash(finalPassword);

            long newId = dao.insertUser(newUser);

            runOnUiThread(() -> {
                btnRegister.setEnabled(true);
                if (newId > 0) {
                    // Auto-login: save session and go to MainActivity
                    sessionManager.saveSession((int) newId, finalFullName, finalEmail);
                    Toast.makeText(this,
                            "Welcome to Reflect, " + finalFullName + "! 🎉",
                            Toast.LENGTH_LONG).show();
                    goToHome();
                } else {
                    Toast.makeText(this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void goToHome() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
