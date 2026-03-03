package me.madhushan.reflect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvUserName, tvAvatarInitials;
    private ImageView ivAvatarPhoto;
    private SwitchMaterial switchDarkMode, switchNotifications;

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                sessionManager.markNotifDialogShown();
                switchNotifications.setOnCheckedChangeListener(null);
                switchNotifications.setChecked(isGranted);
                setupNotificationToggle();
                if (!isGranted)
                    Toast.makeText(requireContext(), "Enable notifications in App Settings.", Toast.LENGTH_LONG).show();
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        sessionManager      = new SessionManager(requireContext());
        tvUserName          = v.findViewById(R.id.tv_user_name);
        tvAvatarInitials    = v.findViewById(R.id.tv_avatar_initials);
        ivAvatarPhoto       = v.findViewById(R.id.iv_avatar_photo);
        switchDarkMode      = v.findViewById(R.id.switch_dark_mode);
        switchNotifications = v.findViewById(R.id.switch_notifications);

        v.findViewById(R.id.btn_edit_avatar).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), PersonalDetailsActivity.class)));
        v.findViewById(R.id.row_personal_details).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), PersonalDetailsActivity.class)));
        v.findViewById(R.id.row_subscription).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), SubscriptionActivity.class)));
        v.findViewById(R.id.row_help).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), HelpSupportActivity.class)));
        v.findViewById(R.id.btn_logout).setOnClickListener(b -> showLogoutDialog());

        setupDarkModeSwitch();
        setupNotificationToggle();
        populateUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateUserData();
        syncNotificationToggle();
    }

    private void populateUserData() {
        String name = sessionManager.getUserName();
        tvUserName.setText(name == null || name.isEmpty() ? "User" : name);
        AvatarLoader.loadFromSession(requireContext(), ivAvatarPhoto, tvAvatarInitials, sessionManager);
    }

    private void syncNotificationToggle() {
        if (!isNotificationPermissionGranted()) sessionManager.clearNotificationPreference();
        boolean saved = sessionManager.getNotificationsEnabled();
        switchNotifications.setOnCheckedChangeListener(null);
        switchNotifications.setChecked(saved);
        setupNotificationToggle();
    }

    private void setupNotificationToggle() {
        switchNotifications.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) handleNotificationToggleOn(); else handleNotificationToggleOff();
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
                new AlertDialog.Builder(requireContext())
                        .setTitle("Enable Notifications")
                        .setMessage("Allow Reflect to send daily reminders and goal updates?")
                        .setPositiveButton("Enable", (d, w) -> sessionManager.setNotificationsEnabled(true))
                        .setNegativeButton("Not now", (d, w) -> revertToggleOff()).show();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Enable Notifications")
                        .setMessage("Reflect needs notification permission for reminders.")
                        .setPositiveButton("Allow", (d, w) -> notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton("Not now", (d, w) -> revertToggleOff()).show();
            } else { revertToggleOff(); showOpenSettingsDialog(); }
        } else {
            if (NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Enable Notifications")
                        .setMessage("Allow Reflect to send daily reminders?")
                        .setPositiveButton("Enable", (d, w) -> sessionManager.setNotificationsEnabled(true))
                        .setNegativeButton("Not now", (d, w) -> revertToggleOff()).show();
            } else { revertToggleOff(); showOpenSettingsDialog(); }
        }
    }

    private void revertToggleOff() {
        switchNotifications.setOnCheckedChangeListener(null);
        switchNotifications.setChecked(false);
        setupNotificationToggle();
        sessionManager.setNotificationsEnabled(false);
    }

    private void showOpenSettingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Notifications Blocked")
                .setMessage("Please enable notifications in App Settings.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(i);
                })
                .setNegativeButton("Cancel", null).show();
    }

    private boolean isNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        return NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();
    }

    private void setupDarkModeSwitch() {
        int mode = AppCompatDelegate.getDefaultNightMode();
        boolean isDark = mode == AppCompatDelegate.MODE_NIGHT_YES ||
                (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM &&
                        (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                                == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((btn, checked) ->
                AppCompatDelegate.setDefaultNightMode(checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_message)
                .setPositiveButton(R.string.profile_logout_confirm_yes, (d, w) -> {
                    sessionManager.clearSession();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .setNegativeButton(R.string.profile_logout_confirm_cancel, null).show();
    }
}





