package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

import me.madhushan.reflect.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private TextView tvUserName, tvAvatarInitials;
    private SwitchMaterial switchDarkMode, switchNotifications;
    private LinearLayout rowPersonalDetails, rowSubscription, rowHelp, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        initViews();
        setupDarkModeSwitch();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateUserData();
    }

    private void initViews() {
        tvUserName        = findViewById(R.id.tv_user_name);
        tvAvatarInitials  = findViewById(R.id.tv_avatar_initials);
        switchDarkMode    = findViewById(R.id.switch_dark_mode);
        switchNotifications = findViewById(R.id.switch_notifications);
        rowPersonalDetails  = findViewById(R.id.row_personal_details);
        rowSubscription     = findViewById(R.id.row_subscription);
        rowHelp             = findViewById(R.id.row_help);
        btnLogout           = findViewById(R.id.btn_logout);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_edit_avatar).setOnClickListener(v ->
                startActivity(new Intent(this, PersonalDetailsActivity.class)));
    }

    private void populateUserData() {
        String fullName = sessionManager.getUserName();
        if (fullName == null || fullName.isEmpty()) fullName = "User";
        tvUserName.setText(fullName);
        tvAvatarInitials.setText(getInitials(fullName));
    }

    private void setupDarkModeSwitch() {
        // Reflect current system/night mode in the toggle
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        boolean isDark = (currentMode == AppCompatDelegate.MODE_NIGHT_YES) ||
                (currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                        (getResources().getConfiguration().uiMode &
                                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                                android.content.res.Configuration.UI_MODE_NIGHT_YES);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupClickListeners() {
        rowPersonalDetails.setOnClickListener(v ->
                startActivity(new Intent(this, PersonalDetailsActivity.class)));

        rowSubscription.setOnClickListener(v ->
                startActivity(new Intent(this, SubscriptionActivity.class)));

        rowHelp.setOnClickListener(v ->
                startActivity(new Intent(this, HelpSupportActivity.class)));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Notifications enabled" : "Notifications disabled",
                        Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Bottom nav
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
        findViewById(R.id.nav_goals).setOnClickListener(v ->
                Toast.makeText(this, "Goals — coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.nav_journal).setOnClickListener(v ->
                Toast.makeText(this, "Journal — coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.nav_profile).setOnClickListener(v -> { /* already here */ });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_message)
                .setPositiveButton(R.string.profile_logout_confirm_yes, (dialog, which) -> {
                    sessionManager.clearSession();
                    // Reset to follow system theme on logout
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .setNegativeButton(R.string.profile_logout_confirm_cancel, null)
                .show();
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }
}


