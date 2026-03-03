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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Goal;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.utils.SessionManager;

public class GoalsFragment extends Fragment {

    private GoalDao goalDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout goalsContainer, goalsEmptyState;
    private TextView filterAll, filterActive, filterCompleted;
    private String currentFilter = "all";

    private final ActivityResultLauncher<Intent> goalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) loadData();
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        sessionManager  = new SessionManager(requireContext());
        goalDao         = AppDatabase.getInstance(requireContext()).goalDao();
        executor        = Executors.newSingleThreadExecutor();

        goalsContainer  = v.findViewById(R.id.goals_container);
        goalsEmptyState = v.findViewById(R.id.goals_empty_state);
        filterAll       = v.findViewById(R.id.goals_filter_all);
        filterActive    = v.findViewById(R.id.goals_filter_active);
        filterCompleted = v.findViewById(R.id.goals_filter_completed);

        filterAll.setOnClickListener(b       -> { currentFilter = "all";       applyFilterUI(); loadData(); });
        filterActive.setOnClickListener(b    -> { currentFilter = "active";    applyFilterUI(); loadData(); });
        filterCompleted.setOnClickListener(b -> { currentFilter = "completed"; applyFilterUI(); loadData(); });

        loadData();
    }

    @Override public void onResume() { super.onResume(); loadData(); }

    private void applyFilterUI() {
        int white    = getResources().getColor(R.color.white, null);
        int inactive = getResources().getColor(R.color.colorTextSecondary, null);
        filterAll.setBackgroundResource(currentFilter.equals("all") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterAll.setTextColor(currentFilter.equals("all") ? white : inactive);
        filterActive.setBackgroundResource(currentFilter.equals("active") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterActive.setTextColor(currentFilter.equals("active") ? white : inactive);
        filterCompleted.setBackgroundResource(currentFilter.equals("completed") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterCompleted.setTextColor(currentFilter.equals("completed") ? white : inactive);
    }

    public void loadData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<Goal> all = goalDao.getGoalsForUser(userId);
            List<Goal> filtered = new ArrayList<>();
            for (Goal g : all) {
                if      (currentFilter.equals("all"))                          filtered.add(g);
                else if (currentFilter.equals("active")    && g.isAchieved==0) filtered.add(g);
                else if (currentFilter.equals("completed") && g.isAchieved==1) filtered.add(g);
            }
            requireActivity().runOnUiThread(() -> renderGoals(filtered));
        });
    }

    private void renderGoals(List<Goal> goals) {
        goalsContainer.removeAllViews();
        goalsEmptyState.setVisibility(goals.isEmpty() ? View.VISIBLE : View.GONE);
        if (goals.isEmpty()) return;

        float dp = getResources().getDisplayMetrics().density;
        for (Goal goal : goals) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card_dark);
            card.setElevation(2*dp);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,0,0,(int)(12*dp));
            card.setLayoutParams(lp);
            card.setPadding((int)(16*dp),(int)(16*dp),(int)(16*dp),(int)(16*dp));
            card.setClickable(true); card.setFocusable(true);
            card.setForeground(requireContext().getDrawable(android.R.drawable.list_selector_background));

            // Row: icon + title + badge
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            FrameLayout iconBox = new FrameLayout(requireContext());
            int isz = (int)(48*dp);
            LinearLayout.LayoutParams ibp = new LinearLayout.LayoutParams(isz, isz);
            ibp.setMarginEnd((int)(12*dp)); ibp.gravity = android.view.Gravity.CENTER_VERTICAL;
            iconBox.setLayoutParams(ibp);
            iconBox.setBackgroundResource(R.drawable.bg_circle_blue);
            ImageView ico = new ImageView(requireContext());
            FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams((int)(24*dp),(int)(24*dp));
            fp.gravity = android.view.Gravity.CENTER;
            ico.setLayoutParams(fp);
            ico.setImageResource(R.drawable.ic_flag);
            ico.setColorFilter(getResources().getColor(R.color.colorBlueIcon, null));
            iconBox.addView(ico); row.addView(iconBox);

            LinearLayout textCol = new LinearLayout(requireContext());
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            TextView tvTitle = new TextView(requireContext());
            tvTitle.setText(goal.title); tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            tvTitle.setTextSize(16f); tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            textCol.addView(tvTitle);
            TextView tvSub = new TextView(requireContext());
            tvSub.setText(goal.deadline != null ? "Deadline: " + goal.deadline : (goal.category != null ? goal.category : "No deadline"));
            tvSub.setTextColor(getResources().getColor(R.color.colorTextSecondary, null)); tvSub.setTextSize(12f);
            textCol.addView(tvSub); row.addView(textCol);

            TextView badge = new TextView(requireContext());
            badge.setText(goal.isAchieved == 1 ? "Done" : "Active");
            badge.setTextSize(11f); badge.setTypeface(null, android.graphics.Typeface.BOLD);
            badge.setTextColor(getResources().getColor(goal.isAchieved == 1 ? R.color.colorGreenIcon : R.color.primary, null));
            badge.setBackgroundResource(goal.isAchieved == 1 ? R.drawable.bg_circle_green : R.drawable.bg_badge_primary);
            badge.setPadding((int)(8*dp),(int)(3*dp),(int)(8*dp),(int)(3*dp));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bp.gravity = android.view.Gravity.CENTER_VERTICAL; badge.setLayoutParams(bp);
            row.addView(badge); card.addView(row);

            // Progress bar
            LinearLayout pSec = new LinearLayout(requireContext());
            pSec.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams pslp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pslp.setMargins(0,(int)(12*dp),0,0); pSec.setLayoutParams(pslp);
            LinearLayout pRow = new LinearLayout(requireContext());
            pRow.setOrientation(LinearLayout.HORIZONTAL);
            pRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView pLbl = new TextView(requireContext());
            pLbl.setText("Progress"); pLbl.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            pLbl.setTextSize(11f); pLbl.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            pRow.addView(pLbl);
            int pct = goal.isAchieved == 1 ? 100 : 0;
            TextView pPct = new TextView(requireContext());
            pPct.setText(pct + "%"); pPct.setTextColor(getResources().getColor(R.color.primary, null));
            pPct.setTextSize(11f); pPct.setTypeface(null, android.graphics.Typeface.BOLD);
            pRow.addView(pPct); pSec.addView(pRow);
            ProgressBar pb = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100); pb.setProgress(pct);
            pb.setProgressDrawable(requireContext().getDrawable(R.drawable.bg_bar_active));
            LinearLayout.LayoutParams pblp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(6*dp));
            pblp.setMargins(0,(int)(6*dp),0,0); pb.setLayoutParams(pblp);
            pSec.addView(pb); card.addView(pSec);

            card.setOnClickListener(b -> {
                Intent i = new Intent(requireContext(), GoalDetailsActivity.class);
                i.putExtra(GoalDetailsActivity.EXTRA_GOAL_ID, goal.id);
                goalLauncher.launch(i);
            });
            goalsContainer.addView(card);
        }
    }

    @Override public void onDestroy() { super.onDestroy(); if (executor != null) executor.shutdown(); }
}






