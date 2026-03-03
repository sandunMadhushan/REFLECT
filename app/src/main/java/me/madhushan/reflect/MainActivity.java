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
import me.madhushan.reflect.database.Reflection;
import me.madhushan.reflect.database.ReflectionDao;
import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private GoalDao goalDao;
    private ReflectionDao reflectionDao;
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
    private View tabHome, tabGoals, tabJournal;
    private LinearLayout goalsContainer, goalsEmptyState;
    private TextView goalsFilterAll, goalsFilterActive, goalsFilterCompleted;
    private String currentGoalFilter = "all";

    // ── Journal tab views ─────────────────────────────────────────────────
    private LinearLayout journalEntriesContainer, journalEmptyState;
    private TextView journalFilterAll, journalFilterWeek, journalFilterMonth, journalFilterFavorites;
    private String currentJournalFilter = "all";

    // ── Nav bar items ─────────────────────────────────────────────────────
    private ImageView navHomeIcon, navGoalsIcon, navJournalIcon, navProfileIcon;
    private TextView navHomeLabel, navGoalsLabel, navJournalLabel, navProfileLabel;

    // ── Current tab ───────────────────────────────────────────────────────
    private String currentTab = "home"; // "home", "goals", "journal"

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

    private final ActivityResultLauncher<Intent> journalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadJournalData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        goalDao        = AppDatabase.getInstance(this).goalDao();
        reflectionDao  = AppDatabase.getInstance(this).reflectionDao();
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

        // ── Journal tab views ──────────────────────────────────────────────
        tabJournal              = findViewById(R.id.tab_journal);
        journalEntriesContainer = findViewById(R.id.journal_entries_container);
        journalEmptyState       = findViewById(R.id.journal_empty_state);
        journalFilterAll        = findViewById(R.id.journal_filter_all);
        journalFilterWeek       = findViewById(R.id.journal_filter_week);
        journalFilterMonth      = findViewById(R.id.journal_filter_month);
        journalFilterFavorites  = findViewById(R.id.journal_filter_favorites);

        journalFilterAll.setOnClickListener(v       -> { currentJournalFilter = "all";       applyJournalFilterUI(); loadJournalData(); });
        journalFilterWeek.setOnClickListener(v      -> { currentJournalFilter = "week";      applyJournalFilterUI(); loadJournalData(); });
        journalFilterMonth.setOnClickListener(v     -> { currentJournalFilter = "month";     applyJournalFilterUI(); loadJournalData(); });
        journalFilterFavorites.setOnClickListener(v -> { currentJournalFilter = "favorites"; applyJournalFilterUI(); loadJournalData(); });

        // Journal FAB
        findViewById(R.id.journal_fab).setOnClickListener(v ->
                journalLauncher.launch(new Intent(this, AddReflectionActivity.class)));

        // ── Nav bar items ──────────────────────────────────────────────────
        navHomeIcon    = findViewById(R.id.nav_home_icon);
        navHomeLabel   = findViewById(R.id.nav_home_label);
        navGoalsIcon   = findViewById(R.id.nav_goals_icon);
        navGoalsLabel  = findViewById(R.id.nav_goals_label);
        navJournalIcon  = findViewById(R.id.nav_journal_icon);
        navJournalLabel = findViewById(R.id.nav_journal_label);
        navProfileIcon  = findViewById(R.id.nav_profile_icon);
        navProfileLabel = findViewById(R.id.nav_profile_label);

        setupNavigation();
        requestNotificationPermissionIfNeeded();
        loadHomeData();
        loadGoalsData();
        loadJournalData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeData();
        loadGoalsData();
        loadJournalData();
        AvatarLoader.loadFromSession(this,
                findViewById(R.id.iv_avatar_photo),
                findViewById(R.id.tv_avatar_initials),
                sessionManager);
    }

    // ── Tab switching ──────────────────────────────────────────────────────

    private void switchTab(String tab) {
        currentTab = tab;

        tabHome.setVisibility(tab.equals("home")    ? View.VISIBLE : View.GONE);
        tabGoals.setVisibility(tab.equals("goals")  ? View.VISIBLE : View.GONE);
        tabJournal.setVisibility(tab.equals("journal") ? View.VISIBLE : View.GONE);

        int primary = getResources().getColor(R.color.primary,   null);
        int hint    = getResources().getColor(R.color.text_hint, null);

        navHomeIcon.setColorFilter(tab.equals("home") ? primary : hint);
        navHomeLabel.setTextColor(tab.equals("home") ? primary : hint);
        navHomeLabel.setTypeface(null, tab.equals("home")
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        navGoalsIcon.setColorFilter(tab.equals("goals") ? primary : hint);
        navGoalsLabel.setTextColor(tab.equals("goals") ? primary : hint);
        navGoalsLabel.setTypeface(null, tab.equals("goals")
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        navJournalIcon.setColorFilter(tab.equals("journal") ? primary : hint);
        navJournalLabel.setTextColor(tab.equals("journal") ? primary : hint);
        navJournalLabel.setTypeface(null, tab.equals("journal")
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

        // Journal tab — switch in-app
        findViewById(R.id.nav_journal).setOnClickListener(v -> switchTab("journal"));

        // Profile
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Notification bell
        findViewById(R.id.btn_notifications).setOnClickListener(v ->
                Toast.makeText(this, "Notifications — coming soon", Toast.LENGTH_SHORT).show());

        // Back press — non-home tabs go back to home; home is blocked
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!currentTab.equals("home")) {
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

    // ── Journal tab data ───────────────────────────────────────────────────

    private void loadJournalData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<Reflection> all = reflectionDao.getReflectionsForUser(userId);
            List<Reflection> filtered = new ArrayList<>();

            if (currentJournalFilter.equals("all")) {
                filtered.addAll(all);
            } else if (currentJournalFilter.equals("week")) {
                // Get Monday of current week
                Calendar cal = Calendar.getInstance();
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                int daysFromMon = (dow == Calendar.SUNDAY) ? 6 : dow - 2;
                cal.add(Calendar.DAY_OF_YEAR, -daysFromMon);
                String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                for (Reflection r : all) {
                    if (r.createdAt != null && r.createdAt.compareTo(weekStart) >= 0) filtered.add(r);
                }
            } else if (currentJournalFilter.equals("month")) {
                String monthStart = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
                for (Reflection r : all) {
                    if (r.createdAt != null && r.createdAt.startsWith(monthStart)) filtered.add(r);
                }
            } else if (currentJournalFilter.equals("favorites")) {
                for (Reflection r : all) {
                    if (r.isFavorite == 1) filtered.add(r);
                }
            }

            final List<Reflection> result = filtered;
            runOnUiThread(() -> renderJournalEntries(result));
        });
    }

    private void applyJournalFilterUI() {
        int white    = getResources().getColor(R.color.white,              null);
        int inactive = getResources().getColor(R.color.colorTextSecondary, null);

        journalFilterAll.setBackgroundResource(currentJournalFilter.equals("all")       ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        journalFilterAll.setTextColor(currentJournalFilter.equals("all")                ? white : inactive);
        journalFilterWeek.setBackgroundResource(currentJournalFilter.equals("week")     ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        journalFilterWeek.setTextColor(currentJournalFilter.equals("week")              ? white : inactive);
        journalFilterMonth.setBackgroundResource(currentJournalFilter.equals("month")   ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        journalFilterMonth.setTextColor(currentJournalFilter.equals("month")            ? white : inactive);
        journalFilterFavorites.setBackgroundResource(currentJournalFilter.equals("favorites") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        journalFilterFavorites.setTextColor(currentJournalFilter.equals("favorites")    ? white : inactive);
    }

    private void renderJournalEntries(List<Reflection> entries) {
        journalEntriesContainer.removeAllViews();
        if (entries.isEmpty()) {
            journalEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        journalEmptyState.setVisibility(View.GONE);
        float density = getResources().getDisplayMetrics().density;

        for (Reflection r : entries) {
            // ── Card ──────────────────────────────────────────────────────
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundResource(R.drawable.bg_card_dark);
            card.setElevation(2 * density);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, (int)(12 * density));
            card.setLayoutParams(cardLp);
            int pad = (int)(14 * density);
            card.setPadding(pad, pad, pad, pad);
            card.setClickable(true);
            card.setFocusable(true);

            // ── Mood icon box ─────────────────────────────────────────────
            FrameLayout iconBox = new FrameLayout(this);
            int iconSize = (int)(48 * density);
            LinearLayout.LayoutParams ibLp = new LinearLayout.LayoutParams(iconSize, iconSize);
            ibLp.setMarginEnd((int)(12 * density));
            ibLp.gravity = android.view.Gravity.CENTER_VERTICAL;
            iconBox.setLayoutParams(ibLp);
            iconBox.setBackgroundResource(moodBackground(r.mood));

            TextView emojiTv = new TextView(this);
            FrameLayout.LayoutParams eLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            eLp.gravity = android.view.Gravity.CENTER;
            emojiTv.setLayoutParams(eLp);
            emojiTv.setText(moodEmoji(r.mood));
            emojiTv.setTextSize(22f);
            iconBox.addView(emojiTv);
            card.addView(iconBox);

            // ── Text column ───────────────────────────────────────────────
            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Title row: title + time
            LinearLayout titleRow = new LinearLayout(this);
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tvTitle = new TextView(this);
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvTitle.setText(r.title != null ? r.title : "Reflection");
            tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            tvTitle.setTextSize(14f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setMaxLines(1);
            titleRow.addView(tvTitle);

            TextView tvTime = new TextView(this);
            tvTime.setText(formatTime(r.createdAt));
            tvTime.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            tvTime.setTextSize(11f);
            LinearLayout.LayoutParams timeLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeLp.gravity = android.view.Gravity.CENTER_VERTICAL;
            tvTime.setLayoutParams(timeLp);
            titleRow.addView(tvTime);
            textCol.addView(titleRow);

            // Date (in primary colour)
            TextView tvDate = new TextView(this);
            tvDate.setText(formatDate(r.createdAt));
            tvDate.setTextColor(getResources().getColor(R.color.primary, null));
            tvDate.setTextSize(11f);
            tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateLp.setMargins(0, (int)(2 * density), 0, (int)(4 * density));
            tvDate.setLayoutParams(dateLp);
            textCol.addView(tvDate);

            // Content preview
            if (r.content != null && !r.content.isEmpty()) {
                TextView tvContent = new TextView(this);
                tvContent.setText(r.content);
                tvContent.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
                tvContent.setTextSize(13f);
                tvContent.setMaxLines(2);
                tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);
                textCol.addView(tvContent);
            }

            card.addView(textCol);

            // Favorite star (trailing)
            if (r.isFavorite == 1) {
                TextView starTv = new TextView(this);
                starTv.setText("★");
                starTv.setTextColor(getResources().getColor(R.color.primary, null));
                starTv.setTextSize(18f);
                LinearLayout.LayoutParams starLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                starLp.gravity = android.view.Gravity.CENTER_VERTICAL;
                starLp.setMarginStart((int)(8 * density));
                starTv.setLayoutParams(starLp);
                card.addView(starTv);
            }

            // Long-press to toggle favorite
            card.setOnLongClickListener(v -> {
                executor.execute(() -> {
                    r.isFavorite = (r.isFavorite == 1) ? 0 : 1;
                    reflectionDao.updateReflection(r);
                    runOnUiThread(() -> {
                        String msg = r.isFavorite == 1 ? "Added to favorites" : "Removed from favorites";
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        loadJournalData();
                    });
                });
                return true;
            });

            journalEntriesContainer.addView(card);
        }
    }

    private int moodBackground(String mood) {
        if (mood == null) return R.drawable.bg_circle_blue;
        switch (mood) {
            case "happy":   return R.drawable.bg_circle_green;
            case "anxious": return R.drawable.bg_circle_purple;
            case "neutral":
            case "sad":
            case "calm":
            default:        return R.drawable.bg_circle_blue;
        }
    }

    private String moodEmoji(String mood) {
        if (mood == null) return "😌";
        switch (mood) {
            case "happy":   return "😄";
            case "calm":    return "😌";
            case "neutral": return "😐";
            case "sad":     return "😔";
            case "anxious": return "😰";
            default:        return "😌";
        }
    }

    /** Returns "9:41 AM" from "2026-03-04 09:41:00" */
    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.length() < 16) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return out.format(in.parse(createdAt));
        } catch (Exception e) {
            return "";
        }
    }

    /** Returns "Mar 4" from "2026-03-04 09:41:00" */
    private String formatDate(String createdAt) {
        if (createdAt == null || createdAt.length() < 10) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM d", Locale.getDefault());
            return out.format(in.parse(createdAt.substring(0, 10)));
        } catch (Exception e) {
            return "";
        }
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

