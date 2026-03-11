package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.AppNotificationDao;
import me.madhushan.reflect.database.Goal;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.database.Habit;
import me.madhushan.reflect.database.HabitDao;
import me.madhushan.reflect.database.HabitCompletionDao;
import me.madhushan.reflect.database.Reflection;
import me.madhushan.reflect.database.ReflectionDao;
import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.InspirationLoader;
import me.madhushan.reflect.utils.SessionManager;

public class HomeFragment extends Fragment {

    private GoalDao goalDao;
    private HabitDao habitDao;
    private HabitCompletionDao habitCompletionDao;
    private ReflectionDao reflectionDao;
    private AppNotificationDao notificationDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout statsRow, emptyState, sectionProgress, chartCard, recentActivityContainer;
    private TextView tvRecentLabel, tvActiveGoals, tvCompleted, tvHabits, tvNotifBadge;
    private CircularProgressView circularProgress;
    private View[] bars;
    private TextView[] dayLabels;

    // -1 means "show all (today)" ; 0–6 means Mon–Sun filter
    private int selectedDayIndex = -1;
    // The 7 date strings for this week (Mon–Sun)
    private final String[] weekDates = new String[7];

    private final ActivityResultLauncher<Intent> goalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) loadData();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(requireContext());
        goalDao        = AppDatabase.getInstance(requireContext()).goalDao();
        habitDao       = AppDatabase.getInstance(requireContext()).habitDao();
        habitCompletionDao = AppDatabase.getInstance(requireContext()).habitCompletionDao();
        reflectionDao      = AppDatabase.getInstance(requireContext()).reflectionDao();
        notificationDao    = AppDatabase.getInstance(requireContext()).appNotificationDao();
        executor       = Executors.newSingleThreadExecutor();

        // Build the Mon–Sun date strings for this week
        buildWeekDates();

        // Set selectedDayIndex to today by default
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        selectedDayIndex = (dow == Calendar.SUNDAY) ? 6 : dow - 2;

        // Avatar
        TextView tvName     = v.findViewById(R.id.tv_user_name);
        TextView tvInitials = v.findViewById(R.id.tv_avatar_initials);
        ImageView ivPhoto   = v.findViewById(R.id.iv_avatar_photo);
        tvName.setText(sessionManager.getUserName());
        AvatarLoader.loadFromSession(requireContext(), ivPhoto, tvInitials, sessionManager);

        // Views
        statsRow               = v.findViewById(R.id.stats_row);
        emptyState             = v.findViewById(R.id.empty_state);
        sectionProgress        = v.findViewById(R.id.section_progress);
        chartCard              = v.findViewById(R.id.chart_card);
        tvRecentLabel          = v.findViewById(R.id.tv_recent_activity_label);
        recentActivityContainer= v.findViewById(R.id.recent_activity_container);
        tvActiveGoals          = v.findViewById(R.id.tv_active_goals_count);
        tvCompleted            = v.findViewById(R.id.tv_completed_count);
        tvHabits               = v.findViewById(R.id.tv_habits_count);
        tvNotifBadge           = v.findViewById(R.id.tv_notif_badge);
        circularProgress       = v.findViewById(R.id.circular_progress);

        bars = new View[]{ v.findViewById(R.id.bar_mon), v.findViewById(R.id.bar_tue),
                v.findViewById(R.id.bar_wed), v.findViewById(R.id.bar_thu),
                v.findViewById(R.id.bar_fri), v.findViewById(R.id.bar_sat), v.findViewById(R.id.bar_sun) };
        dayLabels = new TextView[]{ v.findViewById(R.id.label_mon), v.findViewById(R.id.label_tue),
                v.findViewById(R.id.label_wed), v.findViewById(R.id.label_thu),
                v.findViewById(R.id.label_fri), v.findViewById(R.id.label_sat), v.findViewById(R.id.label_sun) };

        // Make each bar's parent column clickable to filter recent activity by that day
        for (int i = 0; i < bars.length; i++) {
            final int dayIdx = i;
            View parent = (View) bars[i].getParent();
            parent.setClickable(true);
            parent.setFocusable(true);
            parent.setOnClickListener(b -> {
                selectedDayIndex = dayIdx;
                highlightTodayBar();
                loadRecentActivity();
            });
        }

        v.findViewById(R.id.btn_add_first_goal).setOnClickListener(b ->
                goalLauncher.launch(new Intent(requireContext(), AddGoalActivity.class)));
        v.findViewById(R.id.btn_view_all_goals).setOnClickListener(b ->
                ((MainActivity) requireActivity()).switchToTab("goals"));

        // Active Goals card → navigate to Goals tab filtered by active
        v.findViewById(R.id.card_active_goals).setOnClickListener(b ->
                ((MainActivity) requireActivity()).switchToTab("goals", "active"));

        // Completed Goals card → navigate to Goals tab filtered by completed
        v.findViewById(R.id.card_completed_goals).setOnClickListener(b ->
                ((MainActivity) requireActivity()).switchToTab("goals", "completed"));

        // Notification bell → open NotificationsActivity
        v.findViewById(R.id.btn_notifications).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), NotificationsActivity.class)));

        // Habits card → open Habit Tracker
        v.findViewById(R.id.card_habits).setOnClickListener(b ->
                startActivity(new Intent(requireContext(), HabitTrackerActivity.class)));

        setupDailyInspiration(v);
        loadData();
    }

    /** Loads today's quote + image from res/xml/inspirations.xml via InspirationLoader. */
    private void setupDailyInspiration(View v) {
        InspirationLoader.Inspiration today =
                InspirationLoader.getTodaysInspiration(requireContext());

        if (today.imageResId != 0)
            ((ImageView) v.findViewById(R.id.iv_quote_bg)).setImageResource(today.imageResId);

        ((TextView) v.findViewById(R.id.tv_quote_text))
                .setText("\u201C" + today.quote + "\u201D");
        ((TextView) v.findViewById(R.id.tv_quote_author))
                .setText(today.author);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        View v = getView();
        if (v != null) {
            AvatarLoader.loadFromSession(requireContext(),
                    v.findViewById(R.id.iv_avatar_photo),
                    v.findViewById(R.id.tv_avatar_initials), sessionManager);
            ((TextView) v.findViewById(R.id.tv_user_name)).setText(sessionManager.getUserName());
        }
        refreshNotifBadge();
    }

    /** Loads the unread notification count and shows/hides the badge on the bell icon. */
    private void refreshNotifBadge() {
        if (tvNotifBadge == null) return;
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            int unread = notificationDao.getUnreadCount(userId);
            if (getActivity() == null) return;
            requireActivity().runOnUiThread(() -> {
                if (unread > 0) {
                    tvNotifBadge.setVisibility(View.VISIBLE);
                    tvNotifBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
                } else {
                    tvNotifBadge.setVisibility(View.GONE);
                }
            });
        });
    }

    /** Build Mon–Sun date strings for the current week. */
    private void buildWeekDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        // Rewind to Monday
        cal.add(Calendar.DAY_OF_YEAR, -(dow == Calendar.SUNDAY ? 6 : dow - 2));
        for (int i = 0; i < 7; i++) {
            weekDates[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public void loadData() {
        int userId = sessionManager.getUserId();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        executor.execute(() -> {
            int active    = goalDao.getActiveGoalsCount(userId);
            int completed = goalDao.getCompletedGoalsCount(userId);
            int total     = goalDao.getTotalGoalsCount(userId);
            int[] barCounts = getWeeklyBarCounts(userId);
            int maxBar = 1;
            for (int c : barCounts) if (c > maxBar) maxBar = c;
            final int fMax = maxBar;

            int totalHabits     = habitDao.getTotalHabitsCount(userId);
            int completedHabits = habitCompletionDao.getCompletedCountForUserOnDate(userId, todayDate);

            requireActivity().runOnUiThread(() -> {
                updateUI(active, completed, total, barCounts, fMax, totalHabits, completedHabits);
                refreshNotifBadge();
            });
        });
    }

    /** Loads recent activity for the selected day (goals + reflections + habits). */
    private void loadRecentActivity() {
        if (recentActivityContainer == null || !isAdded()) return;
        int userId = sessionManager.getUserId();
        String date = weekDates[selectedDayIndex];
        executor.execute(() -> {
            List<Goal>       goals       = goalDao.getGoalsForDate(userId, date);
            List<Reflection> reflections = reflectionDao.getReflectionsForDate(userId, date);
            List<Habit>      habits      = habitCompletionDao.getHabitsCompletedOnDate(userId, date);

            if (getActivity() == null || !isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                recentActivityContainer.removeAllViews();
                boolean hasAny = !goals.isEmpty() || !reflections.isEmpty() || !habits.isEmpty();
                tvRecentLabel.setVisibility(hasAny ? View.VISIBLE : View.GONE);
                recentActivityContainer.setVisibility(hasAny ? View.VISIBLE : View.GONE);

                // Day label for the section header
                String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
                String dayLabel = date.equals(todayStr) ? "Today" : dayNames[selectedDayIndex];
                tvRecentLabel.setText(dayLabel + "'s Activity");

                if (!hasAny) {
                    // Show empty state inside container
                    TextView empty = new TextView(requireContext());
                    empty.setText("Nothing recorded on " + dayLabel + " yet.");
                    empty.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
                    empty.setTextSize(13f);
                    empty.setPadding(0, 8, 0, 8);
                    recentActivityContainer.addView(empty);
                    recentActivityContainer.setVisibility(View.VISIBLE);
                    tvRecentLabel.setVisibility(View.VISIBLE);
                    return;
                }

                for (Goal g       : goals)       recentActivityContainer.addView(buildGoalRow(g));
                for (Reflection r : reflections)  recentActivityContainer.addView(buildReflectionRow(r));
                for (Habit h      : habits)       recentActivityContainer.addView(buildHabitRow(h));
            });
        });
    }

    private int[] getWeeklyBarCounts(int userId) {
        int[] counts = new int[7];
        for (int i = 0; i < 7; i++) {
            int goals    = goalDao.getActivityCountForDate(userId, weekDates[i]);
            int refs     = reflectionDao.getReflectionsForDate(userId, weekDates[i]).size();
            int habits   = habitCompletionDao.getCompletedCountForUserOnDate(userId, weekDates[i]);
            counts[i]    = goals + refs + habits;
        }
        return counts;
    }

    private void updateUI(int active, int completed, int total,
                          int[] barCounts, int maxBar, int totalHabits, int completedHabits) {
        boolean hasGoals = total > 0;
        emptyState.setVisibility(hasGoals ? View.GONE : View.VISIBLE);
        statsRow.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        sectionProgress.setVisibility(View.VISIBLE);
        chartCard.setVisibility(View.VISIBLE);

        tvHabits.setText(completedHabits + "/" + totalHabits);
        circularProgress.setProgress(totalHabits > 0 ? (float) completedHabits / totalHabits : 0f);

        if (hasGoals) {
            tvActiveGoals.setText(String.valueOf(active));
            tvCompleted.setText(String.valueOf(completed));
        }

        float dp = getResources().getDisplayMetrics().density;
        for (int i = 0; i < 7; i++) {
            int h = maxBar > 0 ? Math.max(8, (int)((barCounts[i] / (float)maxBar) * 112)) : 8;
            ViewGroup.LayoutParams lp = bars[i].getLayoutParams();
            lp.height = (int)(h * dp);
            bars[i].setLayoutParams(lp);
        }
        highlightTodayBar();
        loadRecentActivity();
    }

    private void highlightTodayBar() {
        int primary = getResources().getColor(R.color.primary, null);
        int hint    = getResources().getColor(R.color.text_hint, null);
        for (int i = 0; i < bars.length; i++) {
            boolean selected = (i == selectedDayIndex);
            bars[i].setBackgroundResource(selected ? R.drawable.bg_bar_active : R.drawable.bg_bar_inactive_dark);
            if (dayLabels[i] != null) {
                dayLabels[i].setTextColor(selected ? primary : hint);
                dayLabels[i].setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            }
        }
    }

    // ── Activity row builders ─────────────────────────────────────────────

    private View buildGoalRow(Goal goal) {
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout row = makeRowBase(dp);
        FrameLayout iconBg = makeIcon(dp,
                goal.isAchieved == 1 ? R.drawable.bg_circle_green : R.drawable.bg_circle_blue,
                goal.isAchieved == 1 ? R.drawable.ic_check_circle : R.drawable.ic_flag,
                goal.isAchieved == 1 ? R.color.colorGreenIcon : R.color.colorBlueIcon);
        row.addView(iconBg);
        row.addView(makeTextCol(dp,
                goal.title != null ? goal.title : "Untitled",
                (goal.isAchieved == 1 ? "✅ Goal achieved" : "🎯 Goal updated")
                        + (goal.updatedAt != null ? "  ·  " + goal.updatedAt.substring(0, Math.min(10, goal.updatedAt.length())) : "")));
        row.addView(makeArrow(dp));
        row.setOnClickListener(b -> {
            Intent i = new Intent(requireContext(), GoalDetailsActivity.class);
            i.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
            goalLauncher.launch(i);
        });
        return row;
    }

    private View buildReflectionRow(Reflection reflection) {
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout row = makeRowBase(dp);
        FrameLayout iconBg = makeIcon(dp,
                R.drawable.bg_circle_purple,
                R.drawable.ic_journal,
                R.color.primary);
        row.addView(iconBg);
        String moodEmoji = moodEmoji(reflection.mood);
        row.addView(makeTextCol(dp,
                reflection.title != null ? reflection.title : "Reflection",
                moodEmoji + " Reflection  ·  " + (reflection.createdAt != null
                        ? reflection.createdAt.substring(0, Math.min(10, reflection.createdAt.length())) : "")));
        row.addView(makeArrow(dp));
        row.setOnClickListener(b ->
                startActivity(new Intent(requireContext(), ReflectionJournalActivity.class)));
        return row;
    }

    private View buildHabitRow(Habit habit) {
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout row = makeRowBase(dp);
        FrameLayout iconBg = makeIcon(dp,
                R.drawable.bg_habit_icon_emerald,
                R.drawable.ic_check_circle,
                R.color.colorGreenIcon);
        row.addView(iconBg);
        row.addView(makeTextCol(dp,
                habit.title != null ? habit.title : "Habit",
                "🔥 Habit completed  ·  Streak: " + habit.streakCount + " days"));
        row.addView(makeArrow(dp));
        row.setOnClickListener(b ->
                startActivity(new Intent(requireContext(), HabitTrackerActivity.class)));
        return row;
    }

    // ── Row building helpers ──────────────────────────────────────────────

    private LinearLayout makeRowBase(float dp) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card_dark);
        row.setClickable(true);
        row.setFocusable(true);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0, 0, 0, (int)(10 * dp));
        row.setLayoutParams(rp);
        row.setPadding((int)(14*dp), (int)(14*dp), (int)(14*dp), (int)(14*dp));
        return row;
    }

    private FrameLayout makeIcon(float dp, int bgRes, int iconRes, int tintColorRes) {
        FrameLayout bg = new FrameLayout(requireContext());
        int sz = (int)(40 * dp);
        bg.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
        bg.setBackgroundResource(bgRes);
        ImageView icon = new ImageView(requireContext());
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams((int)(20*dp), (int)(20*dp));
        ip.gravity = android.view.Gravity.CENTER;
        icon.setLayoutParams(ip);
        icon.setImageResource(iconRes);
        icon.setColorFilter(getResources().getColor(tintColorRes, null));
        bg.addView(icon);
        return bg;
    }

    private LinearLayout makeTextCol(float dp, String title, String subtitle) {
        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cp.setMarginStart((int)(14*dp));
        col.setLayoutParams(cp);
        TextView t1 = new TextView(requireContext());
        t1.setText(title);
        t1.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        t1.setTextSize(13f);
        t1.setTypeface(null, android.graphics.Typeface.BOLD);
        col.addView(t1);
        TextView t2 = new TextView(requireContext());
        t2.setText(subtitle);
        t2.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        t2.setTextSize(11f);
        col.addView(t2);
        return col;
    }

    private ImageView makeArrow(float dp) {
        ImageView arrow = new ImageView(requireContext());
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams((int)(18*dp), (int)(18*dp));
        ap.gravity = android.view.Gravity.CENTER_VERTICAL;
        arrow.setLayoutParams(ap);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(getResources().getColor(R.color.text_hint, null));
        return arrow;
    }

    private String moodEmoji(String mood) {
        if (mood == null) return "📝";
        switch (mood) {
            case "happy":   return "😄";
            case "calm":    return "😌";
            case "sad":     return "😔";
            case "anxious": return "😰";
            default:        return "😐";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}
