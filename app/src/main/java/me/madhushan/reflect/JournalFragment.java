package me.madhushan.reflect;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class JournalFragment extends Fragment {

    private ReflectionDao reflectionDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    private LinearLayout entriesContainer, emptyState, dateFilterBanner;
    private TextView filterAll, filterWeek, filterMonth, filterFavorites;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private TextView tvSelectedDate, btnClearDate;
    private String currentFilter = "all";

    // null means no date filter active; non-null means user picked a specific date
    private String selectedDate = null; // "yyyy-MM-dd"

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        sessionManager   = new SessionManager(requireContext());
        reflectionDao    = AppDatabase.getInstance(requireContext()).reflectionDao();
        executor         = Executors.newSingleThreadExecutor();

        entriesContainer  = v.findViewById(R.id.entries_container);
        emptyState        = v.findViewById(R.id.empty_state);
        tvEmptyTitle      = v.findViewById(R.id.tv_empty_title);
        tvEmptySubtitle   = v.findViewById(R.id.tv_empty_subtitle);
        dateFilterBanner  = v.findViewById(R.id.date_filter_banner);
        tvSelectedDate    = v.findViewById(R.id.tv_selected_date);
        btnClearDate      = v.findViewById(R.id.btn_clear_date);
        filterAll         = v.findViewById(R.id.filter_all);
        filterWeek        = v.findViewById(R.id.filter_week);
        filterMonth       = v.findViewById(R.id.filter_month);
        filterFavorites   = v.findViewById(R.id.filter_favorites);

        filterAll.setOnClickListener(b       -> setFilter("all"));
        filterWeek.setOnClickListener(b      -> setFilter("week"));
        filterMonth.setOnClickListener(b     -> setFilter("month"));
        filterFavorites.setOnClickListener(b -> setFilter("favorites"));

        // Calendar icon — show DatePickerDialog
        v.findViewById(R.id.btn_calendar).setOnClickListener(b -> showDatePicker());

        // Clear date filter button
        btnClearDate.setOnClickListener(b -> clearDateFilter());

        loadData();
    }

    @Override public void onResume() { super.onResume(); loadData(); }

    // ── Date picker ───────────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // If a date is already selected, pre-populate the picker with that date
        if (selectedDate != null) {
            try {
                java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate);
                if (d != null) cal.setTime(d);
            } catch (Exception ignored) {}
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, dayOfMonth) -> {
                    // Build the date string
                    String picked = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            year, month + 1, dayOfMonth);
                    applyDateFilter(picked);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        // Don't allow picking future dates
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.setTitle("Filter by date");
        dialog.show();
    }

    private void applyDateFilter(String date) {
        selectedDate = date;
        // Format for display: "March 12, 2026"
        try {
            java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            String display = d != null
                    ? new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(d)
                    : date;
            tvSelectedDate.setText("Showing: " + display);
        } catch (Exception e) {
            tvSelectedDate.setText("Showing: " + date);
        }
        dateFilterBanner.setVisibility(View.VISIBLE);
        // Reset chip filters when date is picked — date takes priority
        currentFilter = "all";
        applyFilterUI();
        loadData();
    }

    private void clearDateFilter() {
        selectedDate = null;
        dateFilterBanner.setVisibility(View.GONE);
        loadData();
    }

    // ── Filter chips ──────────────────────────────────────────────────────

    private void setFilter(String f) {
        // Switching a chip clears any active date filter
        selectedDate = null;
        dateFilterBanner.setVisibility(View.GONE);
        currentFilter = f;
        applyFilterUI();
        loadData();
    }

    private void applyFilterUI() {
        int white    = getResources().getColor(R.color.white, null);
        int inactive = getResources().getColor(R.color.colorTextSecondary, null);

        filterAll.setBackgroundResource(currentFilter.equals("all") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterAll.setTextColor(currentFilter.equals("all") ? white : inactive);
        filterAll.setTypeface(null, currentFilter.equals("all") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        filterWeek.setBackgroundResource(currentFilter.equals("week") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterWeek.setTextColor(currentFilter.equals("week") ? white : inactive);
        filterWeek.setTypeface(null, currentFilter.equals("week") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        filterMonth.setBackgroundResource(currentFilter.equals("month") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterMonth.setTextColor(currentFilter.equals("month") ? white : inactive);
        filterMonth.setTypeface(null, currentFilter.equals("month") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        filterFavorites.setBackgroundResource(currentFilter.equals("favorites") ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filterFavorites.setTextColor(currentFilter.equals("favorites") ? white : inactive);
        filterFavorites.setTypeface(null, currentFilter.equals("favorites") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    // ── Data loading ──────────────────────────────────────────────────────

    public void loadData() {
        if (!isAdded() || sessionManager == null) return;
        int userId = sessionManager.getUserId();
        final String dateSnap = selectedDate; // capture for background thread

        executor.execute(() -> {
            List<Reflection> all = reflectionDao.getReflectionsForUser(userId);
            List<Reflection> filtered = new ArrayList<>();

            if (dateSnap != null) {
                // Date filter takes priority over chip filters
                for (Reflection r : all) {
                    if (r.createdAt != null && r.createdAt.startsWith(dateSnap)) {
                        filtered.add(r);
                    }
                }
            } else {
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
                        String mp = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
                        for (Reflection r : all)
                            if (r.createdAt != null && r.createdAt.startsWith(mp)) filtered.add(r);
                        break;
                    case "favorites":
                        for (Reflection r : all) if (r.isFavorite == 1) filtered.add(r);
                        break;
                    default:
                        filtered.addAll(all);
                }
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                renderEntries(filtered, dateSnap);
            });
        });
    }

    // ── Rendering ─────────────────────────────────────────────────────────

    private void renderEntries(List<Reflection> entries, String dateSnap) {
        entriesContainer.removeAllViews();

        if (entries.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            if (dateSnap != null) {
                // Specific date was picked but nothing found
                String display = dateSnap;
                try {
                    java.util.Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateSnap);
                    if (d != null) display = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(d);
                } catch (Exception ignored) {}
                tvEmptyTitle.setText("No reflections on " + display);
                tvEmptySubtitle.setText("You didn't write anything on this day. Try a different date.");
            } else {
                switch (currentFilter) {
                    case "week":
                        tvEmptyTitle.setText("No reflections this week");
                        tvEmptySubtitle.setText("You haven't written anything this week. Start journaling today!");
                        break;
                    case "month":
                        tvEmptyTitle.setText("No reflections this month");
                        tvEmptySubtitle.setText("Nothing written this month yet. Tap + to add a reflection.");
                        break;
                    case "favorites":
                        tvEmptyTitle.setText("No favorites yet");
                        tvEmptySubtitle.setText("Long-press any reflection to mark it as a favourite.");
                        break;
                    default:
                        tvEmptyTitle.setText("No reflections yet");
                        tvEmptySubtitle.setText("Tap the + button to write your first reflection.");
                        break;
                }
            }
            return;
        }

        emptyState.setVisibility(View.GONE);

        float dp = getResources().getDisplayMetrics().density;
        for (Reflection r : entries) {
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackground(requireContext().getDrawable(R.drawable.bg_journal_card));
            card.setElevation(2*dp);
            card.setPadding((int)(16*dp),(int)(16*dp),(int)(16*dp),(int)(16*dp));
            card.setClickable(true); card.setFocusable(true);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0,0,0,(int)(12*dp)); card.setLayoutParams(clp);

            // Mood icon box
            FrameLayout iconBox = new FrameLayout(requireContext());
            int bsz = (int)(48*dp);
            LinearLayout.LayoutParams ibp = new LinearLayout.LayoutParams(bsz, bsz);
            ibp.setMarginEnd((int)(14*dp)); ibp.gravity = android.view.Gravity.TOP;
            iconBox.setLayoutParams(ibp);
            iconBox.setBackground(requireContext().getDrawable(moodBoxDrawable(r.mood)));
            ImageView moodIcon = new ImageView(requireContext());
            FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams((int)(26*dp),(int)(26*dp));
            fp.gravity = android.view.Gravity.CENTER;
            moodIcon.setLayoutParams(fp);
            moodIcon.setImageResource(moodIconDrawable(r.mood));
            moodIcon.setColorFilter(getResources().getColor(moodIconColor(r.mood), null));
            iconBox.addView(moodIcon); card.addView(iconBox);

            // Text column
            LinearLayout col = new LinearLayout(requireContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            LinearLayout titleRow = new LinearLayout(requireContext());
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            titleRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView tvTitle = new TextView(requireContext());
            tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvTitle.setText(r.title != null ? r.title : "Reflection");
            tvTitle.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            tvTitle.setTextSize(15f); tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitle.setMaxLines(1); tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
            titleRow.addView(tvTitle);
            TextView tvTime = new TextView(requireContext());
            tvTime.setText(formatTime(r.createdAt));
            tvTime.setTextColor(getResources().getColor(R.color.colorTextSecondary, null)); tvTime.setTextSize(11f);
            titleRow.addView(tvTime); col.addView(titleRow);

            TextView tvDate = new TextView(requireContext());
            tvDate.setText(formatDate(r.createdAt).toUpperCase(Locale.getDefault()));
            tvDate.setTextColor(getResources().getColor(R.color.primary, null));
            tvDate.setTextSize(11f); tvDate.setTypeface(null, android.graphics.Typeface.BOLD); tvDate.setLetterSpacing(0.08f);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dlp.setMargins(0,(int)(3*dp),0,(int)(5*dp)); tvDate.setLayoutParams(dlp);
            col.addView(tvDate);

            if (r.content != null && !r.content.isEmpty()) {
                TextView tvContent = new TextView(requireContext());
                tvContent.setText(r.content);
                tvContent.setTextColor(getResources().getColor(R.color.colorTextSecondary, null));
                tvContent.setTextSize(13f); tvContent.setMaxLines(2);
                tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvContent.setLineSpacing(0f, 1.4f); col.addView(tvContent);
            }
            card.addView(col);

            card.setOnLongClickListener(b -> {
                executor.execute(() -> {
                    r.isFavorite = r.isFavorite == 1 ? 0 : 1;
                    reflectionDao.updateReflection(r);
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), r.isFavorite == 1 ? "⭐ Favorited" : "Removed from favorites", Toast.LENGTH_SHORT).show();
                        loadData();
                    });
                });
                return true;
            });
            entriesContainer.addView(card);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private int moodBoxDrawable(String m) {
        if (m == null) return R.drawable.bg_mood_blue;
        switch (m) { case "happy": return R.drawable.bg_mood_green; case "sad": return R.drawable.bg_mood_amber;
            case "anxious": return R.drawable.bg_mood_purple; default: return R.drawable.bg_mood_blue; }
    }
    private int moodIconDrawable(String m) {
        if (m == null) return R.drawable.ic_sentiment_satisfied;
        switch (m) { case "happy": return R.drawable.ic_sentiment_satisfied;
            case "sad": return R.drawable.ic_sentiment_dissatisfied;
            case "neutral": return R.drawable.ic_sentiment_neutral;
            case "anxious": return R.drawable.ic_psychology;
            default: return R.drawable.ic_sentiment_satisfied; }
    }
    private int moodIconColor(String m) {
        if (m == null) return R.color.colorBlueIcon;
        switch (m) {
            case "happy":   return R.color.colorGreenIcon;
            case "sad":     return R.color.colorTextSecondary;
            case "neutral": return R.color.colorBlueIcon;
            case "anxious": return R.color.colorPurpleIcon;
            case "calm":
            default:        return R.color.colorBlueIcon;
        }
    }
    private String formatTime(String s) {
        if (s == null || s.length() < 16) return "";
        try { SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("h:mm a", Locale.getDefault());
            java.util.Date d = in.parse(s); return d != null ? out.format(d) : ""; } catch (Exception e) { return ""; }
    }
    private String formatDate(String s) {
        if (s == null || s.length() < 10) return "";
        try { SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM d", Locale.getDefault());
            java.util.Date d = in.parse(s.substring(0,10)); return d != null ? out.format(d) : ""; } catch (Exception e) { return ""; }
    }

    @Override public void onDestroy() { super.onDestroy(); if (executor != null) executor.shutdown(); }
}
