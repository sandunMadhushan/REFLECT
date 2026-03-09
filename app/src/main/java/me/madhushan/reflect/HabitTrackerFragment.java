package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HabitTrackerFragment extends Fragment {

    // Views
    private TextView tvMonthYear;
    private TextView tvStreakDays;
    private TextView tvCompletionPct;
    private RecyclerView rvHabits;
    private TextView tvEmptyHabits;

    // Calendar views
    private final TextView[] tvDays = new TextView[7];
    private final FrameLayout[] dayFrames = new FrameLayout[7];

    // Data
    private HabitAdapter adapter;
    private final List<HabitModel> habitList = new ArrayList<>();

    // Calendar state
    private Calendar displayedCalendar;
    private int selectedDayIndex;

    // ─────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_habit_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupCalendar();
        setupRecyclerView();
        setupClickListeners(view);
        loadDummyData();
    }

    // ─────────────────────────────────────────────
    // Bind views
    // ─────────────────────────────────────────────

    private void bindViews(View root) {

        tvMonthYear = root.findViewById(R.id.tv_month_year);
        tvStreakDays = root.findViewById(R.id.tv_streak_days);
        tvCompletionPct = root.findViewById(R.id.tv_completion_pct);
        rvHabits = root.findViewById(R.id.rv_habits);
        tvEmptyHabits = root.findViewById(R.id.tv_empty_habits);

        int[] dayViewIds = {
                R.id.tv_day_0, R.id.tv_day_1, R.id.tv_day_2, R.id.tv_day_3,
                R.id.tv_day_4, R.id.tv_day_5, R.id.tv_day_6
        };

        int[] frameIds = {
                R.id.day_0, R.id.day_1, R.id.day_2, R.id.day_3,
                R.id.day_4, R.id.day_5, R.id.day_6
        };

        for (int i = 0; i < 7; i++) {
            tvDays[i] = root.findViewById(dayViewIds[i]);
            dayFrames[i] = root.findViewById(frameIds[i]);
        }
    }

    // ─────────────────────────────────────────────
    // Calendar
    // ─────────────────────────────────────────────

    private void setupCalendar() {

        displayedCalendar = Calendar.getInstance();
        selectedDayIndex = displayedCalendar.get(Calendar.DAY_OF_WEEK) - 1;

        renderCalendar();
    }

    private void renderCalendar() {

        if (getContext() == null) return;

        Calendar weekStart = (Calendar) displayedCalendar.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        Calendar midWeek = (Calendar) weekStart.clone();
        midWeek.add(Calendar.DAY_OF_MONTH, 3);

        tvMonthYear.setText(
                new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        .format(midWeek.getTime())
        );

        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {

            Calendar day = (Calendar) weekStart.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            tvDays[i].setText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)));

            boolean isToday = isSameDay(day, today);
            boolean isSelected = (i == selectedDayIndex);

            if (isSelected) {

                tvDays[i].setBackground(
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_ht_selected_day));

                tvDays[i].setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.ht_selected_day_text));

                tvDays[i].setTypeface(null, android.graphics.Typeface.BOLD);

            } else {

                tvDays[i].setBackground(null);

                tvDays[i].setTextColor(
                        ContextCompat.getColor(requireContext(),
                                isToday ? R.color.ht_teal : R.color.ht_date_text));

                tvDays[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            final int idx = i;

            dayFrames[i].setOnClickListener(v -> selectDay(idx));
        }
    }

    private void selectDay(int idx) {
        selectedDayIndex = idx;
        renderCalendar();
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    // ─────────────────────────────────────────────
    // RecyclerView
    // ─────────────────────────────────────────────

    private void setupRecyclerView() {

        adapter = new HabitAdapter(requireContext(), habitList);

        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHabits.setAdapter(adapter);
        rvHabits.setNestedScrollingEnabled(false);

        adapter.setOnHabitToggleListener((habit, isCompleted) -> updateCompletionStats());
    }

    // ─────────────────────────────────────────────
    // Dummy data
    // ─────────────────────────────────────────────

    private void loadDummyData() {

        habitList.clear();

        habitList.add(new HabitModel("Morning Meditation", "15 minutes · Daily", 12, true));
        habitList.add(new HabitModel("Drink Water", "8 glasses · Daily", 7, true));
        habitList.add(new HabitModel("Read 20 Pages", "20 pages · Daily", 5, false));
        habitList.add(new HabitModel("Evening Workout", "30 minutes · Daily", 3, false));

        adapter.notifyDataSetChanged();

        updateEmptyState();
        updateStats();
    }

    // ─────────────────────────────────────────────
    // Stats
    // ─────────────────────────────────────────────

    private void updateEmptyState() {

        if (habitList.isEmpty()) {
            rvHabits.setVisibility(View.GONE);
            tvEmptyHabits.setVisibility(View.VISIBLE);
        } else {
            rvHabits.setVisibility(View.VISIBLE);
            tvEmptyHabits.setVisibility(View.GONE);
        }
    }

    private void updateStats() {
        updateStreakStat();
        updateCompletionStats();
    }

    private void updateStreakStat() {

        int maxStreak = 0;

        for (HabitModel h : habitList) {
            if (h.streak > maxStreak) maxStreak = h.streak;
        }

        tvStreakDays.setText(getString(R.string.ht_streak_days, maxStreak));
    }

    private void updateCompletionStats() {

        if (habitList.isEmpty()) {
            tvCompletionPct.setText(getString(R.string.ht_completion_pct, 0));
            return;
        }

        int doneCount = 0;

        for (HabitModel h : habitList) {
            if (h.completed) doneCount++;
        }

        int pct = (int) ((doneCount / (float) habitList.size()) * 100);

        tvCompletionPct.setText(getString(R.string.ht_completion_pct, pct));
    }

    // ─────────────────────────────────────────────
    // Click listeners
    // ─────────────────────────────────────────────

    private void setupClickListeners(View root) {

        root.findViewById(R.id.btn_settings).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show());

        root.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            displayedCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            renderCalendar();
        });

        root.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            displayedCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            renderCalendar();
        });

        root.findViewById(R.id.btn_manage).setOnClickListener(v -> {

            try {
                startActivity(new Intent(requireContext(), AddHabitActivity.class));
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Manage habits coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        root.findViewById(R.id.btn_add_habit).setOnClickListener(v -> {

            try {
                startActivity(new Intent(requireContext(), AddHabitActivity.class));
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Add habit coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Called from MainActivity to refresh this fragment
    public void loadData() {
        loadDummyData();
    }

}