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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.FacebookSignInHelper;
import me.madhushan.reflect.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvUserName, tvAvatarInitials;
    private ImageView ivAvatarPhoto;
    private SwitchMaterial switchDarkMode, switchNotifications;

    // Achievement summary views
    private TextView tvAchievementsSummary, tvProfileXp, tvProfileAchievementsCount;
    private View profileAchievementFill;
    private ExecutorService executor;

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

        // Achievement views
        tvAchievementsSummary        = v.findViewById(R.id.tv_achievements_summary);
        tvProfileXp                  = v.findViewById(R.id.tv_profile_xp);
        tvProfileAchievementsCount   = v.findViewById(R.id.tv_profile_achievements_count);
        profileAchievementFill       = v.findViewById(R.id.profile_achievement_fill);
        executor = Executors.newSingleThreadExecutor();

        v.findViewById(R.id.btn_achievements).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), AchievementsActivity.class)));

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
        loadAchievementSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateUserData();
        syncNotificationToggle();
        loadAchievementSummary();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }

    private void loadAchievementSummary() {
        if (!isAdded()) return;
        int userId = sessionManager.getUserId();
        AppDatabase db = AppDatabase.getInstance(requireContext());

        executor.execute(() -> {
            AchievementEngine.Stats stats = new AchievementEngine.Stats();
            stats.maxHabitStreak = getMaxStreak(db, userId);
            stats.totalReflections = db.reflectionDao().getReflectionCount(userId);
            stats.totalGoals = db.goalDao().getTotalGoalsCount(userId);
            stats.completedGoals = db.goalDao().getCompletedGoalsCount(userId);
            stats.totalHabits = db.habitDao().getTotalHabitsCount(userId);
            stats.habitCompletionsTotal = db.habitCompletionDao().getTotalCompletionsForUser(userId);

            List<AchievementEngine.Achievement> all = AchievementEngine.evaluate(stats);
            int xp = AchievementEngine.calcXp(all);
            int level = AchievementEngine.calcLevel(xp);
            int unlocked = 0;
            for (AchievementEngine.Achievement a : all) if (a.unlocked) unlocked++;
            final int total = all.size();
            final int finalXp = xp;
            final int finalLevel = level;
            final int finalUnlocked = unlocked;

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                tvAchievementsSummary.setText("Level " + finalLevel + "  ·  " + finalXp + " XP");
                tvProfileXp.setText("View All");
                tvProfileAchievementsCount.setText(finalUnlocked + "/" + total);

                // Animate progress fill
                profileAchievementFill.post(() -> {
                    if (profileAchievementFill.getParent() == null) return;
                    int parentWidth = ((View) profileAchievementFill.getParent()).getWidth();
                    float pct = total > 0 ? (float) finalUnlocked / total : 0f;
                    ViewGroup.LayoutParams lp = profileAchievementFill.getLayoutParams();
                    lp.width = (int)(parentWidth * pct);
                    profileAchievementFill.setLayoutParams(lp);
                });
            });
        });
    }

    private int getMaxStreak(AppDatabase db, int userId) {
        List<me.madhushan.reflect.database.Habit> habits = db.habitDao().getHabitsForUser(userId);
        int max = 0;
        for (me.madhushan.reflect.database.Habit h : habits) if (h.streakCount > max) max = h.streakCount;
        return max;
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
                    FacebookSignInHelper.logOut(); // clear Facebook session
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .setNegativeButton(R.string.profile_logout_confirm_cancel, null).show();
    }
}





