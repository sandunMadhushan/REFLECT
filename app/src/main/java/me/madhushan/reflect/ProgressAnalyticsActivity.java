package me.madhushan.reflect;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
import me.madhushan.reflect.database.Habit;
import me.madhushan.reflect.database.HabitCompletionDao;
import me.madhushan.reflect.database.HabitDao;
import me.madhushan.reflect.database.Reflection;
import me.madhushan.reflect.database.ReflectionDao;
import me.madhushan.reflect.ui.DonutChartView;
import me.madhushan.reflect.utils.SessionManager;

public class ProgressAnalyticsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private GoalDao goalDao;
    private HabitDao habitDao;
    private HabitCompletionDao habitCompletionDao;
    private ReflectionDao reflectionDao;
    private ExecutorService executor;

    // Filter state: "week" | "month" | "all"
    private String currentFilter = "week";

    // Bar chart day selection
    private int selectedDayIndex = -1; // -1 = none selected
    private final String[] weekDates = new String[7]; // Mon–Sun "yyyy-MM-dd"
    private final String[] fullDayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    // UI references
    private TextView tvHabitPct, tvHabitTrend, tvTotalGoals, tvActiveGoals,
            tvCompletedGoals, tvBestStreak, tvReflectionStreak,
            tvReflectionSubtitle, tvTotalReflections;
    private LinearLayout barChartContainer, dayLabelsContainer,
            heatmapContainer, legendContainer, categoryBarsContainer;
    private LinearLayout dayDetailPanel, dayDetailContainer;
    private TextView tvDayDetailTitle;
    private DonutChartView donutChart;
    private TextView chipWeek, chipMonth, chipAll;

    // Category data
    private static final String[] CAT_NAMES   = {"Personal Growth", "Health & Fitness", "Career & Finance", "Relationships"};
    private static final int[]    CAT_COLORS  = {0xFF4e51e9, 0xFF10B981, 0xFFF97316, 0xFFEC4899};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_analytics);

        sessionManager    = new SessionManager(this);
        goalDao           = AppDatabase.getInstance(this).goalDao();
        habitDao          = AppDatabase.getInstance(this).habitDao();
        habitCompletionDao= AppDatabase.getInstance(this).habitCompletionDao();
        reflectionDao     = AppDatabase.getInstance(this).reflectionDao();
        executor          = Executors.newSingleThreadExecutor();

        // Bind views
        tvHabitPct           = findViewById(R.id.tv_habit_pct);
        tvHabitTrend         = findViewById(R.id.tv_habit_trend);
        tvTotalGoals         = findViewById(R.id.tv_total_goals);
        tvActiveGoals        = findViewById(R.id.tv_active_goals);
        tvCompletedGoals     = findViewById(R.id.tv_completed_goals);
        tvBestStreak         = findViewById(R.id.tv_best_streak);
        tvReflectionStreak   = findViewById(R.id.tv_reflection_streak);
        tvReflectionSubtitle = findViewById(R.id.tv_reflection_subtitle);
        tvTotalReflections   = findViewById(R.id.tv_total_reflections);
        barChartContainer    = findViewById(R.id.bar_chart_container);
        dayLabelsContainer   = findViewById(R.id.day_labels_container);
        heatmapContainer     = findViewById(R.id.heatmap_container);
        legendContainer      = findViewById(R.id.legend_container);
        categoryBarsContainer= findViewById(R.id.category_bars_container);
        dayDetailPanel       = findViewById(R.id.day_detail_panel);
        dayDetailContainer   = findViewById(R.id.day_detail_container);
        tvDayDetailTitle     = findViewById(R.id.tv_day_detail_title);
        donutChart           = findViewById(R.id.donut_chart);
        chipWeek             = findViewById(R.id.chip_week);
        chipMonth            = findViewById(R.id.chip_month);
        chipAll              = findViewById(R.id.chip_all);

        // Build Mon–Sun date strings for this week
        buildWeekDates();

        // Default selected day = today
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        selectedDayIndex = (dow == Calendar.SUNDAY) ? 6 : dow - 2;

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_close_day_detail).setOnClickListener(v -> {
            selectedDayIndex = -1;
            dayDetailPanel.setVisibility(View.GONE);
            highlightSelectedBar();
        });

        // Filter chip listeners
        chipWeek.setOnClickListener(v  -> setFilter("week"));
        chipMonth.setOnClickListener(v -> setFilter("month"));
        chipAll.setOnClickListener(v   -> setFilter("all"));

        loadData();
    }

    private void buildWeekDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_YEAR, -(dow == Calendar.SUNDAY ? 6 : dow - 2));
        for (int i = 0; i < 7; i++) {
            weekDates[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void highlightSelectedBar() {
        int primaryColor = getResources().getColor(R.color.primary, null);
        int trackColor   = getResources().getColor(R.color.colorBarInactive, null);
        int hintColor    = getResources().getColor(R.color.text_hint, null);
        // barChartContainer children are column LinearLayouts, each with a FrameLayout track
        for (int i = 0; i < barChartContainer.getChildCount(); i++) {
            LinearLayout col = (LinearLayout) barChartContainer.getChildAt(i);
            FrameLayout track = (FrameLayout) col.getChildAt(0);
            View fill = track.getChildAt(0);
            boolean selected = (i == selectedDayIndex);
            fill.setBackgroundColor(selected ? primaryColor
                    : (primaryColor & 0x00FFFFFF | 0x55000000));
            track.setBackgroundColor(selected
                    ? getResources().getColor(R.color.colorProgressTrack, null)
                    : trackColor);
        }
        // Update day labels
        for (int i = 0; i < dayLabelsContainer.getChildCount(); i++) {
            TextView lbl = (TextView) dayLabelsContainer.getChildAt(i);
            boolean selected = (i == selectedDayIndex);
            lbl.setTextColor(selected ? primaryColor : hintColor);
            lbl.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void loadDayDetail(int dayIdx) {
        String date = weekDates[dayIdx];
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        String label = date.equals(today) ? "Today" : fullDayNames[dayIdx];

        // Format date for display: "Mon, Mar 9"
        String displayDate = date;
        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            if (d != null) displayDate = new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d);
        } catch (Exception ignored) {}

        tvDayDetailTitle.setText(label + "  ·  " + displayDate);
        dayDetailPanel.setVisibility(View.VISIBLE);

        int userId = sessionManager.getUserId();
        final String dateFinal = date;
        executor.execute(() -> {
            List<Habit>      habits      = habitCompletionDao.getHabitsCompletedOnDate(userId, dateFinal);
            List<Reflection> reflections = reflectionDao.getReflectionsForDate(userId, dateFinal);
            List<Goal>       goals       = goalDao.getGoalsForDate(userId, dateFinal);

            runOnUiThread(() -> {
                dayDetailContainer.removeAllViews();
                float dp = getResources().getDisplayMetrics().density;
                boolean hasAny = !habits.isEmpty() || !reflections.isEmpty() || !goals.isEmpty();

                if (!hasAny) {
                    TextView empty = new TextView(this);
                    empty.setText("Nothing recorded on " + label + ".");
                    empty.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
                    empty.setTextSize(13f);
                    LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    ep.setMargins(0, 0, 0, (int)(4*dp));
                    empty.setLayoutParams(ep);
                    dayDetailContainer.addView(empty);
                    return;
                }

                // Section: Habits completed
                if (!habits.isEmpty()) {
                    addDetailSectionHeader("🔥 Habits Completed", habits.size() + " / " + habitDao.getTotalHabitsCount(userId), dp);
                    for (Habit h : habits) addDetailRow(
                            R.drawable.bg_habit_icon_emerald, R.drawable.ic_check_circle,
                            R.color.colorGreenIcon,
                            h.title != null ? h.title : "Habit",
                            "Streak: " + h.streakCount + " days", dp);
                }

                // Section: Goals updated
                if (!goals.isEmpty()) {
                    addDetailSectionHeader("🎯 Goals Updated", String.valueOf(goals.size()), dp);
                    for (Goal g : goals) addDetailRow(
                            g.isAchieved == 1 ? R.drawable.bg_circle_green : R.drawable.bg_circle_blue,
                            g.isAchieved == 1 ? R.drawable.ic_check_circle : R.drawable.ic_flag,
                            g.isAchieved == 1 ? R.color.colorGreenIcon : R.color.colorBlueIcon,
                            g.title != null ? g.title : "Goal",
                            g.isAchieved == 1 ? "Achieved ✅" : "In Progress", dp);
                }

                // Section: Reflections written
                if (!reflections.isEmpty()) {
                    addDetailSectionHeader("📝 Reflections Written", String.valueOf(reflections.size()), dp);
                    for (Reflection r : reflections) {
                        String moodLabel = r.mood != null
                                ? Character.toUpperCase(r.mood.charAt(0)) + r.mood.substring(1)
                                : "Neutral";
                        addDetailRow(
                                R.drawable.bg_circle_purple, R.drawable.ic_journal,
                                R.color.primary,
                                r.title != null ? r.title : "Reflection",
                                "Mood: " + moodLabel, dp);
                    }
                }
            });
        });
    }

    private void addDetailSectionHeader(String title, String count, float dp) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0, (int)(8*dp), 0, (int)(6*dp));
        row.setLayoutParams(rp);

        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tv.setText(title);
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        tv.setTextSize(12f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tv);

        TextView badge = new TextView(this);
        badge.setText(count);
        badge.setTextColor(getResources().getColor(R.color.primary, null));
        badge.setTextSize(11f);
        badge.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(badge);

        dayDetailContainer.addView(row);
    }

    private void addDetailRow(int bgRes, int iconRes, int tintColorRes, String title, String sub, float dp) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0, 0, 0, (int)(6*dp));
        row.setLayoutParams(rp);
        row.setBackgroundResource(R.drawable.bg_analytics_card);
        row.setPadding((int)(10*dp), (int)(10*dp), (int)(10*dp), (int)(10*dp));

        FrameLayout iconBox = new FrameLayout(this);
        int sz = (int)(34*dp);
        iconBox.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
        iconBox.setBackgroundResource(bgRes);

        ImageView icon = new ImageView(this);
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams((int)(16*dp), (int)(16*dp));
        fp.gravity = Gravity.CENTER;
        icon.setLayoutParams(fp);
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(tintColorRes, null));
        iconBox.addView(icon);
        row.addView(iconBox);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cp.setMarginStart((int)(10*dp));
        col.setLayoutParams(cp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        tvTitle.setTextSize(12f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setMaxLines(1);
        tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        col.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(sub);
        tvSub.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        tvSub.setTextSize(11f);
        col.addView(tvSub);

        row.addView(col);
        dayDetailContainer.addView(row);
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        // Reset day selection when changing time filter
        selectedDayIndex = -1;
        dayDetailPanel.setVisibility(View.GONE);

        int secondary = getResources().getColor(R.color.colorTextSecondary, null);
        int white     = getResources().getColor(R.color.white, null);

        // Reset all chips
        for (TextView chip : new TextView[]{chipWeek, chipMonth, chipAll}) {
            chip.setBackgroundResource(R.drawable.bg_filter_chip_inactive);
            chip.setTextColor(secondary);
        }
        // Activate selected
        TextView active = filter.equals("week") ? chipWeek : filter.equals("month") ? chipMonth : chipAll;
        active.setBackgroundResource(R.drawable.bg_filter_chip_active);
        active.setTextColor(white);

        loadData();
    }

    private void loadData() {
        int userId = sessionManager.getUserId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(Calendar.getInstance().getTime());

        // Compute date range start based on filter
        Calendar rangeStart = Calendar.getInstance();
        if (currentFilter.equals("week")) {
            rangeStart.add(Calendar.DAY_OF_YEAR, -7);
        } else if (currentFilter.equals("month")) {
            rangeStart.add(Calendar.MONTH, -1);
        } else {
            rangeStart.set(2000, 0, 1); // "all time"
        }
        String rangeStartStr = sdf.format(rangeStart.getTime());

        executor.execute(() -> {
            // ── Goals stats ──────────────────────────────────────────────────
            int totalGoals     = goalDao.getTotalGoalsCount(userId);
            int completedGoals = goalDao.getCompletedGoalsCount(userId);
            int activeGoals    = goalDao.getActiveGoalsCount(userId);

            // ── Habit stats ──────────────────────────────────────────────────
            List<Habit> habits    = habitDao.getHabitsForUser(userId);
            int totalHabits       = habits.size();
            int completedToday    = habitCompletionDao.getCompletedCountForUserOnDate(userId, today);
            int habitPct          = totalHabits > 0 ? Math.round(completedToday * 100f / totalHabits) : 0;
            int bestStreak        = 0;
            for (Habit h : habits) if (h.streakCount > bestStreak) bestStreak = h.streakCount;

            // ── Weekly bar data (last 7 days completion %) ───────────────────
            int[] barValues = new int[7]; // 0–100 per day
            Calendar barCal = Calendar.getInstance();
            int dayOfWeek = barCal.get(Calendar.DAY_OF_WEEK);
            barCal.add(Calendar.DAY_OF_YEAR, -(dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2));
            for (int i = 0; i < 7; i++) {
                String d = sdf.format(barCal.getTime());
                int done = habitCompletionDao.getCompletedCountForUserOnDate(userId, d);
                barValues[i] = totalHabits > 0 ? Math.round(done * 100f / totalHabits) : 0;
                barCal.add(Calendar.DAY_OF_YEAR, 1);
            }

            // ── Reflections ──────────────────────────────────────────────────
            int totalReflections = reflectionDao.getTotalReflectionsCount(userId);
            // Reflections in current range
            List<me.madhushan.reflect.database.Reflection> rangeReflections =
                    reflectionDao.getReflectionsFromDate(userId, rangeStartStr);
            int rangeReflCount = rangeReflections != null ? rangeReflections.size() : 0;

            // Heatmap: last 7 days — did user write on that day?
            boolean[] heatmap = new boolean[7];
            Calendar hmCal = Calendar.getInstance();
            hmCal.add(Calendar.DAY_OF_YEAR, -6);
            for (int i = 0; i < 7; i++) {
                String d = sdf.format(hmCal.getTime());
                List<me.madhushan.reflect.database.Reflection> dayRefs =
                        reflectionDao.getReflectionsFromDate(userId, d);
                // Check if any reflection was created exactly on this day
                heatmap[i] = false;
                if (dayRefs != null) {
                    for (me.madhushan.reflect.database.Reflection r : dayRefs) {
                        if (r.createdAt != null && r.createdAt.startsWith(d)) {
                            heatmap[i] = true;
                            break;
                        }
                    }
                }
                hmCal.add(Calendar.DAY_OF_YEAR, 1);
            }

            // ── Category breakdown (count goals by category) ─────────────────
            int[] catCounts = new int[CAT_NAMES.length];
            List<me.madhushan.reflect.database.Goal> allGoals = goalDao.getGoalsForUser(userId);
            int catTotal = 0;
            for (me.madhushan.reflect.database.Goal g : allGoals) {
                for (int i = 0; i < CAT_NAMES.length; i++) {
                    if (CAT_NAMES[i].equals(g.category)) {
                        catCounts[i]++;
                        catTotal++;
                        break;
                    }
                }
            }
            final int finalCatTotal = catTotal;

            // Final copies for UI thread
            final int fHabitPct       = habitPct;
            final int fTotalGoals     = totalGoals;
            final int fActiveGoals    = activeGoals;
            final int fCompletedGoals = completedGoals;
            final int fBestStreak     = bestStreak;
            final int fTotalRef       = totalReflections;
            final int fRangeRef       = rangeReflCount;
            final int[] fBars         = barValues;
            final boolean[] fHeatmap  = heatmap;
            final int[] fCatCounts    = catCounts;

            runOnUiThread(() -> updateUI(fHabitPct, fTotalGoals, fActiveGoals, fCompletedGoals,
                    fBestStreak, fTotalRef, fRangeRef, fBars, fHeatmap, fCatCounts, finalCatTotal));
        });
    }

    private void updateUI(int habitPct, int totalGoals, int activeGoals, int completedGoals,
                          int bestStreak, int totalRef, int rangeRef,
                          int[] bars, boolean[] heatmap, int[] catCounts, int catTotal) {
        float dp = getResources().getDisplayMetrics().density;

        // ── Stat cards ──────────────────────────────────────────────────────
        tvHabitPct.setText(habitPct + "%");
        tvHabitTrend.setText("+" + habitPct + "%");
        tvTotalGoals.setText(String.valueOf(totalGoals));
        tvActiveGoals.setText(String.valueOf(activeGoals));
        tvCompletedGoals.setText(String.valueOf(completedGoals));
        tvBestStreak.setText(bestStreak + " days");
        tvTotalReflections.setText(String.valueOf(totalRef));

        String rangeLabel = currentFilter.equals("week") ? "this week"
                          : currentFilter.equals("month") ? "this month" : "total";
        tvReflectionStreak.setText(rangeRef + " Reflections");
        tvReflectionSubtitle.setText(rangeRef > 0
                ? "You wrote " + rangeRef + " reflections " + rangeLabel + ". Keep it up! 🌟"
                : "No reflections " + rangeLabel + ". Start journaling today!");

        // ── Bar chart ───────────────────────────────────────────────────────
        barChartContainer.removeAllViews();
        dayLabelsContainer.removeAllViews();
        String[] dayLetters = {"M", "T", "W", "T", "F", "S", "S"};
        int primaryColor = getResources().getColor(R.color.primary, null);
        int trackColor   = getResources().getColor(R.color.colorBarInactive, null);

        for (int i = 0; i < 7; i++) {
            boolean isSelected = (i == selectedDayIndex);

            // Bar column — fully clickable
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM);
            col.setClickable(true);
            col.setFocusable(true);
            col.setBackground(ContextCompat.getDrawable(this,
                    android.R.drawable.list_selector_background));
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, (int)(120 * dp), 1f);
            colLp.setMargins((int)(3*dp), 0, (int)(3*dp), 0);
            col.setLayoutParams(colLp);

            // Track (background)
            FrameLayout track = new FrameLayout(this);
            track.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            track.setBackgroundColor(isSelected
                    ? getResources().getColor(R.color.colorProgressTrack, null)
                    : trackColor);

            // Fill
            View fill = new View(this);
            int fillH = Math.max((int)(4*dp), (int)(bars[i] / 100f * 120 * dp));
            FrameLayout.LayoutParams fillLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, fillH);
            fillLp.gravity = Gravity.BOTTOM;
            fill.setLayoutParams(fillLp);
            fill.setBackgroundColor(isSelected ? primaryColor
                    : (primaryColor & 0x00FFFFFF | 0x55000000));

            track.addView(fill);
            col.addView(track);
            barChartContainer.addView(col);

            // Click — select this day
            final int dayIdx = i;
            col.setOnClickListener(v -> {
                if (selectedDayIndex == dayIdx) {
                    // Tap same bar again = close
                    selectedDayIndex = -1;
                    dayDetailPanel.setVisibility(View.GONE);
                } else {
                    selectedDayIndex = dayIdx;
                    loadDayDetail(dayIdx);
                }
                highlightSelectedBar();
            });

            // Day label
            TextView label = new TextView(this);
            label.setText(dayLetters[i]);
            label.setTextSize(11f);
            label.setGravity(Gravity.CENTER);
            label.setTextColor(isSelected ? primaryColor
                    : getResources().getColor(R.color.text_hint, null));
            if (isSelected) label.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            label.setLayoutParams(lblLp);
            dayLabelsContainer.addView(label);
        }

        // Show day detail for initially selected day (today)
        if (selectedDayIndex >= 0) {
            loadDayDetail(selectedDayIndex);
        }

        // ── Heatmap dots ─────────────────────────────────────────────────
        heatmapContainer.removeAllViews();
        for (int i = 0; i < 7; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(
                    0, (int)(8*dp), 1f);
            dotLp.setMargins((int)(2*dp), 0, (int)(2*dp), 0);
            dot.setLayoutParams(dotLp);
            dot.setBackgroundColor(heatmap[i] ? 0xFFFFFFFF : 0x44FFFFFF);
            // Round corners
            dot.setBackground(getRoundedDot(heatmap[i]));
            heatmapContainer.addView(dot);
        }

        // ── Donut chart + legend + category bars ────────────────────────
        List<DonutChartView.Segment> segments = new ArrayList<>();
        legendContainer.removeAllViews();
        categoryBarsContainer.removeAllViews();

        boolean hasAnyGoal = catTotal > 0;
        for (int i = 0; i < CAT_NAMES.length; i++) {
            float pct = hasAnyGoal ? catCounts[i] * 100f / catTotal : 0f;
            if (pct > 0) {
                segments.add(new DonutChartView.Segment(CAT_COLORS[i], pct));
            }

            // Legend item
            LinearLayout legendRow = new LinearLayout(this);
            legendRow.setOrientation(LinearLayout.HORIZONTAL);
            legendRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lrLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lrLp.setMargins(0, 0, 0, (int)(6*dp));
            legendRow.setLayoutParams(lrLp);

            View dot = new View(this);
            dot.setLayoutParams(new LinearLayout.LayoutParams((int)(10*dp), (int)(10*dp)));
            dot.setBackgroundColor(CAT_COLORS[i]);
            applyCircle(dot);

            TextView catName = new TextView(this);
            catName.setText(shortCatName(CAT_NAMES[i]));
            catName.setTextSize(11f);
            catName.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            LinearLayout.LayoutParams cnLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cnLp.setMarginStart((int)(6*dp));
            catName.setLayoutParams(cnLp);

            TextView catPct = new TextView(this);
            catPct.setText(Math.round(pct) + "%");
            catPct.setTextSize(11f);
            catPct.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            catPct.setTypeface(null, android.graphics.Typeface.BOLD);

            legendRow.addView(dot);
            legendRow.addView(catName);
            legendRow.addView(catPct);
            legendContainer.addView(legendRow);

            // Progress bar row
            LinearLayout catRow = new LinearLayout(this);
            catRow.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams crLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            crLp.setMargins(0, 0, 0, (int)(14*dp));
            catRow.setLayoutParams(crLp);

            // Label + percent row
            LinearLayout catHeaderRow = new LinearLayout(this);
            catHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
            catHeaderRow.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            catHeaderRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView catNameBar = new TextView(this);
            catNameBar.setText(CAT_NAMES[i]);
            catNameBar.setTextSize(13f);
            catNameBar.setTypeface(null, android.graphics.Typeface.BOLD);
            catNameBar.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            catNameBar.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView catPctBar = new TextView(this);
            catPctBar.setText(catCounts[i] + " goals");
            catPctBar.setTextSize(12f);
            catPctBar.setTextColor(CAT_COLORS[i]);
            catPctBar.setTypeface(null, android.graphics.Typeface.BOLD);

            catHeaderRow.addView(catNameBar);
            catHeaderRow.addView(catPctBar);
            catRow.addView(catHeaderRow);

            // Track
            FrameLayout trackBar = new FrameLayout(this);
            LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int)(8*dp));
            tbLp.setMargins(0, (int)(6*dp), 0, 0);
            trackBar.setLayoutParams(tbLp);
            trackBar.setBackgroundColor(getResources().getColor(R.color.colorProgressTrack, null));
            applyRound8(trackBar);

            // Fill
            View fillBar = new View(this);
            int fillW = hasAnyGoal ? (int)(pct / 100f * getResources().getDisplayMetrics().widthPixels) : 0;
            FrameLayout.LayoutParams fbLp = new FrameLayout.LayoutParams(
                    fillW == 0 ? (int)(4*dp) : fillW, ViewGroup.LayoutParams.MATCH_PARENT);
            fillBar.setLayoutParams(fbLp);
            fillBar.setBackgroundColor(CAT_COLORS[i]);
            applyRound8(fillBar);

            trackBar.addView(fillBar);
            catRow.addView(trackBar);
            categoryBarsContainer.addView(catRow);
        }

        if (segments.isEmpty()) {
            segments.add(new DonutChartView.Segment(
                    getResources().getColor(R.color.colorProgressTrack, null), 100f));
        }
        donutChart.setSegments(segments);
        donutChart.setStrokeWidthDp(18f);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private android.graphics.drawable.GradientDrawable getRoundedDot(boolean filled) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setCornerRadius(999f);
        gd.setColor(filled ? 0xFFFFFFFF : 0x44FFFFFF);
        return gd;
    }

    private void applyCircle(View v) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        gd.setCornerRadius(999f);
        android.graphics.drawable.Drawable bg = v.getBackground();
        if (bg instanceof android.graphics.drawable.ColorDrawable) {
            gd.setColor(((android.graphics.drawable.ColorDrawable) bg).getColor());
        } else {
            gd.setColor(Color.TRANSPARENT);
        }
        v.setBackground(gd);
    }

    private void applyRound8(View v) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        gd.setCornerRadius(8f * getResources().getDisplayMetrics().density);
        android.graphics.drawable.Drawable bg = v.getBackground();
        if (bg instanceof android.graphics.drawable.ColorDrawable) {
            gd.setColor(((android.graphics.drawable.ColorDrawable) bg).getColor());
        }
        v.setBackground(gd);
    }

    private String shortCatName(String full) {
        if (full.equals("Personal Growth"))  return "Personal";
        if (full.equals("Health & Fitness")) return "Health";
        if (full.equals("Career & Finance")) return "Career";
        return full;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}



