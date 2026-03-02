package me.madhushan.reflect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    // Launcher for the POST_NOTIFICATIONS permission dialog
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                sessionManager.markNotifDialogShown();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Populate user name + avatar from session
        String fullName = sessionManager.getUserName();
        String photoUrl = sessionManager.getPhotoUrl();

        TextView tvUserName     = findViewById(R.id.tv_user_name);
        TextView tvInitials     = findViewById(R.id.tv_avatar_initials);
        ImageView ivAvatarPhoto = findViewById(R.id.iv_avatar_photo);

        tvUserName.setText(fullName);
        AvatarLoader.load(ivAvatarPhoto, tvInitials,
                photoUrl, AvatarLoader.getInitials(fullName));

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

    private void requestNotificationPermissionIfNeeded() {
        // Only show the system dialog ONCE ever — never overwrite user's saved choice
        if (sessionManager.hasNotifDialogBeenShown()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                sessionManager.setNotificationsEnabled(true);
                sessionManager.markNotifDialogShown();
            } else {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            boolean enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
            sessionManager.setNotificationsEnabled(enabled);
            sessionManager.markNotifDialogShown();
        }
    }
}