package me.madhushan.reflect;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Habit;
import me.madhushan.reflect.database.HabitDao;
import me.madhushan.reflect.utils.SessionManager;

public class AddHabitActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";

    private TextInputEditText etHabitName, etHabitDescription;
    private TextView freqDaily, freqWeekly, freqSpecific;
    private TextView tvTitle, tvSaveBtn;
    private TextView[] dayViews;

    private String selectedFrequency = "daily";
    private String selectedColor     = "indigo";
    private String selectedIcon      = "self_improvement"; // independent of color

    private final boolean[] activeDays = {true, true, true, true, true, true, true};

    private HabitDao habitDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    /** null = add mode, non-null = edit mode */
    private Habit habitToEdit = null;

    // Color picker
    private final int[] colorViewIds = {
            R.id.color_indigo, R.id.color_emerald, R.id.color_pink,
            R.id.color_orange, R.id.color_primary
    };
    private final int[] colorCheckIds = {
            R.id.color_indigo_check, R.id.color_emerald_check, R.id.color_pink_check,
            R.id.color_orange_check, R.id.color_primary_check
    };
    private final String[] colorNames = {"indigo", "emerald", "pink", "orange", "primary"};

    // Icon picker — parallel arrays: view IDs, icon name strings, drawable res IDs
    private final int[] iconViewIds = {
            R.id.icon_self_improvement, R.id.icon_water_drop, R.id.icon_fitness_center,
            R.id.icon_menu_book, R.id.icon_favorite, R.id.icon_mindfulness,
            R.id.icon_psychology, R.id.icon_calendar
    };
    private final String[] iconNames = {
            "self_improvement", "water_drop", "fitness_center",
            "menu_book", "favorite", "mindfulness",
            "psychology", "calendar"
    };
    private final int[] iconDrawables = {
            R.drawable.ic_self_improvement, R.drawable.ic_water_drop, R.drawable.ic_fitness_center,
            R.drawable.ic_menu_book, R.drawable.ic_favorite, R.drawable.ic_mindfulness,
            R.drawable.ic_psychology, R.drawable.ic_calendar
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        habitDao       = AppDatabase.getInstance(this).habitDao();
        sessionManager = new SessionManager(this);
        executor       = Executors.newSingleThreadExecutor();

        etHabitName        = findViewById(R.id.et_habit_name);
        etHabitDescription = findViewById(R.id.et_habit_description);
        freqDaily          = findViewById(R.id.freq_daily);
        freqWeekly         = findViewById(R.id.freq_weekly);
        freqSpecific       = findViewById(R.id.freq_specific);
        tvTitle            = findViewById(R.id.tv_screen_title);
        tvSaveBtn          = findViewById(R.id.btn_save_habit);

        // Day chips
        int[] dayIds = {R.id.day_mon, R.id.day_tue, R.id.day_wed,
                        R.id.day_thu, R.id.day_fri, R.id.day_sat, R.id.day_sun};
        dayViews = new TextView[7];
        for (int i = 0; i < 7; i++) {
            dayViews[i] = findViewById(dayIds[i]);
            final int idx = i;
            dayViews[i].setOnClickListener(v -> toggleDay(idx));
        }

        // Frequency
        freqDaily.setOnClickListener(v    -> selectFrequency("daily"));
        freqWeekly.setOnClickListener(v   -> selectFrequency("weekly"));
        freqSpecific.setOnClickListener(v -> selectFrequency("specific"));

        // Color picker
        for (int i = 0; i < colorViewIds.length; i++) {
            final int idx = i;
            findViewById(colorViewIds[i]).setOnClickListener(v -> selectColor(idx));
        }

        // Icon picker
        for (int i = 0; i < iconViewIds.length; i++) {
            final int idx = i;
            findViewById(iconViewIds[i]).setOnClickListener(v -> selectIcon(idx));
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        tvSaveBtn.setOnClickListener(v -> saveHabit());

        // Edit mode?
        int habitId = getIntent().getIntExtra(EXTRA_HABIT_ID, -1);
        if (habitId != -1) {
            executor.execute(() -> {
                habitToEdit = habitDao.getHabitById(habitId);
                runOnUiThread(this::populateForEdit);
            });
        } else {
            updateFrequencyUI();
            updateDayChips();
            updateIconPicker();
        }
    }

    /** Pre-populate all fields with the existing habit's data */
    private void populateForEdit() {
        if (habitToEdit == null) { finish(); return; }

        // Update title + button label
        if (tvTitle != null) tvTitle.setText("Edit Habit");
        tvSaveBtn.setText("Update Habit");

        // Name & description
        etHabitName.setText(habitToEdit.title);
        etHabitDescription.setText(habitToEdit.description);

        // Frequency
        selectedFrequency = habitToEdit.frequency != null ? habitToEdit.frequency : "daily";
        selectedColor     = habitToEdit.iconColor  != null ? habitToEdit.iconColor  : "indigo";
        selectedIcon      = habitToEdit.iconName   != null ? habitToEdit.iconName   : "self_improvement";

        // Active days — parse "1111111" string
        String daysStr = habitToEdit.activeDays;
        if (daysStr != null && daysStr.length() == 7) {
            for (int i = 0; i < 7; i++) activeDays[i] = daysStr.charAt(i) == '1';
        }

        // Sync color checks
        for (int i = 0; i < colorNames.length; i++) {
            findViewById(colorCheckIds[i])
                    .setVisibility(colorNames[i].equals(selectedColor) ? View.VISIBLE : View.GONE);
        }

        updateFrequencyUI();
        updateDayChips();
        updateIconPicker();
        findViewById(R.id.active_days_row)
                .setVisibility(selectedFrequency.equals("weekly") ? View.GONE : View.VISIBLE);
    }

    private void selectFrequency(String freq) {
        selectedFrequency = freq;
        updateFrequencyUI();
        findViewById(R.id.active_days_row)
                .setVisibility(freq.equals("weekly") ? View.GONE : View.VISIBLE);
    }

    private void updateFrequencyUI() {
        int white   = getResources().getColor(R.color.white, getTheme());
        int textSec = getResources().getColor(R.color.colorTextSecondary, getTheme());

        freqDaily.setTextColor(selectedFrequency.equals("daily")       ? white : textSec);
        freqWeekly.setTextColor(selectedFrequency.equals("weekly")     ? white : textSec);
        freqSpecific.setTextColor(selectedFrequency.equals("specific") ? white : textSec);

        freqDaily.setBackground(selectedFrequency.equals("daily")
                ? AppCompatResources.getDrawable(this, R.drawable.bg_btn_primary) : null);
        freqWeekly.setBackground(selectedFrequency.equals("weekly")
                ? AppCompatResources.getDrawable(this, R.drawable.bg_btn_primary) : null);
        freqSpecific.setBackground(selectedFrequency.equals("specific")
                ? AppCompatResources.getDrawable(this, R.drawable.bg_btn_primary) : null);
    }

    private void toggleDay(int idx) {
        activeDays[idx] = !activeDays[idx];
        updateDayChips();
    }

    private void updateDayChips() {
        int white   = getResources().getColor(R.color.white, getTheme());
        int textSec = getResources().getColor(R.color.colorTextSecondary, getTheme());
        for (int i = 0; i < 7; i++) {
            if (activeDays[i]) {
                dayViews[i].setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_day_chip_active));
                dayViews[i].setTextColor(white);
            } else {
                dayViews[i].setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_day_chip_inactive));
                dayViews[i].setTextColor(textSec);
            }
        }
    }

    private void selectColor(int idx) {
        selectedColor = colorNames[idx];
        for (int i = 0; i < colorCheckIds.length; i++) {
            findViewById(colorCheckIds[i]).setVisibility(i == idx ? View.VISIBLE : View.GONE);
        }
        // Update icon preview tint to match new color
        updateIconPicker();
    }

    private void selectIcon(int idx) {
        selectedIcon = iconNames[idx];
        updateIconPicker();
    }

    private void updateIconPicker() {
        int white   = getResources().getColor(R.color.white, getTheme());
        int textSec = getResources().getColor(R.color.colorTextSecondary, getTheme());

        for (int i = 0; i < iconViewIds.length; i++) {
            android.widget.FrameLayout container =
                    (android.widget.FrameLayout) findViewById(iconViewIds[i]);
            ImageView img = (ImageView) container.getChildAt(0);
            boolean isSelected = iconNames[i].equals(selectedIcon);

            container.setBackground(isSelected
                    ? AppCompatResources.getDrawable(this, R.drawable.bg_btn_primary)
                    : AppCompatResources.getDrawable(this, R.drawable.bg_icon_picker_unselected));

            if (img != null) {
                img.setImageResource(iconDrawables[i]);
                img.setColorFilter(isSelected ? white : textSec);
            }
        }
    }

    private void saveHabit() {
        String name = etHabitName.getText() != null ? etHabitName.getText().toString().trim() : "";
        String desc = etHabitDescription.getText() != null ? etHabitDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etHabitName.setError("Habit name is required");
            etHabitName.requestFocus();
            return;
        }

        StringBuilder days = new StringBuilder();
        for (boolean d : activeDays) days.append(d ? "1" : "0");

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        if (habitToEdit != null) {
            // ── Edit mode: update existing habit ──
            habitToEdit.title       = name;
            habitToEdit.description = desc;
            habitToEdit.iconName    = selectedIcon;
            habitToEdit.iconColor   = selectedColor;
            habitToEdit.frequency   = selectedFrequency;
            habitToEdit.activeDays  = days.toString();
            habitToEdit.updatedAt   = now;

            executor.execute(() -> {
                habitDao.updateHabit(habitToEdit);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Habit updated!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            });
        } else {
            // ── Add mode: insert new habit ──
            Habit habit = new Habit();
            habit.userId      = sessionManager.getUserId();
            habit.title       = name;
            habit.description = desc;
            habit.iconName    = selectedIcon;
            habit.iconColor   = selectedColor;
            habit.frequency   = selectedFrequency;
            habit.activeDays  = days.toString();
            habit.streakCount = 0;
            habit.createdAt   = now;
            habit.updatedAt   = now;

            executor.execute(() -> {
                habitDao.insertHabit(habit);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Habit added!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}

