package me.madhushan.reflect;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.database.Habit;
import me.madhushan.reflect.database.HabitCompletionDao;
import me.madhushan.reflect.database.HabitDao;
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

    // UI references
    private TextView tvHabitPct, tvHabitTrend, tvTotalGoals, tvActiveGoals,
            tvCompletedGoals, tvBestStreak, tvReflectionStreak,
            tvReflectionSubtitle, tvTotalReflections;
    private LinearLayout barChartContainer, dayLabelsContainer,
            heatmapContainer, legendContainer, categoryBarsContainer;
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
        donutChart           = findViewById(R.id.donut_chart);
        chipWeek             = findViewById(R.id.chip_week);
        chipMonth            = findViewById(R.id.chip_month);
        chipAll              = findViewById(R.id.chip_all);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Filter chip listeners
        chipWeek.setOnClickListener(v  -> setFilter("week"));
        chipMonth.setOnClickListener(v -> setFilter("month"));
        chipAll.setOnClickListener(v   -> setFilter("all"));

        loadData();
    }

    private void setFilter(String filter) {
        currentFilter = filter;

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
        int todayIdx = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        todayIdx = todayIdx == Calendar.SUNDAY ? 6 : todayIdx - 2;
        int primaryColor = getResources().getColor(R.color.primary, null);
        int trackColor   = getResources().getColor(R.color.colorBarInactive, null);

        for (int i = 0; i < 7; i++) {
            boolean isToday = (i == todayIdx);

            // Bar column
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, (int)(120 * dp), 1f);
            colLp.setMargins((int)(3*dp), 0, (int)(3*dp), 0);
            col.setLayoutParams(colLp);

            // Track (background)
            FrameLayout track = new FrameLayout(this);
            track.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            track.setBackgroundColor(trackColor);

            // Fill
            View fill = new View(this);
            int fillH = Math.max((int)(4*dp), (int)(bars[i] / 100f * 120 * dp));
            FrameLayout.LayoutParams fillLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, fillH);
            fillLp.gravity = Gravity.BOTTOM;
            fill.setLayoutParams(fillLp);
            fill.setBackgroundColor(isToday ? primaryColor : (primaryColor & 0x00FFFFFF | 0x66000000));

            track.addView(fill);
            col.addView(track);
            barChartContainer.addView(col);

            // Day label
            TextView label = new TextView(this);
            label.setText(dayLetters[i]);
            label.setTextSize(11f);
            label.setGravity(Gravity.CENTER);
            label.setTextColor(isToday ? primaryColor
                    : getResources().getColor(R.color.text_hint, null));
            if (isToday) label.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            label.setLayoutParams(lblLp);
            dayLabelsContainer.addView(label);
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



