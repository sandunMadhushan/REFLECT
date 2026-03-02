package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);

        // Populate user name + avatar initials from session
        String fullName = sessionManager.getUserName();
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvAvatarInitials = findViewById(R.id.tv_avatar_initials);
        tvUserName.setText(fullName);
        tvAvatarInitials.setText(getInitials(fullName));

        // Set circular progress (5/8 = 0.625)
        CircularProgressView circularProgress = findViewById(R.id.circular_progress);
        circularProgress.setProgress(0.625f);

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

    /** Returns up-to-2-letter initials from a full name */
    private String getInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }
}