package me.madhushan.reflect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Reflection;
import me.madhushan.reflect.database.ReflectionDao;
import me.madhushan.reflect.utils.SessionManager;

public class ReflectionJournalActivity extends AppCompatActivity {

    private ReflectionDao reflectionDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout entriesContainer;
    private LinearLayout emptyState;
    private TextView filterAll, filterWeek, filterMonth, filterFavorites;
    private String currentFilter = "all";

    private final ActivityResultLauncher<Intent> addLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) loadData();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflection_journal);

        sessionManager  = new SessionManager(this);
        reflectionDao   = AppDatabase.getInstance(this).reflectionDao();
        executor        = Executors.newSingleThreadExecutor();

        entriesContainer = findViewById(R.id.entries_container);
        emptyState       = findViewById(R.id.empty_state);
        filterAll        = findViewById(R.id.filter_all);
        filterWeek       = findViewById(R.id.filter_week);
        filterMonth      = findViewById(R.id.filter_month);
        filterFavorites  = findViewById(R.id.filter_favorites);

        filterAll.setOnClickListener(v       -> setFilter("all"));
        filterWeek.setOnClickListener(v      -> setFilter("week"));
        filterMonth.setOnClickListener(v     -> setFilter("month"));
        filterFavorites.setOnClickListener(v -> setFilter("favorites"));

        // FAB — write new reflection
        findViewById(R.id.fab_add).setOnClickListener(v ->
                addLauncher.launch(new Intent(this, AddReflectionActivity.class)));

        // Bottom navigation
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        findViewById(R.id.nav_goals).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("open_tab", "goals");
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        // Journal tab — already here, just stay (or re-apply filter)
        findViewById(R.id.nav_journal).setOnClickListener(v -> { /* already on journal */ });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // ── Filter ─────────────────────────────────────────────────────────────

    private void setFilter(String filter) {
        currentFilter = filter;
        applyFilterUI();
        loadData();
    }

    private void applyFilterUI() {
        int white    = getResources().getColor(R.color.white, null);
        int inactive = getResources().getColor(R.color.colorTextSecondary, null);

        filterAll.setBackgroundResource(currentFilter.equals("all")           ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterAll.setTextColor(currentFilter.equals("all")                     ? white : inactive);
        filterAll.setTypeface(null, currentFilter.equals("all") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        filterWeek.setBackgroundResource(currentFilter.equals("week")         ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterWeek.setTextColor(currentFilter.equals("week")                   ? white : inactive);
        filterWeek.setTypeface(null, currentFilter.equals("week") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        filterMonth.setBackgroundResource(currentFilter.equals("month")       ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterMonth.setTextColor(currentFilter.equals("month")                 ? white : inactive);
        filterMonth.setTypeface(null, currentFilter.equals("month") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        filterFavorites.setBackgroundResource(currentFilter.equals("favorites") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterFavorites.setTextColor(currentFilter.equals("favorites")           ? white : inactive);
        filterFavorites.setTypeface(null, currentFilter.equals("favorites") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    // ── Data loading ───────────────────────────────────────────────────────

    private void loadData() {
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            List<Reflection> all = reflectionDao.getReflectionsForUser(userId);
            List<Reflection> filtered = new ArrayList<>();

            switch (currentFilter) {
                case "week":
                    Calendar cal = Calendar.getInstance();
                    int dow = cal.get(Calendar.DAY_OF_WEEK);
                    cal.add(Calendar.DAY_OF_YEAR, -(dow == Calendar.SUNDAY ? 6 : dow - 2));
                    String weekStart = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                    for (Reflection r : all)
                        if (r.createdAt != null && r.createdAt.compareTo(weekStart) >= 0) filtered.add(r);
                    break;
                case "month":
                    String monthPrefix = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                            .format(Calendar.getInstance().getTime());
                    for (Reflection r : all)
                        if (r.createdAt != null && r.createdAt.startsWith(monthPrefix)) filtered.add(r);
                    break;
                case "favorites":
                    for (Reflection r : all) if (r.isFavorite == 1) filtered.add(r);
                    break;
                default:
                    filtered.addAll(all);
                    break;
            }

            final List<Reflection> result = filtered;
            runOnUiThread(() -> renderEntries(result));
        });
    }

    // ── Render entry cards ─────────────────────────────────────────────────

    private void renderEntries(List<Reflection> entries) {
        entriesContainer.removeAllViews();

        if (entries.isEmpty()) {
            emptyState.setVisibility(android.view.View.VISIBLE);
            return;
        }
        emptyState.setVisibility(android.view.View.GONE);

        float dp = getResources().getDisplayMetrics().density;

        for (Reflection r : entries) {

            // ── Outer card ──────────────────────────────────────────────────
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackground(getDrawable(R.drawable.bg_journal_card));
            card.setElevation(2 * dp);
            card.setPadding((int)(16*dp),(int)(16*dp),(int)(16*dp),(int)(16*dp));
            card.setClickable(true);
            card.setFocusable(true);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, (int)(12 * dp));
            card.setLayoutParams(cardLp);

            // ── Mood icon box (rounded-xl, 48×48) ──────────────────────────
            FrameLayout iconBox = new FrameLayout(this);
            int boxSize = (int)(48 * dp);
            LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(boxSize, boxSize);
            boxLp.setMarginEnd((int)(14 * dp));
            boxLp.gravity = android.view.Gravity.TOP;
            iconBox.setLayoutParams(boxLp);
            iconBox.setBackground(getDrawable(moodBoxDrawable(r.mood)));

            ImageView moodIcon = new ImageView(this);
            FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(
                    (int)(26*dp),(int)(26*dp));
            iconLp.gravity = android.view.Gravity.CENTER;
            moodIcon.setLayoutParams(iconLp);
            moodIcon.setImageResource(moodIconDrawable(r.mood));
            moodIcon.setColorFilter(getResources().getColor(moodIconColor(r.mood), null));
            iconBox.addView(moodIcon);
            card.addView(iconBox);

            // ── Right column ────────────────────────────────────────────────
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Title row: title + time
            LinearLayout titleRow = new LinearLayout(this);
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            titleRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tvTitle = new TextView(this);
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvTitle.setText(r.title != null ? r.title : "Reflection");
            tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            tvTitle.setTextSize(15f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setMaxLines(1);
            tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
            titleRow.addView(tvTitle);

            TextView tvTime = new TextView(this);
            tvTime.setText(formatTime(r.createdAt));
            tvTime.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
            tvTime.setTextSize(11f);
            titleRow.addView(tvTime);
            col.addView(titleRow);

            // Date in primary colour (uppercase)
            TextView tvDate = new TextView(this);
            tvDate.setText(formatDate(r.createdAt).toUpperCase(Locale.getDefault()));
            tvDate.setTextColor(getResources().getColor(R.color.primary, null));
            tvDate.setTextSize(11f);
            tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDate.setLetterSpacing(0.08f);
            LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateLp.setMargins(0, (int)(3*dp), 0, (int)(5*dp));
            tvDate.setLayoutParams(dateLp);
            col.addView(tvDate);

            // Content preview (2 lines)
            if (r.content != null && !r.content.isEmpty()) {
                TextView tvContent = new TextView(this);
                tvContent.setText(r.content);
                tvContent.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
                tvContent.setTextSize(13f);
                tvContent.setMaxLines(2);
                tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvContent.setLineSpacing(0f, 1.4f);
                col.addView(tvContent);
            }

            card.addView(col);

            // Long-press → toggle favourite
            card.setOnLongClickListener(v -> {
                executor.execute(() -> {
                    r.isFavorite = (r.isFavorite == 1) ? 0 : 1;
                    reflectionDao.updateReflection(r);
                    runOnUiThread(() -> {
                        String msg = r.isFavorite == 1 ? "⭐ Added to favorites" : "Removed from favorites";
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        loadData();
                    });
                });
                return true;
            });

            entriesContainer.addView(card);
        }
    }

    // ── Mood helpers ───────────────────────────────────────────────────────

    private int moodBoxDrawable(String mood) {
        if (mood == null) return R.drawable.bg_mood_blue;
        switch (mood) {
            case "happy":   return R.drawable.bg_mood_green;
            case "sad":     return R.drawable.bg_mood_amber;
            case "neutral": return R.drawable.bg_mood_blue;
            case "anxious": return R.drawable.bg_mood_purple;
            case "calm":
            default:        return R.drawable.bg_mood_blue;
        }
    }

    private int moodIconDrawable(String mood) {
        if (mood == null) return R.drawable.ic_sentiment_satisfied;
        switch (mood) {
            case "happy":   return R.drawable.ic_sentiment_satisfied;
            case "sad":     return R.drawable.ic_sentiment_dissatisfied;
            case "neutral": return R.drawable.ic_sentiment_neutral;
            case "anxious": return R.drawable.ic_psychology;
            case "calm":
            default:        return R.drawable.ic_sentiment_satisfied;
        }
    }

    private int moodIconColor(String mood) {
        if (mood == null) return R.color.colorBlueIcon;
        switch (mood) {
            case "happy":   return R.color.colorGreenIcon;
            case "sad":     return R.color.colorTextSecondary;
            case "neutral": return R.color.colorBlueIcon;
            case "anxious": return R.color.colorPurpleIcon;
            case "calm":
            default:        return R.color.colorBlueIcon;
        }
    }

    // ── Date/time formatting ───────────────────────────────────────────────

    /** "2026-03-04 09:41:00" → "9:41 AM" */
    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.length() < 16) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("h:mm a", Locale.getDefault());
            java.util.Date d = in.parse(createdAt);
            return d != null ? out.format(d) : "";
        } catch (Exception e) { return ""; }
    }

    /** "2026-03-04 09:41:00" → "Mar 4" */
    private String formatDate(String createdAt) {
        if (createdAt == null || createdAt.length() < 10) return "";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM d", Locale.getDefault());
            java.util.Date d = in.parse(createdAt.substring(0, 10));
            return d != null ? out.format(d) : "";
        } catch (Exception e) { return ""; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}


