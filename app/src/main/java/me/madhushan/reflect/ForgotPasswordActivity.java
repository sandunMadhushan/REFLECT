package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.UserDao;
import me.madhushan.reflect.utils.PasswordUtils;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Step layouts
    private LinearLayout layoutStep1, layoutStep2, layoutSuccess;

    // Step 1
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnFindAccount;

    // Step 2
    private TextView tvFoundEmail;
    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;

    // Step 3
    private MaterialButton btnGoToLogin;

    // Shared
    private TextView tvStep, tvSubtitle, tvBackToLogin;

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Email confirmed in step 1, used in step 2 */
    private String confirmedEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = AppDatabase.getInstance(this);

        initViews();
        setupClickListeners();

        // Handle back press: step 2 → step 1, otherwise go to Login
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutStep2.getVisibility() == View.VISIBLE) {
                    layoutStep1.setVisibility(View.VISIBLE);
                    layoutStep2.setVisibility(View.GONE);
                    tvStep.setText(R.string.forgot_step_email);
                    tvSubtitle.setText(R.string.forgot_password_subtitle);
                    tvStep.setVisibility(View.VISIBLE);
                    tvSubtitle.setVisibility(View.VISIBLE);
                } else {
                    goToLogin();
                }
            }
        });
    }

    private void initViews() {
        layoutStep1     = findViewById(R.id.layout_step1);
        layoutStep2     = findViewById(R.id.layout_step2);
        layoutSuccess   = findViewById(R.id.layout_success);

        tvStep          = findViewById(R.id.tv_step);
        tvSubtitle      = findViewById(R.id.tv_subtitle);
        tvBackToLogin   = findViewById(R.id.tv_back_to_login);

        // Step 1
        tilEmail        = findViewById(R.id.til_email);
        etEmail         = findViewById(R.id.et_email);
        btnFindAccount  = findViewById(R.id.btn_find_account);

        // Step 2
        tvFoundEmail        = findViewById(R.id.tv_found_email);
        tilNewPassword      = findViewById(R.id.til_new_password);
        tilConfirmPassword  = findViewById(R.id.til_confirm_password);
        etNewPassword       = findViewById(R.id.et_new_password);
        etConfirmPassword   = findViewById(R.id.et_confirm_password);
        btnResetPassword    = findViewById(R.id.btn_reset_password);

        // Step 3
        btnGoToLogin = findViewById(R.id.btn_go_to_login);
    }

    private void setupClickListeners() {
        btnFindAccount.setOnClickListener(v -> handleFindAccount());
        btnResetPassword.setOnClickListener(v -> handleResetPassword());
        btnGoToLogin.setOnClickListener(v -> goToLogin());
        tvBackToLogin.setOnClickListener(v -> goToLogin());
    }

    // ── Step 1: Verify email exists ──────────────────────────────────────────

    private void handleFindAccount() {
        tilEmail.setError(null);
        String email = text(etEmail);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            return;
        }

        btnFindAccount.setEnabled(false);
        btnFindAccount.setText(R.string.btn_find_account_loading);

        executor.execute(() -> {
            UserDao dao = db.userDao();
            me.madhushan.reflect.database.User user = dao.findByEmail(email);

            runOnUiThread(() -> {
                btnFindAccount.setEnabled(true);
                btnFindAccount.setText(R.string.btn_find_account);

                if (user == null) {
                    tilEmail.setError(getString(R.string.forgot_email_not_found));
                } else {
                    confirmedEmail = email;
                    showStep2(email);
                }
            });
        });
    }

    // ── Step 2: Set new password ─────────────────────────────────────────────

    private void handleResetPassword() {
        tilNewPassword.setError(null);
        tilConfirmPassword.setError(null);

        String newPwd     = text(etNewPassword);
        String confirmPwd = text(etConfirmPassword);

        if (TextUtils.isEmpty(newPwd)) {
            tilNewPassword.setError(getString(R.string.error_password_required));
            return;
        }
        if (newPwd.length() < 6) {
            tilNewPassword.setError(getString(R.string.forgot_password_too_short));
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            tilConfirmPassword.setError(getString(R.string.forgot_password_mismatch));
            return;
        }

        btnResetPassword.setEnabled(false);
        btnResetPassword.setText(R.string.btn_reset_password_loading);

        String hashedPassword = PasswordUtils.hash(newPwd);

        executor.execute(() -> {
            db.userDao().updatePassword(confirmedEmail, hashedPassword);

            runOnUiThread(() -> {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText(R.string.btn_reset_password);
                showSuccess();
            });
        });
    }

    // ── Navigation helpers ───────────────────────────────────────────────────

    private void showStep2(String email) {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        layoutSuccess.setVisibility(View.GONE);

        tvFoundEmail.setText(email);
        tvStep.setText(R.string.forgot_step_reset);
        tvSubtitle.setText(R.string.forgot_step2_subtitle);
        tvBackToLogin.setVisibility(View.VISIBLE);
    }

    private void showSuccess() {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.VISIBLE);

        tvStep.setVisibility(View.GONE);
        tvSubtitle.setVisibility(View.GONE);
        tvBackToLogin.setVisibility(View.GONE);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}






