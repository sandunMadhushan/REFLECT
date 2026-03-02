package me.madhushan.reflect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Goal;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private GoalDao goalDao;
    private ExecutorService executor;

    // Views
    private LinearLayout statsRow, emptyState, sectionProgress, chartCard;
    private TextView tvRecentLabel;
    private LinearLayout recentActivityContainer;
    private TextView tvActiveGoals, tvCompleted, tvHabits;
    private CircularProgressView circularProgress;

    // Bar chart bar views
    private View[] bars; // [0]=Mon ... [6]=Sun

    // Launcher for notification permission
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                sessionManager.markNotifDialogShown();
            });

    // Launcher for Goals / AddGoal screens — refresh home when we return
    private final ActivityResultLauncher<Intent> goalsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> loadHomeData());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        goalDao = AppDatabase.getInstance(this).goalDao();
        executor = Executors.newSingleThreadExecutor();

        // Avatar + name
        String fullName = sessionManager.getUserName();
        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvInitials = findViewById(R.id.tv_avatar_initials);
        ImageView ivAvatarPhoto = findViewById(R.id.iv_avatar_photo);
        tvUserName.setText(fullName);
        AvatarLoader.loadFromSession(this, ivAvatarPhoto, tvInitials, sessionManager);

        // Find sections
        statsRow                = findViewById(R.id.stats_row);
        emptyState              = findViewById(R.id.empty_state);
        sectionProgress         = findViewById(R.id.section_progress);
        chartCard               = findViewById(R.id.chart_card);
        tvRecentLabel           = findViewById(R.id.tv_recent_activity_label);
        recentActivityContainer = findViewById(R.id.recent_activity_container);

        // Stats text views
        tvActiveGoals    = findViewById(R.id.tv_active_goals_count);
        tvCompleted      = findViewById(R.id.tv_completed_count);
        tvHabits         = findViewById(R.id.tv_habits_count);
        circularProgress = findViewById(R.id.circular_progress);

        // Chart bars
        bars = new View[]{
            findViewById(R.id.bar_mon),
            findViewById(R.id.bar_tue),
            findViewById(R.id.bar_wed),
            findViewById(R.id.bar_thu),
            findViewById(R.id.bar_fri),
            findViewById(R.id.bar_sat),
            findViewById(R.id.bar_sun)
        };

        setupNavigation();
        requestNotificationPermissionIfNeeded();
        loadHomeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData();
        AvatarLoader.loadFromSession(this,
                (ImageView) findViewById(R.id.iv_avatar_photo),
                (TextView)  findViewById(R.id.tv_avatar_initials),
                sessionManager);
    }

    // ── Load real data from Room ──────────────────────────────────────────

    private void loadHomeData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            int activeCount    = goalDao.getActiveGoalsCount(userId);
            int completedCount = goalDao.getCompletedGoalsCount(userId);
            int totalCount     = goalDao.getTotalGoalsCount(userId);
            List<Goal> recent  = goalDao.getRecentGoals(userId, 5);

            int[] barCounts = getWeeklyBarCounts(userId);
            int maxBar = 1;
            for (int c : barCounts) if (c > maxBar) maxBar = c;

            final int finalMax = maxBar;
            runOnUiThread(() -> updateUI(activeCount, completedCount, totalCount,
                    recent, barCounts, finalMax));
        });
    }

    private int[] getWeeklyBarCounts(int userId) {
        int[] counts = new int[7];
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMon = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 2;
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMon);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            counts[i] = goalDao.getActivityCountForDate(userId, sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return counts;
    }

    private void updateUI(int activeCount, int completedCount, int totalCount,
                          List<Goal> recent, int[] barCounts, int maxBar) {
        boolean hasGoals = totalCount > 0;

        emptyState.setVisibility(hasGoals ? View.GONE  : View.VISIBLE);
        statsRow.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        sectionProgress.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        chartCard.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        tvRecentLabel.setVisibility(hasGoals && !recent.isEmpty() ? View.VISIBLE : View.GONE);
        recentActivityContainer.setVisibility(hasGoals ? View.VISIBLE : View.GONE);

        if (!hasGoals) return;

        tvActiveGoals.setText(String.valueOf(activeCount));
        tvCompleted.setText(String.valueOf(completedCount));
        tvHabits.setText(activeCount + "/" + totalCount);
        float progress = totalCount > 0 ? (float) completedCount / totalCount : 0f;
        circularProgress.setProgress(progress);

        // Bar chart
        int maxHeightDp = 112;
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < 7; i++) {
            int heightDp = maxBar > 0 ? Math.max(8, (int)((barCounts[i] / (float) maxBar) * maxHeightDp)) : 8;
            android.view.ViewGroup.LayoutParams lp = bars[i].getLayoutParams();
            lp.height = (int)(heightDp * density);
            bars[i].setLayoutParams(lp);
        }
        highlightTodayBar();

        // Recent activity rows
        recentActivityContainer.removeAllViews();
        for (Goal goal : recent) {
            View row = buildActivityRow(goal);
            recentActivityContainer.addView(row);
        }
    }

    private void highlightTodayBar() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int todayIndex = (dow == Calendar.SUNDAY) ? 6 : dow - 2;
        for (int i = 0; i < bars.length; i++) {
            bars[i].setBackgroundResource(
                i == todayIndex ? R.drawable.bg_bar_active : R.drawable.bg_bar_inactive_dark);
        }
    }

    private View buildActivityRow(Goal goal) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card_dark);
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        float density = getResources().getDisplayMetrics().density;
        rowParams.setMargins(0, 0, 0, (int)(10 * density));
        row.setLayoutParams(rowParams);
        int pad = (int)(14 * density);
        row.setPadding(pad, pad, pad, pad);

        // Icon circle
        FrameLayout iconBg = new FrameLayout(this);
        int size = (int)(40 * density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(size, size);
        iconBg.setLayoutParams(iconParams);
        iconBg.setBackgroundResource(goal.isAchieved == 1
                ? R.drawable.bg_circle_green : R.drawable.bg_circle_blue);
        ImageView icon = new ImageView(this);
        FrameLayout.LayoutParams iParams = new FrameLayout.LayoutParams(
                (int)(20 * density), (int)(20 * density));
        iParams.gravity = android.view.Gravity.CENTER;
        icon.setLayoutParams(iParams);
        icon.setImageResource(goal.isAchieved == 1 ? R.drawable.ic_check_circle : R.drawable.ic_flag);
        icon.setColorFilter(getResources().getColor(
                goal.isAchieved == 1 ? R.color.colorGreenIcon : R.color.colorBlueIcon, null));
        iconBg.addView(icon);
        row.addView(iconBg);

        // Text column
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textParams.setMarginStart((int)(14 * density));
        textCol.setLayoutParams(textParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(goal.title != null ? goal.title : "Untitled Goal");
        tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(this);
        String status = goal.isAchieved == 1 ? "✅ Achieved" : "🎯 In Progress";
        String date   = goal.updatedAt != null ? "  ·  " + goal.updatedAt : "";
        tvSub.setText(status + date);
        tvSub.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        tvSub.setTextSize(11f);
        textCol.addView(tvSub);
        row.addView(textCol);

        // Arrow
        ImageView arrow = new ImageView(this);
        int arrowSize = (int)(18 * density);
        LinearLayout.LayoutParams aParams = new LinearLayout.LayoutParams(arrowSize, arrowSize);
        aParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        arrow.setLayoutParams(aParams);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(getResources().getColor(R.color.text_hint, null));
        row.addView(arrow);

        // Click → open goal details
        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalDetailsActivity.class);
            intent.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
            goalsLauncher.launch(intent);
        });

        return row;
    }

    // ── Navigation ────────────────────────────────────────────────────────

    private void setupNavigation() {
        // Home — already here
        findViewById(R.id.nav_home).setOnClickListener(v -> { /* no-op */ });

        // Goals nav → GoalsActivity
        findViewById(R.id.nav_goals).setOnClickListener(v ->
                goalsLauncher.launch(new Intent(this, GoalsActivity.class)));

        // FAB + → AddGoalActivity directly
        findViewById(R.id.nav_add).setOnClickListener(v ->
                goalsLauncher.launch(new Intent(this, AddGoalActivity.class)));

        // Empty state "Add First Goal" button → AddGoalActivity
        findViewById(R.id.btn_add_first_goal).setOnClickListener(v ->
                goalsLauncher.launch(new Intent(this, AddGoalActivity.class)));

        // Journal — coming soon
        findViewById(R.id.nav_journal).setOnClickListener(v ->
                Toast.makeText(this, "Journal — coming soon", Toast.LENGTH_SHORT).show());

        // Profile
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Notification bell
        findViewById(R.id.btn_notifications).setOnClickListener(v ->
                Toast.makeText(this, "Notifications — coming soon", Toast.LENGTH_SHORT).show());

        // Block back button — user must log out explicitly
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { /* blocked */ }
        });
    }

    // ── Notification permission ───────────────────────────────────────────

    private void requestNotificationPermissionIfNeeded() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}
