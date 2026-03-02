package me.madhushan.reflect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    // Launcher for the POST_NOTIFICATIONS permission dialog
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                // Result is silently saved — ProfileActivity.onResume() will reflect it
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Populate user name + avatar initials from session
        String fullName = sessionManager.getUserName();
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvAvatarInitials = findViewById(R.id.tv_avatar_initials);
        tvUserName.setText(fullName);
        tvAvatarInitials.setText(getInitials(fullName));

        // Set circular progress (5/8 = 0.625)
        CircularProgressView circularProgress = findViewById(R.id.circular_progress);
        circularProgress.setProgress(0.625f);

        // Request notification permission on first app open (Android 13+)
        requestNotificationPermissionIfNeeded();

        // Bottom nav click listeners
        findViewById(R.id.nav_home).setOnClickListener(v ->
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_goals).setOnClickListener(v ->
                Toast.makeText(this, "Goals — coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_add).setOnClickListener(v ->
                Toast.makeText(this, "Add — coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_journal).setOnClickListener(v ->
                Toast.makeText(this, "Journal — coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Notification bell
        findViewById(R.id.btn_notifications).setOnClickListener(v ->
                Toast.makeText(this, "Notifications — coming soon", Toast.LENGTH_SHORT).show());

        // Block back — user must log out explicitly
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing — back is blocked on home screen
            }
        });
    }

    /**
     * On Android 13+ (API 33), POST_NOTIFICATIONS is a runtime permission.
     * Ask only if not already granted. Save the result so the Profile toggle
     * reflects the actual state.
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Below Android 13 — notifications are allowed by default
            sessionManager.setNotificationsEnabled(true);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            // Already granted — make sure the pref is in sync
            sessionManager.setNotificationsEnabled(true);
        } else {
            // Ask the user — result handled in notifPermissionLauncher
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    /** Returns up-to-2-letter initials from a full name */
    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }
}