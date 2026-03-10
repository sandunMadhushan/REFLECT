package me.madhushan.reflect;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.GoalDao;
import me.madhushan.reflect.database.HabitCompletionDao;
import me.madhushan.reflect.database.HabitDao;
import me.madhushan.reflect.database.ReflectionDao;
import me.madhushan.reflect.utils.SessionManager;

public class AchievementsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private GoalDao goalDao;
    private HabitDao habitDao;
    private HabitCompletionDao completionDao;
    private ReflectionDao reflectionDao;
    private ExecutorService executor;

    private TextView tvLevel, tvXp, tvProgressLabel;
    private View progressFill;
    private LinearLayout achievementsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        sessionManager = new SessionManager(this);
        AppDatabase db = AppDatabase.getInstance(this);
        goalDao        = db.goalDao();
        habitDao       = db.habitDao();
        completionDao  = db.habitCompletionDao();
        reflectionDao  = db.reflectionDao();
        executor       = Executors.newSingleThreadExecutor();

        tvLevel           = findViewById(R.id.tv_level);
        tvXp              = findViewById(R.id.tv_xp);
        tvProgressLabel   = findViewById(R.id.tv_progress_label);
        progressFill      = findViewById(R.id.progress_fill);
        achievementsContent = findViewById(R.id.achievements_content);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadAndRender();
    }

    private void loadAndRender() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            AchievementEngine.Stats stats = new AchievementEngine.Stats();
            stats.maxHabitStreak        = getMaxStreak(userId);
            stats.totalReflections      = reflectionDao.getReflectionCount(userId);
            stats.totalGoals            = goalDao.getTotalGoalsCount(userId);
            stats.completedGoals        = goalDao.getCompletedGoalsCount(userId);
            stats.totalHabits           = habitDao.getTotalHabitsCount(userId);
            stats.habitCompletionsTotal = completionDao.getTotalCompletionsForUser(userId);

            List<AchievementEngine.Achievement> all = AchievementEngine.evaluate(stats);
            int xp       = AchievementEngine.calcXp(all);
            int level    = AchievementEngine.calcLevel(xp);
            int unlocked = 0;
            for (AchievementEngine.Achievement a : all) if (a.unlocked) unlocked++;
            final int total   = all.size();
            final int finalXp = xp;
            final int finalLevel = level;
            final int finalUnlocked = unlocked;

            runOnUiThread(() -> render(all, finalXp, finalLevel, finalUnlocked, total));
        });
    }

    private int getMaxStreak(int userId) {
        List<me.madhushan.reflect.database.Habit> habits = habitDao.getHabitsForUser(userId);
        int max = 0;
        for (me.madhushan.reflect.database.Habit h : habits) {
            if (h.streakCount > max) max = h.streakCount;
        }
        return max;
    }

    private void render(List<AchievementEngine.Achievement> achievements,
                        int xp, int level, int unlocked, int total) {
        tvLevel.setText("Level " + level);
        tvXp.setText(xp + " XP");
        tvProgressLabel.setText(unlocked + "/" + total + " Unlocked");

        // Animate progress bar width
        progressFill.post(() -> {
            int parentWidth = ((View) progressFill.getParent()).getWidth();
            float pct = total > 0 ? (float) unlocked / total : 0f;
            ViewGroup.LayoutParams lp = progressFill.getLayoutParams();
            lp.width = (int) (parentWidth * pct);
            progressFill.setLayoutParams(lp);
        });

        // Group achievements by category preserving insertion order
        Map<String, List<AchievementEngine.Achievement>> grouped = new LinkedHashMap<>();
        for (AchievementEngine.Achievement a : achievements) {
            grouped.computeIfAbsent(a.category, k -> new ArrayList<>()).add(a);
        }

        achievementsContent.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;

        for (Map.Entry<String, List<AchievementEngine.Achievement>> entry : grouped.entrySet()) {
            // Section header
            achievementsContent.addView(buildSectionHeader(entry.getKey(), dp));

            // 2-column grid
            List<AchievementEngine.Achievement> cats = entry.getValue();
            for (int i = 0; i < cats.size(); i += 2) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowLp.setMargins(0, 0, 0, (int) (12 * dp));
                row.setLayoutParams(rowLp);

                row.addView(buildCard(cats.get(i), dp));
                if (i + 1 < cats.size()) {
                    View spacer = new View(this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams((int)(12*dp), 1));
                    row.addView(spacer);
                    row.addView(buildCard(cats.get(i + 1), dp));
                } else {
                    // Empty placeholder for last odd card
                    View placeholder = new View(this);
                    LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                    plp.setMarginStart((int)(12*dp));
                    placeholder.setLayoutParams(plp);
                    row.addView(placeholder);
                }
                achievementsContent.addView(row);
            }
        }
    }

    private View buildSectionHeader(String category, float dp) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int)(8*dp), 0, (int)(12*dp));
        row.setLayoutParams(lp);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams((int)(22*dp), (int)(22*dp)));
        icon.setImageResource(getCategoryIcon(category));
        icon.setColorFilter(getCategoryColor(category));
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams((int)(22*dp), (int)(22*dp));
        ilp.setMarginEnd((int)(8*dp));
        icon.setLayoutParams(ilp);
        row.addView(icon);

        TextView tv = new TextView(this);
        tv.setText(category);
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary, getTheme()));
        tv.setTextSize(16f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tv);

        return row;
    }

    private View buildCard(AchievementEngine.Achievement a, float dp) {
        // Root FrameLayout — holds card + lock badge
        FrameLayout root = new FrameLayout(this);
        LinearLayout.LayoutParams rootLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        root.setLayoutParams(rootLp);

        // Card body
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding((int)(14*dp), (int)(16*dp), (int)(14*dp), (int)(14*dp));

        if (a.unlocked) {
            card.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_achievement_card_unlocked));
        } else {
            card.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_achievement_card_locked));
            card.setAlpha(a.currentValue > 0 ? 0.8f : 0.6f);
        }

        FrameLayout.LayoutParams cardLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(cardLp);

        // Teal bottom border for unlocked
        if (a.unlocked) {
            card.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_achievement_card_unlocked));
            // Add bottom accent line
            card.setBackgroundResource(R.drawable.bg_achievement_card_unlocked);
            // We'll fake bottom border with elevation + a teal bottom stripe
        }

        // Icon circle
        FrameLayout iconCircle = new FrameLayout(this);
        int circleSize = (int)(64*dp);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(circleSize, circleSize);
        clp.setMargins(0, 0, 0, (int)(10*dp));
        iconCircle.setLayoutParams(clp);
        iconCircle.setBackground(AppCompatResources.getDrawable(this,
                a.unlocked ? R.drawable.bg_achievement_icon_unlocked : R.drawable.bg_achievement_icon_locked));

        ImageView iconView = new ImageView(this);
        FrameLayout.LayoutParams ivlp = new FrameLayout.LayoutParams(
                (int)(32*dp), (int)(32*dp));
        ivlp.gravity = Gravity.CENTER;
        iconView.setLayoutParams(ivlp);
        iconView.setImageResource(getIconDrawable(a.iconName));
        iconView.setColorFilter(a.unlocked
                ? getResources().getColor(R.color.colorTealAccent, getTheme())
                : getResources().getColor(R.color.colorAchievementTextLocked, getTheme()));
        iconCircle.addView(iconView);
        card.addView(iconCircle);

        // Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText(a.title);
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(getResources().getColor(
                a.unlocked ? R.color.colorTextPrimary : R.color.colorAchievementTextLocked, getTheme()));
        card.addView(tvTitle);

        // Description
        TextView tvDesc = new TextView(this);
        tvDesc.setText(a.description);
        tvDesc.setTextSize(11f);
        tvDesc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dlp.setMargins(0, (int)(2*dp), 0, 0);
        tvDesc.setLayoutParams(dlp);
        tvDesc.setTextColor(getResources().getColor(
                a.unlocked ? R.color.colorTextSecondary : R.color.colorAchievementTextLocked, getTheme()));
        card.addView(tvDesc);

        // Progress bar for in-progress locked achievements
        if (!a.unlocked && a.currentValue > 0 && a.targetValue > 0) {
            LinearLayout.LayoutParams pbContLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pbContLp.setMargins(0, (int)(10*dp), 0, 0);

            FrameLayout pbCont = new FrameLayout(this);
            pbCont.setLayoutParams(pbContLp);
            pbCont.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_achievement_progress_track));
            pbCont.setMinimumHeight((int)(6*dp));

            View fill = new View(this);
            float pct = Math.min(1f, (float) a.currentValue / a.targetValue);
            FrameLayout.LayoutParams fillLp = new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            fill.setLayoutParams(fillLp);
            fill.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_achievement_progress_fill));
            fill.setAlpha(0.6f);
            pbCont.addView(fill);

            // Set width after layout
            final View fillRef = fill;
            final float pctRef = pct;
            pbCont.post(() -> {
                ViewGroup.LayoutParams lp2 = fillRef.getLayoutParams();
                lp2.width = (int)(pbCont.getWidth() * pctRef);
                fillRef.setLayoutParams(lp2);
            });

            card.addView(pbCont);

            // "X/Y" label
            TextView tvProg = new TextView(this);
            tvProg.setText(a.currentValue + "/" + a.targetValue);
            tvProg.setTextSize(9f);
            tvProg.setGravity(Gravity.CENTER);
            tvProg.setTextColor(getResources().getColor(R.color.colorAchievementTextLocked, getTheme()));
            LinearLayout.LayoutParams tvProgLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvProgLp.setMargins(0, (int)(3*dp), 0, 0);
            tvProg.setLayoutParams(tvProgLp);
            card.addView(tvProg);
        }

        root.addView(card);

        // Lock / unlock badge (top-right corner)
        FrameLayout badge = new FrameLayout(this);
        int badgeSize = (int)(26*dp);
        FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(badgeSize, badgeSize);
        badgeLp.gravity = Gravity.TOP | Gravity.END;
        badgeLp.setMargins(0, (int)(6*dp), (int)(6*dp), 0);
        badge.setLayoutParams(badgeLp);
        if (a.unlocked) {
            badge.setBackground(AppCompatResources.getDrawable(this, R.drawable.bg_xp_badge));
        }

        ImageView badgeIcon = new ImageView(this);
        FrameLayout.LayoutParams bilp = new FrameLayout.LayoutParams((int)(14*dp), (int)(14*dp));
        bilp.gravity = Gravity.CENTER;
        badgeIcon.setLayoutParams(bilp);
        badgeIcon.setImageResource(a.unlocked ? R.drawable.ic_check_circle : R.drawable.ic_goals);
        badgeIcon.setColorFilter(a.unlocked
                ? getResources().getColor(R.color.colorTealAccent, getTheme())
                : getResources().getColor(R.color.colorAchievementTextLocked, getTheme()));
        badge.addView(badgeIcon);
        root.addView(badge);

        return root;
    }

    private int getCategoryIcon(String cat) {
        switch (cat) {
            case "Streaks":     return R.drawable.ic_fire;
            case "Reflections": return R.drawable.ic_psychology;
            case "Goals":       return R.drawable.ic_flag;
            default:            return R.drawable.ic_favorite;
        }
    }

    private int getCategoryColor(String cat) {
        switch (cat) {
            case "Streaks":     return 0xFFF97316; // orange
            case "Reflections": return 0xFF9333EA; // purple
            case "Goals":       return 0xFF3B82F6; // blue
            default:            return 0xFF10B981; // green
        }
    }

    private int getIconDrawable(String name) {
        if (name == null) return R.drawable.ic_favorite;
        switch (name) {
            case "check_circle":    return R.drawable.ic_check_circle;
            case "bolt":            return R.drawable.ic_fire;
            case "calendar_month":  return R.drawable.ic_calendar;
            case "diamond":         return R.drawable.ic_goals;
            case "edit_note":       return R.drawable.ic_edit;
            case "menu_book":       return R.drawable.ic_menu_book;
            case "psychology":      return R.drawable.ic_psychology;
            case "self_improvement":return R.drawable.ic_self_improvement;
            case "flag":            return R.drawable.ic_flag;
            case "emoji_events":    return R.drawable.ic_goals;
            case "military_tech":   return R.drawable.ic_goals;
            case "star":            return R.drawable.ic_favorite;
            case "favorite":        return R.drawable.ic_favorite;
            case "fitness_center":  return R.drawable.ic_fitness_center;
            default:                return R.drawable.ic_check_circle;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}

