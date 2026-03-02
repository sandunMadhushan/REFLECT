package me.madhushan.reflect;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

public class GoalDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "goal_id";

    private int goalId;
    private Goal currentGoal;
    private GoalDao goalDao;
    private ExecutorService executor;

    private TextView tvTitle, tvDescription, tvCategory, tvPriority, tvDeadline, tvCreated;
    private TextView tvProgressPct, tvMarkLabel, tvNoReflections;
    private CircularProgressView circularProgress;
    private LinearLayout reflectionsContainer, btnMarkAchieved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_details);

        goalId  = getIntent().getIntExtra(EXTRA_GOAL_ID, -1);
        goalDao = AppDatabase.getInstance(this).goalDao();
        executor = Executors.newSingleThreadExecutor();

        tvTitle        = findViewById(R.id.tv_goal_title);
        tvDescription  = findViewById(R.id.tv_goal_description);
        tvCategory     = findViewById(R.id.tv_category_badge);
        tvPriority     = findViewById(R.id.tv_priority_val);
        tvDeadline     = findViewById(R.id.tv_deadline_val);
        tvCreated      = findViewById(R.id.tv_created_val);
        tvProgressPct  = findViewById(R.id.tv_progress_pct);
        circularProgress = findViewById(R.id.circular_progress);
        reflectionsContainer = findViewById(R.id.reflections_container);
        tvNoReflections = findViewById(R.id.tv_no_reflections);
        btnMarkAchieved = findViewById(R.id.btn_mark_achieved);
        tvMarkLabel    = findViewById(R.id.tv_mark_btn_label);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_edit).setOnClickListener(v -> showEditDialog());
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());
        btnMarkAchieved.setOnClickListener(v -> toggleAchieved());
        findViewById(R.id.btn_add_reflection).setOnClickListener(v -> showAddReflectionDialog());

        loadGoal();
    }

    private void loadGoal() {
        executor.execute(() -> {
            currentGoal = goalDao.getGoalById(goalId);
            runOnUiThread(this::bindGoal);
        });
    }

    private void bindGoal() {
        if (currentGoal == null) { finish(); return; }

        tvTitle.setText(currentGoal.title);
        tvDescription.setText(currentGoal.description != null ? currentGoal.description : "");
        tvDescription.setVisibility(currentGoal.description != null && !currentGoal.description.isEmpty()
                ? View.VISIBLE : View.GONE);

        tvCategory.setText(currentGoal.category != null ? currentGoal.category : "Personal Growth");
        tvPriority.setText(capitalize(currentGoal.priority != null ? currentGoal.priority : "medium"));
        tvDeadline.setText(currentGoal.deadline != null ? currentGoal.deadline : "—");
        tvCreated.setText(currentGoal.createdAt != null ? currentGoal.createdAt.substring(5) : "—"); // MM-DD

        // Progress: 0% if active, 100% if achieved
        float progress = currentGoal.isAchieved == 1 ? 1f : 0f;
        circularProgress.setProgress(progress);
        tvProgressPct.setText(currentGoal.isAchieved == 1 ? "100%" : "0%");

        // Mark button label
        tvMarkLabel.setText(currentGoal.isAchieved == 1 ? "Mark as Active" : "Mark as Achieved");

        // Reflections
        bindReflections();
    }

    private void bindReflections() {
        reflectionsContainer.removeAllViews();
        String notes = currentGoal.reflectionNotes;
        if (notes == null || notes.trim().isEmpty()) {
            tvNoReflections.setVisibility(View.VISIBLE);
            return;
        }
        tvNoReflections.setVisibility(View.GONE);
        String[] entries = notes.split("\\|\\|");
        float density = getResources().getDisplayMetrics().density;
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card_dark);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, (int)(10 * density));
            card.setLayoutParams(lp);
            int pad = (int)(14 * density);
            card.setPadding(pad, pad, pad, pad);

            TextView tvNote = new TextView(this);
            tvNote.setText(entry.trim());
            tvNote.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
            tvNote.setTextSize(13f);
            tvNote.setLineSpacing(4 * density, 1f);
            card.addView(tvNote);
            reflectionsContainer.addView(card);
        }
    }

    private void toggleAchieved() {
        if (currentGoal == null) return;
        int newStatus = currentGoal.isAchieved == 1 ? 0 : 1;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        executor.execute(() -> {
            currentGoal.isAchieved = newStatus;
            currentGoal.updatedAt  = today;
            goalDao.updateGoal(currentGoal);
            runOnUiThread(() -> {
                bindGoal();
                setResult(RESULT_OK);
                Toast.makeText(this,
                        newStatus == 1 ? "🎉 Goal achieved!" : "Goal marked as active",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showEditDialog() {
        if (currentGoal == null) return;
        final EditText input = new EditText(this);
        input.setText(currentGoal.title);
        input.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Edit Goal Title")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
                        executor.execute(() -> {
                            currentGoal.title = newTitle;
                            currentGoal.updatedAt = today;
                            goalDao.updateGoal(currentGoal);
                            runOnUiThread(() -> {
                                bindGoal();
                                setResult(RESULT_OK);
                            });
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"" + (currentGoal != null ? currentGoal.title : "") + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    executor.execute(() -> {
                        if (currentGoal != null) goalDao.deleteGoal(currentGoal);
                        runOnUiThread(() -> {
                            setResult(RESULT_OK);
                            Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddReflectionDialog() {
        final EditText input = new EditText(this);
        input.setHint("Write your reflection...");
        input.setMinLines(3);
        input.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Add Reflection")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String note = input.getText().toString().trim();
                    if (!note.isEmpty()) {
                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Calendar.getInstance().getTime());
                        executor.execute(() -> {
                            String existing = currentGoal.reflectionNotes != null ? currentGoal.reflectionNotes : "";
                            currentGoal.reflectionNotes = existing.isEmpty() ? note : existing + "||" + note;
                            currentGoal.updatedAt = today;
                            goalDao.updateGoal(currentGoal);
                            runOnUiThread(() -> {
                                bindReflections();
                                setResult(RESULT_OK);
                                Toast.makeText(this, "Reflection saved ✍️", Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}

