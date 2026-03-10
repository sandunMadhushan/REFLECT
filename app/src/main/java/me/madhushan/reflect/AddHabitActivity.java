package me.madhushan.reflect;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

    private TextInputEditText etHabitName, etHabitDescription;
    private TextView freqDaily, freqWeekly, freqSpecific;
    private TextView[] dayViews;

    private String selectedFrequency = "daily";
    private String selectedColor = "indigo";
    private final boolean[] activeDays = {true, true, true, true, true, true, true};

    private HabitDao habitDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private final int[] colorViewIds = {
            R.id.color_indigo, R.id.color_emerald, R.id.color_pink,
            R.id.color_orange, R.id.color_primary
    };
    private final int[] colorCheckIds = {
            R.id.color_indigo_check, R.id.color_emerald_check, R.id.color_pink_check,
            R.id.color_orange_check, R.id.color_primary_check
    };
    private final String[] colorNames = {"indigo", "emerald", "pink", "orange", "primary"};

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

        int[] dayIds = {R.id.day_mon, R.id.day_tue, R.id.day_wed,
                        R.id.day_thu, R.id.day_fri, R.id.day_sat, R.id.day_sun};
        dayViews = new TextView[7];
        for (int i = 0; i < 7; i++) {
            dayViews[i] = findViewById(dayIds[i]);
            final int idx = i;
            dayViews[i].setOnClickListener(v -> toggleDay(idx));
        }

        freqDaily.setOnClickListener(v    -> selectFrequency("daily"));
        freqWeekly.setOnClickListener(v   -> selectFrequency("weekly"));
        freqSpecific.setOnClickListener(v -> selectFrequency("specific"));

        for (int i = 0; i < colorViewIds.length; i++) {
            final int idx = i;
            findViewById(colorViewIds[i]).setOnClickListener(v -> selectColor(idx));
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_habit).setOnClickListener(v -> saveHabit());

        updateFrequencyUI();
        updateDayChips();
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

        freqDaily.setTextColor(selectedFrequency.equals("daily")    ? white : textSec);
        freqWeekly.setTextColor(selectedFrequency.equals("weekly")  ? white : textSec);
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

        Habit habit = new Habit();
        habit.userId      = sessionManager.getUserId();
        habit.title       = name;
        habit.description = TextUtils.isEmpty(desc) ? "" : desc;
        habit.iconName    = getIconNameForColor(selectedColor);
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

    private String getIconNameForColor(String color) {
        switch (color) {
            case "emerald": return "water_drop";
            case "pink":    return "book_2";
            case "orange":  return "fitness_center";
            case "primary": return "check_circle";
            default:        return "self_improvement";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}

