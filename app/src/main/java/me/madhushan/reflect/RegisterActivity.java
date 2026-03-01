package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private MaterialButton btnRegister;
    private TextView tvLogin, tvTermsLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
            // Go back to LoginActivity
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
        String fullName       = text(etFullName);
        String email          = text(etEmail);
        String password       = text(etPassword);
        String confirmPassword = text(etConfirmPassword);

        boolean valid = true;

        // Full name validation
        if (fullName.isEmpty()) {
            tilFullName.setError("Full name is required");
            valid = false;
        } else {
            tilFullName.setError(null);
        }

        // Email validation
        if (email.isEmpty()) {
            tilEmail.setError("Email address is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        // Password validation
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            tilPassword.setError(null);
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password");
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        // Terms validation
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (valid) {
            // TODO: Integrate registration backend here
            Toast.makeText(this, "Account created! Welcome to Reflect 🎉", Toast.LENGTH_LONG).show();
        }
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}

