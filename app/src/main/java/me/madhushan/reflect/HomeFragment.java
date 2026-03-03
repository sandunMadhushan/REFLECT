package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import me.madhushan.reflect.database.Goal;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.ui.CircularProgressView;
import me.madhushan.reflect.utils.AvatarLoader;
import me.madhushan.reflect.utils.SessionManager;

public class HomeFragment extends Fragment {

    private GoalDao goalDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout statsRow, emptyState, sectionProgress, chartCard, recentActivityContainer;
    private TextView tvRecentLabel, tvActiveGoals, tvCompleted, tvHabits;
    private CircularProgressView circularProgress;
    private View[] bars;
    private TextView[] dayLabels;

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
        executor       = Executors.newSingleThreadExecutor();

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
        circularProgress       = v.findViewById(R.id.circular_progress);

        bars = new View[]{ v.findViewById(R.id.bar_mon), v.findViewById(R.id.bar_tue),
                v.findViewById(R.id.bar_wed), v.findViewById(R.id.bar_thu),
                v.findViewById(R.id.bar_fri), v.findViewById(R.id.bar_sat), v.findViewById(R.id.bar_sun) };
        dayLabels = new TextView[]{ v.findViewById(R.id.label_mon), v.findViewById(R.id.label_tue),
                v.findViewById(R.id.label_wed), v.findViewById(R.id.label_thu),
                v.findViewById(R.id.label_fri), v.findViewById(R.id.label_sat), v.findViewById(R.id.label_sun) };

        v.findViewById(R.id.btn_add_first_goal).setOnClickListener(b ->
                goalLauncher.launch(new Intent(requireContext(), AddGoalActivity.class)));
        v.findViewById(R.id.btn_view_all_goals).setOnClickListener(b ->
                ((MainActivity) requireActivity()).switchToTab("goals"));
        v.findViewById(R.id.btn_notifications).setOnClickListener(b ->
                Toast.makeText(requireContext(), "Notifications — coming soon", Toast.LENGTH_SHORT).show());

        loadData();
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
    }

    public void loadData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            int active    = goalDao.getActiveGoalsCount(userId);
            int completed = goalDao.getCompletedGoalsCount(userId);
            int total     = goalDao.getTotalGoalsCount(userId);
            List<Goal> recent = goalDao.getRecentGoals(userId, 5);
            int[] bars    = getWeeklyBarCounts(userId);
            int maxBar    = 1;
            for (int c : bars) if (c > maxBar) maxBar = c;
            final int fMax = maxBar;
            requireActivity().runOnUiThread(() -> updateUI(active, completed, total, recent, bars, fMax));
        });
    }

    private int[] getWeeklyBarCounts(int userId) {
        int[] counts = new int[7];
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_YEAR, -(dow == Calendar.SUNDAY ? 6 : dow - 2));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            counts[i] = goalDao.getActivityCountForDate(userId, sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return counts;
    }

    private void updateUI(int active, int completed, int total, List<Goal> recent, int[] barCounts, int maxBar) {
        boolean hasGoals = total > 0;
        emptyState.setVisibility(hasGoals ? View.GONE : View.VISIBLE);
        statsRow.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        sectionProgress.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        chartCard.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        tvRecentLabel.setVisibility(hasGoals && !recent.isEmpty() ? View.VISIBLE : View.GONE);
        recentActivityContainer.setVisibility(hasGoals ? View.VISIBLE : View.GONE);
        if (!hasGoals) return;

        tvActiveGoals.setText(String.valueOf(active));
        tvCompleted.setText(String.valueOf(completed));
        tvHabits.setText(active + "/" + total);
        circularProgress.setProgress(total > 0 ? (float) completed / total : 0f);

        float dp = getResources().getDisplayMetrics().density;
        for (int i = 0; i < 7; i++) {
            int h = maxBar > 0 ? Math.max(8, (int)((barCounts[i] / (float)maxBar) * 112)) : 8;
            ViewGroup.LayoutParams lp = bars[i].getLayoutParams();
            lp.height = (int)(h * dp);
            bars[i].setLayoutParams(lp);
        }
        highlightTodayBar();

        recentActivityContainer.removeAllViews();
        for (Goal g : recent) recentActivityContainer.addView(buildRow(g));
    }

    private void highlightTodayBar() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int today = (dow == Calendar.SUNDAY) ? 6 : dow - 2;
        int primary = getResources().getColor(R.color.primary, null);
        int hint    = getResources().getColor(R.color.text_hint, null);
        for (int i = 0; i < bars.length; i++) {
            bars[i].setBackgroundResource(i == today ? R.drawable.bg_bar_active : R.drawable.bg_bar_inactive_dark);
            if (dayLabels[i] != null) {
                dayLabels[i].setTextColor(i == today ? primary : hint);
                dayLabels[i].setTypeface(null, i == today ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
            }
        }
    }

    private View buildRow(Goal goal) {
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card_dark);
        row.setClickable(true); row.setFocusable(true);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0, 0, 0, (int)(10 * dp));
        row.setLayoutParams(rp);
        row.setPadding((int)(14*dp),(int)(14*dp),(int)(14*dp),(int)(14*dp));

        FrameLayout iconBg = new FrameLayout(requireContext());
        int sz = (int)(40*dp);
        iconBg.setLayoutParams(new LinearLayout.LayoutParams(sz, sz));
        iconBg.setBackgroundResource(goal.isAchieved == 1 ? R.drawable.bg_circle_green : R.drawable.bg_circle_blue);
        ImageView icon = new ImageView(requireContext());
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams((int)(20*dp),(int)(20*dp));
        ip.gravity = android.view.Gravity.CENTER;
        icon.setLayoutParams(ip);
        icon.setImageResource(goal.isAchieved == 1 ? R.drawable.ic_check_circle : R.drawable.ic_flag);
        icon.setColorFilter(getResources().getColor(goal.isAchieved == 1 ? R.color.colorGreenIcon : R.color.colorBlueIcon, null));
        iconBg.addView(icon); row.addView(iconBg);

        LinearLayout col = new LinearLayout(requireContext());
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        cp.setMarginStart((int)(14*dp));
        col.setLayoutParams(cp);
        TextView t1 = new TextView(requireContext());
        t1.setText(goal.title != null ? goal.title : "Untitled");
        t1.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        t1.setTextSize(13f); t1.setTypeface(null, android.graphics.Typeface.BOLD);
        col.addView(t1);
        TextView t2 = new TextView(requireContext());
        t2.setText((goal.isAchieved == 1 ? "✅ Achieved" : "🎯 In Progress") + (goal.updatedAt != null ? "  ·  " + goal.updatedAt : ""));
        t2.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
        t2.setTextSize(11f);
        col.addView(t2); row.addView(col);

        ImageView arrow = new ImageView(requireContext());
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams((int)(18*dp),(int)(18*dp));
        ap.gravity = android.view.Gravity.CENTER_VERTICAL;
        arrow.setLayoutParams(ap);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(getResources().getColor(R.color.text_hint, null));
        row.addView(arrow);

        row.setOnClickListener(b -> {
            Intent i = new Intent(requireContext(), GoalDetailsActivity.class);
            i.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
            goalLauncher.launch(i);
        });
        return row;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}


