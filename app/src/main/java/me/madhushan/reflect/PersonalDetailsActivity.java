package me.madhushan.reflect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.UserDao;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.PasswordUtils;
import me.madhushan.reflect.utils.SessionManager;

public class PersonalDetailsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private UserDao userDao;
    private ExecutorService executor;

    private TextView tvAvatarInitials;
    private ImageView ivAvatarPhoto;
    private EditText etFullName, etEmail, etCurrentPassword, etNewPassword, etConfirmPassword;
    private LinearLayout btnSave, btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        sessionManager = new SessionManager(this);
        userDao = AppDatabase.getInstance(this).userDao();
        executor = Executors.newSingleThreadExecutor();

        initViews();
        populateData();
        setupListeners();
    }

    private void initViews() {
        tvAvatarInitials   = findViewById(R.id.tv_avatar_initials);
        ivAvatarPhoto      = findViewById(R.id.iv_avatar_photo);
        etFullName         = findViewById(R.id.et_full_name);
        etEmail            = findViewById(R.id.et_email);
        etCurrentPassword  = findViewById(R.id.et_current_password);
        etNewPassword      = findViewById(R.id.et_new_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        btnSave            = findViewById(R.id.btn_save);
        btnDeleteAccount   = findViewById(R.id.btn_delete_account);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void populateData() {
        String name     = sessionManager.getUserName();
        String email    = sessionManager.getUserEmail();
        String photoUrl = sessionManager.getPhotoUrl();
        if (name  == null) name  = "";
        if (email == null) email = "";

        etFullName.setText(name);
        etEmail.setText(email);
        AvatarLoader.load(ivAvatarPhoto, tvAvatarInitials,
                photoUrl, AvatarLoader.getInitials(name));
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveChanges());
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void saveChanges() {
        String newName    = etFullName.getText().toString().trim();
        String currentPwd = etCurrentPassword.getText().toString();
        String newPwd     = etNewPassword.getText().toString();
        String confirmPwd = etConfirmPassword.getText().toString();

        // Validate name
        if (TextUtils.isEmpty(newName)) {
            etFullName.setError(getString(R.string.personal_details_error_name_empty));
            etFullName.requestFocus();
            return;
        }

        boolean changingPassword = !TextUtils.isEmpty(currentPwd) ||
                                   !TextUtils.isEmpty(newPwd) ||
                                   !TextUtils.isEmpty(confirmPwd);

        String email = sessionManager.getUserEmail();

        if (changingPassword) {
            // Validate password fields
            if (newPwd.length() < 6) {
                etNewPassword.setError(getString(R.string.personal_details_error_pwd_short));
                etNewPassword.requestFocus();
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                etConfirmPassword.setError(getString(R.string.personal_details_error_pwd_mismatch));
                etConfirmPassword.requestFocus();
                return;
            }

            String hashedCurrent = PasswordUtils.hash(currentPwd);
            String finalNewName = newName;
            executor.execute(() -> {
                me.madhushan.reflect.database.User user = userDao.findByEmail(email);
                if (user == null || !user.passwordHash.equals(hashedCurrent)) {
                    runOnUiThread(() -> {
                        etCurrentPassword.setError(getString(R.string.personal_details_error_current_pwd));
                        etCurrentPassword.requestFocus();
                    });
                    return;
                }

                // Update name and password
                userDao.updateNameAndPassword(email, finalNewName, PasswordUtils.hash(newPwd));

                runOnUiThread(() -> {
                    sessionManager.setUserName(finalNewName);
                    tvAvatarInitials.setText(AvatarLoader.getInitials(finalNewName));
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                    Toast.makeText(this, getString(R.string.personal_details_success_password), Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else {
            // Name-only update
            String finalNewName = newName;
            executor.execute(() -> {
                userDao.updateName(email, finalNewName);
                runOnUiThread(() -> {
                    sessionManager.setUserName(finalNewName);
                    tvAvatarInitials.setText(AvatarLoader.getInitials(finalNewName));
                    Toast.makeText(this, getString(R.string.personal_details_success_name), Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.personal_details_delete_confirm_title)
                .setMessage(R.string.personal_details_delete_confirm_msg)
                .setPositiveButton(R.string.personal_details_delete_confirm_yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.personal_details_delete_confirm_cancel, null)
                .show();
    }

    private void deleteAccount() {
        String email = sessionManager.getUserEmail();
        executor.execute(() -> {
            me.madhushan.reflect.database.User user = userDao.findByEmail(email);
            if (user != null) {
                userDao.deleteUser(user);
            }
            sessionManager.clearSession();
            runOnUiThread(() -> {
                android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}













