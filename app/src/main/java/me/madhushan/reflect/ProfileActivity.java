package me.madhushan.reflect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import me.madhushan.reflect.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private TextView tvUserName, tvAvatarInitials;
    private SwitchMaterial switchDarkMode, switchNotifications;
    private LinearLayout rowPersonalDetails, rowSubscription, rowHelp, btnLogout;

    // Launcher for the POST_NOTIFICATIONS runtime permission
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                sessionManager.markNotifDialogShown();
                switchNotifications.setOnCheckedChangeListener(null);
                switchNotifications.setChecked(isGranted);
                setupNotificationToggle();
                if (!isGranted) {
                    Toast.makeText(this,
                            "Permission denied. Enable notifications in App Settings.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        initViews();
        setupDarkModeSwitch();
        setupNotificationToggle();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateUserData();
        // Sync toggle with actual permission state every time screen is visible
        // (user may have changed it in system settings)
        syncNotificationToggle();
    }

    private void initViews() {
        tvUserName          = findViewById(R.id.tv_user_name);
        tvAvatarInitials    = findViewById(R.id.tv_avatar_initials);
        switchDarkMode      = findViewById(R.id.switch_dark_mode);
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

    /** Called on onResume — shows correct toggle state without overwriting the user's choice. */
    private void syncNotificationToggle() {
        boolean systemGranted = isNotificationPermissionGranted();

        if (!systemGranted) {
            // System permission was revoked externally (via System Settings)
            // Force save OFF and show toggle as OFF
            sessionManager.clearNotificationPreference();
        }

        // Always read the saved pref — never overwrite it here
        boolean savedChoice = sessionManager.getNotificationsEnabled();

        switchNotifications.setOnCheckedChangeListener(null);
        switchNotifications.setChecked(savedChoice);
        setupNotificationToggle();
    }

    /** Attach the toggle listener. */
    private void setupNotificationToggle() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleNotificationToggleOn();
            } else {
                handleNotificationToggleOff();
            }
        });
    }

    private void handleNotificationToggleOff() {
        // Simply save OFF — do NOT clear the dialog-shown flag.
        // This prevents MainActivity from re-triggering the system dialog.
        sessionManager.setNotificationsEnabled(false);
    }

    private void handleNotificationToggleOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isNotificationPermissionGranted()) {
                // System permission is granted — show an in-app confirmation dialog
                // (Android won't re-show the system dialog for already-granted permissions)
                new AlertDialog.Builder(this)
                        .setTitle("Enable Notifications")
                        .setMessage("Allow Reflect to send you daily reflection reminders and goal updates?")
                        .setPositiveButton("Enable", (d, w) ->
                                sessionManager.setNotificationsEnabled(true))
                        .setNegativeButton("Not now", (d, w) -> revertToggleOff())
                        .show();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Denied before — show rationale then re-request
                new AlertDialog.Builder(this)
                        .setTitle("Enable Notifications")
                        .setMessage("Reflect needs notification permission to send you daily reflection reminders and goal updates.")
                        .setPositiveButton("Allow", (d, w) ->
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton("Not now", (d, w) -> revertToggleOff())
                        .show();
            } else {
                // Permanently denied — go to App Settings
                revertToggleOff();
                showOpenSettingsDialog();
            }
        } else {
            // Android 12 and below
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                new AlertDialog.Builder(this)
                        .setTitle("Enable Notifications")
                        .setMessage("Allow Reflect to send you daily reflection reminders and goal updates?")
                        .setPositiveButton("Enable", (d, w) ->
                                sessionManager.setNotificationsEnabled(true))
                        .setNegativeButton("Not now", (d, w) -> revertToggleOff())
                        .show();
            } else {
                revertToggleOff();
                showOpenSettingsDialog();
            }
        }
    }

    private void revertToggleOff() {
        switchNotifications.setOnCheckedChangeListener(null);
        switchNotifications.setChecked(false);
        setupNotificationToggle();
        sessionManager.setNotificationsEnabled(false);
    }

    private void showOpenSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notifications Blocked")
                .setMessage("Notification permission is disabled. Please enable it in App Settings to receive reminders.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — check the runtime permission
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below — check system notification setting
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        }
    }

    private void setupDarkModeSwitch() {
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


