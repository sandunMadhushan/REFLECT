package me.madhushan.reflect;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Habit;
import me.madhushan.reflect.database.HabitCompletion;
import me.madhushan.reflect.database.HabitCompletionDao;
import me.madhushan.reflect.database.HabitDao;
import me.madhushan.reflect.utils.SessionManager;

public class HabitTrackerActivity extends AppCompatActivity {

    private HabitDao habitDao;
    private HabitCompletionDao completionDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout habitsListContainer;
    private LinearLayout emptyHabitsState;
    private LinearLayout calendarStrip;
    private TextView tvStreakCount;
    private TextView tvCompletionRate;

    private String selectedDate;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final ActivityResultLauncher<Intent> addHabitLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) loadHabits();
            });

    private final ActivityResultLauncher<Intent> editHabitLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) loadHabits();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        habitDao     = AppDatabase.getInstance(this).habitDao();
        completionDao = AppDatabase.getInstance(this).habitCompletionDao();
        sessionManager = new SessionManager(this);
        executor     = Executors.newSingleThreadExecutor();

        habitsListContainer = findViewById(R.id.habits_list_container);
        emptyHabitsState    = findViewById(R.id.empty_habits_state);
        calendarStrip       = findViewById(R.id.calendar_strip);
        tvStreakCount       = findViewById(R.id.tv_streak_count);
        tvCompletionRate    = findViewById(R.id.tv_completion_rate);

        selectedDate = sdf.format(Calendar.getInstance().getTime());

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_habit).setOnClickListener(v ->
                addHabitLauncher.launch(new Intent(this, AddHabitActivity.class)));
        findViewById(R.id.btn_manage).setOnClickListener(v ->
                Toast.makeText(this, "Tap a habit to edit · Long-press to delete", Toast.LENGTH_SHORT).show());

        buildCalendarStrip();
        loadHabits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }

    // ─── Calendar strip (7 days centred on today) ──────────────────────────

    private void buildCalendarStrip() {
        calendarStrip.removeAllViews();
        Calendar cal = Calendar.getInstance();
        // Show 3 days before, today, 3 days after
        cal.add(Calendar.DAY_OF_YEAR, -3);

        String[] dayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        String todayStr = sdf.format(Calendar.getInstance().getTime());
        float dp = getResources().getDisplayMetrics().density;

        int colorTextPrimary   = getResources().getColor(R.color.colorTextPrimary, getTheme());
        int colorTextSecondary = getResources().getColor(R.color.colorTextSecondary, getTheme());
        int colorPrimary       = getResources().getColor(R.color.primary, getTheme());

        for (int i = 0; i < 7; i++) {
            String dateStr = sdf.format(cal.getTime());
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String dayLabel = dayLabels[dayOfWeek - 1];
            int dayNum = cal.get(Calendar.DAY_OF_MONTH);
            boolean isToday    = dateStr.equals(todayStr);
            boolean isSelected = dateStr.equals(selectedDate);

            LinearLayout dayView = new LinearLayout(this);
            dayView.setOrientation(LinearLayout.VERTICAL);
            dayView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)(60 * dp), (int)(80 * dp));
            lp.setMarginEnd((int)(6 * dp));
            dayView.setLayoutParams(lp);
            if (isSelected) {
                dayView.setBackground(androidx.appcompat.content.res.AppCompatResources
                        .getDrawable(this, R.drawable.bg_cal_day_selected));
            }
            dayView.setClickable(true);
            dayView.setFocusable(true);

            // Day label
            TextView tvLabel = new TextView(this);
            tvLabel.setText(dayLabel);
            tvLabel.setTextSize(11f);
            tvLabel.setGravity(Gravity.CENTER);
            tvLabel.setTypeface(null, Typeface.BOLD);
            tvLabel.setTextColor((isSelected || isToday) ? colorTextPrimary : colorTextSecondary);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.setMargins(0, 0, 0, (int)(4 * dp));
            tvLabel.setLayoutParams(tlp);
            dayView.addView(tvLabel);

            // Day number circle
            FrameLayout numBg = new FrameLayout(this);
            int circleSize = (int)(44 * dp);
            numBg.setLayoutParams(new LinearLayout.LayoutParams(circleSize, circleSize));
            if (isToday) {
                numBg.setBackground(androidx.appcompat.content.res.AppCompatResources
                        .getDrawable(this, R.drawable.bg_cal_day_today));
            } else {
                numBg.setBackground(androidx.appcompat.content.res.AppCompatResources
                        .getDrawable(this, R.drawable.bg_calendar_card));
            }

            TextView tvNum = new TextView(this);
            tvNum.setText(String.valueOf(dayNum));
            tvNum.setTextSize(16f);
            tvNum.setGravity(Gravity.CENTER);
            tvNum.setTypeface(null, Typeface.BOLD);
            tvNum.setTextColor(isToday ? Color.WHITE : (isSelected ? colorPrimary : colorTextSecondary));
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            flp.gravity = Gravity.CENTER;
            tvNum.setLayoutParams(flp);
            numBg.addView(tvNum);
            dayView.addView(numBg);

            // Dot below for today
            if (isToday) {
                View dot = new View(this);
                LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(
                        (int)(6 * dp), (int)(6 * dp));
                dotLp.setMargins(0, (int)(4 * dp), 0, 0);
                dotLp.gravity = Gravity.CENTER_HORIZONTAL;
                dot.setLayoutParams(dotLp);
                dot.setBackground(androidx.appcompat.content.res.AppCompatResources
                        .getDrawable(this, R.drawable.bg_day_chip_active));
                dayView.addView(dot);
            }

            final String clickDate = dateStr;
            dayView.setOnClickListener(v -> {
                selectedDate = clickDate;
                buildCalendarStrip();
                loadHabits();
            });

            calendarStrip.addView(dayView);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    // ─── Load & render habits ───────────────────────────────────────────────

    public void loadHabits() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<Habit> habits = habitDao.getHabitsForUser(userId);
            int totalHabits = habits.size();

            // Count completed today
            int completedToday = 0;
            for (Habit h : habits) {
                HabitCompletion c = completionDao.getCompletionForDate(h.id, selectedDate);
                if (c != null) completedToday++;
            }
            final int finalCompleted = completedToday;

            // Overall streak: max streak across all habits
            int maxStreak = 0;
            for (Habit h : habits) {
                if (h.streakCount > maxStreak) maxStreak = h.streakCount;
            }
            final int finalMaxStreak = maxStreak;

            final int completionPct = totalHabits > 0 ?
                    (int) Math.round((completedToday * 100.0) / totalHabits) : 0;

            runOnUiThread(() -> renderHabits(habits, finalCompleted, totalHabits, finalMaxStreak, completionPct));
        });
    }

    private void renderHabits(List<Habit> habits, int completedToday, int totalHabits,
                               int maxStreak, int completionPct) {
        tvStreakCount.setText(maxStreak + " Days");
        tvCompletionRate.setText(completionPct + "%");

        habitsListContainer.removeAllViews();

        if (habits.isEmpty()) {
            emptyHabitsState.setVisibility(View.VISIBLE);
            return;
        }
        emptyHabitsState.setVisibility(View.GONE);

        for (Habit habit : habits) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_habit, habitsListContainer, false);

            FrameLayout iconBg   = itemView.findViewById(R.id.habit_icon_bg);
            ImageView   icon     = itemView.findViewById(R.id.habit_icon);
            TextView    tvTitle  = itemView.findViewById(R.id.tv_habit_title);
            TextView    tvDesc   = itemView.findViewById(R.id.tv_habit_description);
            TextView    tvStreak = itemView.findViewById(R.id.tv_streak);
            SwitchCompat toggle  = itemView.findViewById(R.id.habit_toggle);

            tvTitle.setText(habit.title != null ? habit.title : "");
            tvDesc.setText(habit.description != null ? habit.description : "");
            tvStreak.setText(String.valueOf(habit.streakCount));

            // Apply icon color and icon independently
            applyIconStyle(iconBg, icon, habit.iconColor, habit.iconName);

            // Set toggle state based on completion for selected date
            executor.execute(() -> {
                HabitCompletion existing = completionDao.getCompletionForDate(habit.id, selectedDate);
                runOnUiThread(() -> {
                    toggle.setOnCheckedChangeListener(null);
                    toggle.setChecked(existing != null);
                    toggle.setOnCheckedChangeListener((btn, isChecked) ->
                            onHabitToggled(habit, isChecked));
                });
            });

            // Toggle should NOT trigger item click
            toggle.setClickable(true);
            toggle.setFocusable(true);

            // Tap item (non-toggle area) → Edit
            itemView.setOnClickListener(v -> openEditHabit(habit));

            // Long press → Edit / Delete options dialog
            itemView.setOnLongClickListener(v -> {
                showOptionsDialog(habit);
                return true;
            });

            habitsListContainer.addView(itemView);
        }
    }

    private void applyIconStyle(FrameLayout bg, ImageView icon, String color, String iconName) {
        // ── Background color ──────────────────────────────────────────────
        int bgDrawable;
        int tintColor;
        switch (color == null ? "indigo" : color) {
            case "emerald":
                bgDrawable = R.drawable.bg_habit_icon_emerald;
                tintColor  = 0xFF10B981;
                break;
            case "pink":
                bgDrawable = R.drawable.bg_habit_icon_pink;
                tintColor  = 0xFFEC4899;
                break;
            case "orange":
                bgDrawable = R.drawable.bg_habit_icon_orange;
                tintColor  = 0xFFF97316;
                break;
            case "primary":
                bgDrawable = R.drawable.bg_habit_icon_primary;
                tintColor  = getResources().getColor(R.color.primary, getTheme());
                break;
            default: // indigo
                bgDrawable = R.drawable.bg_habit_icon_indigo;
                tintColor  = 0xFF6366F1;
                break;
        }
        bg.setBackground(androidx.appcompat.content.res.AppCompatResources.getDrawable(this, bgDrawable));
        icon.setColorFilter(tintColor);

        // ── Icon drawable (independent of color) ──────────────────────────
        icon.setImageResource(getIconDrawable(iconName));
    }

    private int getIconDrawable(String iconName) {
        if (iconName == null) return R.drawable.ic_self_improvement;
        switch (iconName) {
            case "water_drop":      return R.drawable.ic_water_drop;
            case "fitness_center":  return R.drawable.ic_fitness_center;
            case "menu_book":       return R.drawable.ic_menu_book;
            case "favorite":        return R.drawable.ic_favorite;
            case "mindfulness":     return R.drawable.ic_mindfulness;
            case "psychology":      return R.drawable.ic_psychology;
            case "calendar":        return R.drawable.ic_calendar;
            case "check_circle":    return R.drawable.ic_check_circle;
            case "journal":         return R.drawable.ic_journal;
            default:                return R.drawable.ic_self_improvement;
        }
    }

    private void onHabitToggled(Habit habit, boolean isChecked) {
        executor.execute(() -> {
            if (isChecked) {
                // Add completion
                HabitCompletion completion = new HabitCompletion();
                completion.habitId = habit.id;
                completion.completedDate = selectedDate;
                completionDao.insertCompletion(completion);
                // Recalculate streak
                int streak = calculateStreak(habit.id);
                habitDao.updateStreak(habit.id, streak, selectedDate);
            } else {
                // Remove completion
                completionDao.deleteCompletionForDate(habit.id, selectedDate);
                int streak = calculateStreak(habit.id);
                habitDao.updateStreak(habit.id, streak, selectedDate);
            }
            // Refresh stats
            List<Habit> habits = habitDao.getHabitsForUser(sessionManager.getUserId());
            int total = habits.size();
            int completed = completionDao.getCompletedCountForUserOnDate(sessionManager.getUserId(), selectedDate);
            int pct = total > 0 ? (int) Math.round((completed * 100.0) / total) : 0;
            int maxStreak = 0;
            for (Habit h : habits) if (h.streakCount > maxStreak) maxStreak = h.streakCount;
            final int finalMax = maxStreak;
            final int finalPct = pct;
            runOnUiThread(() -> {
                tvStreakCount.setText(finalMax + " Days");
                tvCompletionRate.setText(finalPct + "%");
            });
        });
    }

    private int calculateStreak(int habitId) {
        List<String> dates = completionDao.getAllCompletionDates(habitId);
        if (dates == null || dates.isEmpty()) return 0;

        Calendar cal = Calendar.getInstance();
        // Start from today or selected date, count backward
        try {
            cal.setTime(sdf.parse(selectedDate));
        } catch (Exception e) {
            cal = Calendar.getInstance();
        }

        int streak = 0;
        for (int i = 0; i < dates.size(); i++) {
            String expected = sdf.format(cal.getTime());
            if (dates.contains(expected)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    private void openEditHabit(Habit habit) {
        Intent intent = new Intent(this, AddHabitActivity.class);
        intent.putExtra(AddHabitActivity.EXTRA_HABIT_ID, habit.id);
        editHabitLauncher.launch(intent);
    }

    private void showOptionsDialog(Habit habit) {
        String[] options = {"✏️  Edit Habit", "🗑️  Delete Habit"};
        new AlertDialog.Builder(this)
                .setTitle(habit.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openEditHabit(habit);
                    } else {
                        showDeleteDialog(habit);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Habit habit) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete \"" + habit.title + "\"? This will also remove all completion history.")
                .setPositiveButton("Delete", (d, w) -> {
                    executor.execute(() -> {
                        habitDao.deleteHabit(habit);
                        runOnUiThread(this::loadHabits);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}











