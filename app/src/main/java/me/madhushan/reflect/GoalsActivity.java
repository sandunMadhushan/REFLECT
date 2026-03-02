package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Goal;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.utils.SessionManager;

public class GoalsActivity extends AppCompatActivity {

    private GoalDao goalDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout goalsContainer, emptyState;

    private String currentFilter = "all"; // all, active, completed

    private TextView filterAll, filterActive, filterCompleted;

    private final ActivityResultLauncher<Intent> goalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> { if (result.getResultCode() == RESULT_OK) loadGoals(); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        goalDao = AppDatabase.getInstance(this).goalDao();
        sessionManager = new SessionManager(this);
        executor = Executors.newSingleThreadExecutor();

        goalsContainer = findViewById(R.id.goals_container);
        emptyState     = findViewById(R.id.empty_state);
        filterAll      = findViewById(R.id.filter_all);
        filterActive   = findViewById(R.id.filter_active);
        filterCompleted = findViewById(R.id.filter_completed);

        // FAB
        findViewById(R.id.fab_add_goal).setOnClickListener(v ->
                goalLauncher.launch(new Intent(this, AddGoalActivity.class)));

        // Filter chips
        filterAll.setOnClickListener(v -> { currentFilter = "all"; applyFilterUI(); loadGoals(); });
        filterActive.setOnClickListener(v -> { currentFilter = "active"; applyFilterUI(); loadGoals(); });
        filterCompleted.setOnClickListener(v -> { currentFilter = "completed"; applyFilterUI(); loadGoals(); });

        loadGoals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
    }

    private void applyFilterUI() {
        int active = getResources().getColor(R.color.white, null);
        int inactive = getResources().getColor(R.color.colorTextSecondary, null);

        filterAll.setBackgroundResource(currentFilter.equals("all") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterAll.setTextColor(currentFilter.equals("all") ? active : inactive);

        filterActive.setBackgroundResource(currentFilter.equals("active") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterActive.setTextColor(currentFilter.equals("active") ? active : inactive);

        filterCompleted.setBackgroundResource(currentFilter.equals("completed") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterCompleted.setTextColor(currentFilter.equals("completed") ? active : inactive);
    }

    private void loadGoals() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<Goal> all = goalDao.getGoalsForUser(userId);
            List<Goal> filtered = new ArrayList<>();
            for (Goal g : all) {
                if (currentFilter.equals("all")) filtered.add(g);
                else if (currentFilter.equals("active") && g.isAchieved == 0) filtered.add(g);
                else if (currentFilter.equals("completed") && g.isAchieved == 1) filtered.add(g);
            }
            runOnUiThread(() -> renderGoals(filtered));
        });
    }

    private void renderGoals(List<Goal> goals) {
        goalsContainer.removeAllViews();

        if (goals.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            goalsContainer.setVisibility(View.GONE);
            return;
        }

        emptyState.setVisibility(View.GONE);
        goalsContainer.setVisibility(View.VISIBLE);

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
            card.setForeground(getDrawable(android.R.drawable.list_selector_background));

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
            tvTitle.setTextSize(16f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvTitle);

            TextView tvSub = new TextView(this);
            tvSub.setText(goal.deadline != null ? "Deadline: " + goal.deadline : "No deadline set");
            tvSub.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            tvSub.setTextSize(12f);
            textCol.addView(tvSub);
            row.addView(textCol);

            // Status badge
            TextView badge = new TextView(this);
            String statusText = goal.isAchieved == 1 ? "Done" : "Active";
            badge.setText(statusText);
            badge.setTextSize(11f);
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

            // Progress bar
            LinearLayout progressSection = new LinearLayout(this);
            progressSection.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams psLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            psLp.setMargins(0, (int)(12 * density), 0, 0);
            progressSection.setLayoutParams(psLp);

            LinearLayout progressLabels = new LinearLayout(this);
            progressLabels.setOrientation(LinearLayout.HORIZONTAL);
            progressLabels.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tvProgressLabel = new TextView(this);
            tvProgressLabel.setText("Progress");
            tvProgressLabel.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            tvProgressLabel.setTextSize(11f);
            tvProgressLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            progressLabels.addView(tvProgressLabel);

            TextView tvPct = new TextView(this);
            int pct = goal.isAchieved == 1 ? 100 : 0;
            tvPct.setText(pct + "%");
            tvPct.setTextColor(getResources().getColor(R.color.primary, null));
            tvPct.setTextSize(11f);
            tvPct.setTypeface(null, android.graphics.Typeface.BOLD);
            progressLabels.addView(tvPct);
            progressSection.addView(progressLabels);

            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setProgress(pct);
            progressBar.setProgressDrawable(getDrawable(R.drawable.bg_bar_active));
            LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int)(6 * density));
            pbLp.setMargins(0, (int)(6 * density), 0, 0);
            progressBar.setLayoutParams(pbLp);
            progressSection.addView(progressBar);
            card.addView(progressSection);

            // Click to open details
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, GoalDetailsActivity.class);
                intent.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
                goalLauncher.launch(intent);
            });

            goalsContainer.addView(card);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}

