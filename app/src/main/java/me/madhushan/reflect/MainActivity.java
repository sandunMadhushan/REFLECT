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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    // ── Home tab views ────────────────────────────────────────────────────
    private LinearLayout statsRow, emptyState, sectionProgress, chartCard;
    private TextView tvRecentLabel;
    private LinearLayout recentActivityContainer;
    private TextView tvActiveGoals, tvCompleted, tvHabits;
    private CircularProgressView circularProgress;
    private View[] bars;
    private TextView[] dayLabels;

    // ── Goals tab views ───────────────────────────────────────────────────
    private View tabHome, tabGoals;
    private LinearLayout goalsContainer, goalsEmptyState;
    private TextView goalsFilterAll, goalsFilterActive, goalsFilterCompleted;
    private String currentGoalFilter = "all";

    // ── Nav bar items ─────────────────────────────────────────────────────
    private ImageView navHomeIcon, navGoalsIcon;
    private TextView navHomeLabel, navGoalsLabel;

    // ── Current tab ───────────────────────────────────────────────────────
    private String currentTab = "home"; // "home" or "goals"

    // ── Launchers ─────────────────────────────────────────────────────────
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                sessionManager.markNotifDialogShown();
            });

    private final ActivityResultLauncher<Intent> goalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadHomeData();
                    loadGoalsData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        goalDao        = AppDatabase.getInstance(this).goalDao();
        executor       = Executors.newSingleThreadExecutor();

        // ── Avatar + name ─────────────────────────────────────────────────
        TextView tvUserName   = findViewById(R.id.tv_user_name);
        TextView tvInitials   = findViewById(R.id.tv_avatar_initials);
        ImageView ivAvatarPhoto = findViewById(R.id.iv_avatar_photo);
        tvUserName.setText(sessionManager.getUserName());
        AvatarLoader.loadFromSession(this, ivAvatarPhoto, tvInitials, sessionManager);

        // ── Home tab views ─────────────────────────────────────────────────
        tabHome             = findViewById(R.id.tab_home);
        statsRow            = findViewById(R.id.stats_row);
        emptyState          = findViewById(R.id.empty_state);
        sectionProgress     = findViewById(R.id.section_progress);
        chartCard           = findViewById(R.id.chart_card);
        tvRecentLabel       = findViewById(R.id.tv_recent_activity_label);
        recentActivityContainer = findViewById(R.id.recent_activity_container);
        tvActiveGoals       = findViewById(R.id.tv_active_goals_count);
        tvCompleted         = findViewById(R.id.tv_completed_count);
        tvHabits            = findViewById(R.id.tv_habits_count);
        circularProgress    = findViewById(R.id.circular_progress);

        bars = new View[]{
            findViewById(R.id.bar_mon), findViewById(R.id.bar_tue),
            findViewById(R.id.bar_wed), findViewById(R.id.bar_thu),
            findViewById(R.id.bar_fri), findViewById(R.id.bar_sat),
            findViewById(R.id.bar_sun)
        };
        dayLabels = new TextView[]{
            findViewById(R.id.label_mon), findViewById(R.id.label_tue),
            findViewById(R.id.label_wed), findViewById(R.id.label_thu),
            findViewById(R.id.label_fri), findViewById(R.id.label_sat),
            findViewById(R.id.label_sun)
        };

        // ── Goals tab views ────────────────────────────────────────────────
        tabGoals            = findViewById(R.id.tab_goals);
        goalsContainer      = findViewById(R.id.goals_container);
        goalsEmptyState     = findViewById(R.id.goals_empty_state);
        goalsFilterAll      = findViewById(R.id.goals_filter_all);
        goalsFilterActive   = findViewById(R.id.goals_filter_active);
        goalsFilterCompleted = findViewById(R.id.goals_filter_completed);

        goalsFilterAll.setOnClickListener(v      -> { currentGoalFilter = "all";       applyGoalsFilterUI(); loadGoalsData(); });
        goalsFilterActive.setOnClickListener(v   -> { currentGoalFilter = "active";    applyGoalsFilterUI(); loadGoalsData(); });
        goalsFilterCompleted.setOnClickListener(v -> { currentGoalFilter = "completed"; applyGoalsFilterUI(); loadGoalsData(); });

        // Goals FAB
        findViewById(R.id.goals_fab).setOnClickListener(v ->
                goalLauncher.launch(new Intent(this, AddGoalActivity.class)));

        // ── Nav bar items ──────────────────────────────────────────────────
        navHomeIcon   = findViewById(R.id.nav_home_icon);
        navHomeLabel  = findViewById(R.id.nav_home_label);
        navGoalsIcon  = findViewById(R.id.nav_goals_icon);
        navGoalsLabel = findViewById(R.id.nav_goals_label);

        setupNavigation();
        requestNotificationPermissionIfNeeded();
        loadHomeData();
        loadGoalsData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData();
        loadGoalsData();
        AvatarLoader.loadFromSession(this,
                findViewById(R.id.iv_avatar_photo),
                findViewById(R.id.tv_avatar_initials),
                sessionManager);
    }

    // ── Tab switching ──────────────────────────────────────────────────────

    private void switchTab(String tab) {
        currentTab = tab;

        tabHome.setVisibility(tab.equals("home")  ? View.VISIBLE : View.GONE);
        tabGoals.setVisibility(tab.equals("goals") ? View.VISIBLE : View.GONE);

        int primary   = getResources().getColor(R.color.primary,    null);
        int hint      = getResources().getColor(R.color.text_hint,  null);

        // Home nav item
        navHomeIcon.setColorFilter(tab.equals("home") ? primary : hint);
        navHomeLabel.setTextColor(tab.equals("home") ? primary : hint);
        navHomeLabel.setTypeface(null, tab.equals("home")
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        // Goals nav item
        navGoalsIcon.setColorFilter(tab.equals("goals") ? primary : hint);
        navGoalsLabel.setTextColor(tab.equals("goals") ? primary : hint);
        navGoalsLabel.setTypeface(null, tab.equals("goals")
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    // ── Home data ──────────────────────────────────────────────────────────

    private void loadHomeData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            int activeCount    = goalDao.getActiveGoalsCount(userId);
            int completedCount = goalDao.getCompletedGoalsCount(userId);
            int totalCount     = goalDao.getTotalGoalsCount(userId);
            List<Goal> recent  = goalDao.getRecentGoals(userId, 5);
            int[] barCounts    = getWeeklyBarCounts(userId);
            int maxBar = 1;
            for (int c : barCounts) if (c > maxBar) maxBar = c;
            final int finalMax = maxBar;
            runOnUiThread(() -> updateHomeUI(activeCount, completedCount, totalCount,
                    recent, barCounts, finalMax));
        });
    }

    private int[] getWeeklyBarCounts(int userId) {
        int[] counts = new int[7];
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMon = (dow == Calendar.SUNDAY) ? 6 : dow - 2;
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMon);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            counts[i] = goalDao.getActivityCountForDate(userId, sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return counts;
    }

    private void updateHomeUI(int activeCount, int completedCount, int totalCount,
                              List<Goal> recent, int[] barCounts, int maxBar) {
        boolean hasGoals = totalCount > 0;
        emptyState.setVisibility(hasGoals      ? View.GONE    : View.VISIBLE);
        statsRow.setVisibility(hasGoals         ? View.VISIBLE : View.GONE);
        sectionProgress.setVisibility(hasGoals  ? View.VISIBLE : View.GONE);
        chartCard.setVisibility(hasGoals        ? View.VISIBLE : View.GONE);
        tvRecentLabel.setVisibility(hasGoals && !recent.isEmpty() ? View.VISIBLE : View.GONE);
        recentActivityContainer.setVisibility(hasGoals ? View.VISIBLE : View.GONE);

        if (!hasGoals) return;

        tvActiveGoals.setText(String.valueOf(activeCount));
        tvCompleted.setText(String.valueOf(completedCount));
        tvHabits.setText(activeCount + "/" + totalCount);
        float progress = totalCount > 0 ? (float) completedCount / totalCount : 0f;
        circularProgress.setProgress(progress);

        float density = getResources().getDisplayMetrics().density;
        int maxHeightDp = 112;
        for (int i = 0; i < 7; i++) {
            int heightDp = maxBar > 0 ? Math.max(8, (int)((barCounts[i] / (float) maxBar) * maxHeightDp)) : 8;
            android.view.ViewGroup.LayoutParams lp = bars[i].getLayoutParams();
            lp.height = (int)(heightDp * density);
            bars[i].setLayoutParams(lp);
        }
        highlightTodayBar();

        recentActivityContainer.removeAllViews();
        for (Goal goal : recent) recentActivityContainer.addView(buildActivityRow(goal));
    }

    private void highlightTodayBar() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int todayIndex = (dow == Calendar.SUNDAY) ? 6 : dow - 2;
        int primary = getResources().getColor(R.color.primary,   null);
        int hint    = getResources().getColor(R.color.text_hint, null);
        for (int i = 0; i < bars.length; i++) {
            boolean isToday = (i == todayIndex);
            bars[i].setBackgroundResource(isToday ? R.drawable.bg_bar_active : R.drawable.bg_bar_inactive_dark);
            if (dayLabels[i] != null) {
                dayLabels[i].setTextColor(isToday ? primary : hint);
                dayLabels[i].setTypeface(null,
                        isToday ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            }
        }
    }

    // ── Goals tab data ─────────────────────────────────────────────────────

    private void loadGoalsData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<Goal> all = goalDao.getGoalsForUser(userId);
            List<Goal> filtered = new ArrayList<>();
            for (Goal g : all) {
                if      (currentGoalFilter.equals("all"))       filtered.add(g);
                else if (currentGoalFilter.equals("active")    && g.isAchieved == 0) filtered.add(g);
                else if (currentGoalFilter.equals("completed") && g.isAchieved == 1) filtered.add(g);
            }
            runOnUiThread(() -> renderGoals(filtered));
        });
    }

    private void applyGoalsFilterUI() {
        int white    = getResources().getColor(R.color.white,            null);
        int inactive = getResources().getColor(R.color.colorTextSecondary, null);
        goalsFilterAll.setBackgroundResource(currentGoalFilter.equals("all") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        goalsFilterAll.setTextColor(currentGoalFilter.equals("all") ? white : inactive);
        goalsFilterActive.setBackgroundResource(currentGoalFilter.equals("active") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        goalsFilterActive.setTextColor(currentGoalFilter.equals("active") ? white : inactive);
        goalsFilterCompleted.setBackgroundResource(currentGoalFilter.equals("completed") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        goalsFilterCompleted.setTextColor(currentGoalFilter.equals("completed") ? white : inactive);
    }

    private void renderGoals(List<Goal> goals) {
        goalsContainer.removeAllViews();
        if (goals.isEmpty()) {
            goalsEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        goalsEmptyState.setVisibility(View.GONE);
        float density = getResources().getDisplayMetrics().density;

        for (Goal goal : goals) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card_dark);
            card.setElevation(2 * density);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, (int)(12 * density));
            card.setLayoutParams(cardLp);
            int pad = (int)(16 * density);
            card.setPadding(pad, pad, pad, pad);
            card.setClickable(true);
            card.setFocusable(true);

            // Row: icon + text + badge
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            // Icon box
            FrameLayout iconBox = new FrameLayout(this);
            int iconSize = (int)(48 * density);
            LinearLayout.LayoutParams ibLp = new LinearLayout.LayoutParams(iconSize, iconSize);
            ibLp.setMarginEnd((int)(12 * density));
            iconBox.setLayoutParams(ibLp);
            iconBox.setBackgroundResource(R.drawable.bg_circle_blue);
            ImageView icon = new ImageView(this);
            FrameLayout.LayoutParams iLp = new FrameLayout.LayoutParams(
                    (int)(24 * density), (int)(24 * density));
            iLp.gravity = android.view.Gravity.CENTER;
            icon.setLayoutParams(iLp);
            icon.setImageResource(R.drawable.ic_flag);
            icon.setColorFilter(getResources().getColor(R.color.colorBlueIcon, null));
            iconBox.addView(icon);
            row.addView(iconBox);

            // Title + deadline
            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            TextView tvTitle = new TextView(this);
            tvTitle.setText(goal.title);
            tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            tvTitle.setTextSize(15f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvTitle);
            TextView tvSub = new TextView(this);
            tvSub.setText(goal.deadline != null ? "Deadline: " + goal.deadline : (goal.category != null ? goal.category : "No deadline"));
            tvSub.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            tvSub.setTextSize(12f);
            textCol.addView(tvSub);
            row.addView(textCol);

            // Status badge
            TextView badge = new TextView(this);
            badge.setText(goal.isAchieved == 1 ? "Done" : "Active");
            badge.setTextSize(11f);
            badge.setTypeface(null, android.graphics.Typeface.BOLD);
            badge.setTextColor(goal.isAchieved == 1
                    ? getResources().getColor(R.color.colorGreenIcon, null)
                    : getResources().getColor(R.color.primary, null));
            badge.setBackgroundResource(goal.isAchieved == 1
                    ? R.drawable.bg_circle_green : R.drawable.bg_badge_primary);
            badge.setPadding((int)(8 * density), (int)(3 * density), (int)(8 * density), (int)(3 * density));
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            badgeLp.gravity = android.view.Gravity.CENTER_VERTICAL;
            badge.setLayoutParams(badgeLp);
            row.addView(badge);
            card.addView(row);

            // Progress bar section
            LinearLayout progressSection = new LinearLayout(this);
            progressSection.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams psLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            psLp.setMargins(0, (int)(12 * density), 0, 0);
            progressSection.setLayoutParams(psLp);

            LinearLayout pLabels = new LinearLayout(this);
            pLabels.setOrientation(LinearLayout.HORIZONTAL);
            pLabels.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView tvPLabel = new TextView(this);
            tvPLabel.setText("Progress");
            tvPLabel.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            tvPLabel.setTextSize(11f);
            tvPLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            pLabels.addView(tvPLabel);
            int pct = goal.isAchieved == 1 ? 100 : 0;
            TextView tvPct = new TextView(this);
            tvPct.setText(pct + "%");
            tvPct.setTextColor(getResources().getColor(R.color.primary, null));
            tvPct.setTextSize(11f);
            tvPct.setTypeface(null, android.graphics.Typeface.BOLD);
            pLabels.addView(tvPct);
            progressSection.addView(pLabels);

            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setProgressDrawable(getDrawable(R.drawable.bg_bar_active));
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int)(6 * density));
            pbLp.setMargins(0, (int)(6 * density), 0, 0);
            pb.setLayoutParams(pbLp);
            progressSection.addView(pb);
            card.addView(progressSection);

            // Click → GoalDetailsActivity
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, GoalDetailsActivity.class);
                intent.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
                goalLauncher.launch(intent);
            });

            goalsContainer.addView(card);
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    private void setupNavigation() {
        // Home tab
        findViewById(R.id.nav_home).setOnClickListener(v -> switchTab("home"));

        // Goals tab — switch in-app
        findViewById(R.id.nav_goals).setOnClickListener(v -> switchTab("goals"));

        // FAB + → AddGoalActivity
        findViewById(R.id.nav_add).setOnClickListener(v ->
                goalLauncher.launch(new Intent(this, AddGoalActivity.class)));

        // Empty state add first goal
        findViewById(R.id.btn_add_first_goal).setOnClickListener(v ->
                goalLauncher.launch(new Intent(this, AddGoalActivity.class)));

        // "View All" in progress section → switch to Goals tab
        findViewById(R.id.btn_view_all_goals).setOnClickListener(v -> switchTab("goals"));

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

        // Block back — when on Goals tab go back to Home, else block
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTab.equals("goals")) {
                    switchTab("home");
                }
                // else blocked — user must log out explicitly
            }
        });
    }

    // ── Build home activity row ────────────────────────────────────────────

    private View buildActivityRow(Goal goal) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card_dark);
        row.setClickable(true);
        row.setFocusable(true);
        float density = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, (int)(10 * density));
        row.setLayoutParams(rowParams);
        int pad = (int)(14 * density);
        row.setPadding(pad, pad, pad, pad);

        FrameLayout iconBg = new FrameLayout(this);
        int size = (int)(40 * density);
        iconBg.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        iconBg.setBackgroundResource(goal.isAchieved == 1 ? R.drawable.bg_circle_green : R.drawable.bg_circle_blue);
        ImageView icon = new ImageView(this);
        FrameLayout.LayoutParams iParams = new FrameLayout.LayoutParams((int)(20 * density), (int)(20 * density));
        iParams.gravity = android.view.Gravity.CENTER;
        icon.setLayoutParams(iParams);
        icon.setImageResource(goal.isAchieved == 1 ? R.drawable.ic_check_circle : R.drawable.ic_flag);
        icon.setColorFilter(getResources().getColor(goal.isAchieved == 1 ? R.color.colorGreenIcon : R.color.colorBlueIcon, null));
        iconBg.addView(icon);
        row.addView(iconBg);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
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

        ImageView arrow = new ImageView(this);
        int arrowSize = (int)(18 * density);
        LinearLayout.LayoutParams aParams = new LinearLayout.LayoutParams(arrowSize, arrowSize);
        aParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        arrow.setLayoutParams(aParams);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(getResources().getColor(R.color.text_hint, null));
        row.addView(arrow);

        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, GoalDetailsActivity.class);
            intent.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
            goalLauncher.launch(intent);
        });
        return row;
    }

    // ── Notification permission ────────────────────────────────────────────

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

