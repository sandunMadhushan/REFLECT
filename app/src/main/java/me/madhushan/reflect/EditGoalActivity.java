package me.madhushan.reflect;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Goal;
import me.madhushan.reflect.database.GoalDao;

public class EditGoalActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "goal_id";

    private TextInputEditText etTitle, etDescription;
    private AutoCompleteTextView spinnerCategory;
    private TextView tvDeadline, priorityLow, priorityMedium, priorityHigh;

    private String selectedPriority = "medium";
    private String selectedDeadline = null;

    private GoalDao goalDao;
    private ExecutorService executor;
    private Goal currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goal);

        goalDao  = AppDatabase.getInstance(this).goalDao();
        executor = Executors.newSingleThreadExecutor();

        etTitle         = findViewById(R.id.et_title);
        etDescription   = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvDeadline      = findViewById(R.id.tv_deadline);
        priorityLow     = findViewById(R.id.priority_low);
        priorityMedium  = findViewById(R.id.priority_medium);
        priorityHigh    = findViewById(R.id.priority_high);

        // Category dropdown
        String[] categories = {"Personal Growth", "Health & Fitness", "Career & Finance", "Relationships"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        // Priority buttons
        priorityLow.setOnClickListener(v    -> selectPriority("low"));
        priorityMedium.setOnClickListener(v -> selectPriority("medium"));
        priorityHigh.setOnClickListener(v   -> selectPriority("high"));

        // Date picker
        findViewById(R.id.btn_pick_date).setOnClickListener(v -> showDatePicker());

        // Back / Cancel
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        // Save
        findViewById(R.id.btn_save_goal).setOnClickListener(v -> saveGoal());

        // Load existing goal data
        int goalId = getIntent().getIntExtra(EXTRA_GOAL_ID, -1);
        if (goalId == -1) { finish(); return; }

        executor.execute(() -> {
            currentGoal = goalDao.getGoalById(goalId);
            runOnUiThread(this::prefillFields);
        });
    }

    private void prefillFields() {
        if (currentGoal == null) { finish(); return; }

        // Title
        etTitle.setText(currentGoal.title);
        etTitle.setSelection(etTitle.length()); // cursor at end

        // Description
        if (currentGoal.description != null) {
            etDescription.setText(currentGoal.description);
        }

        // Category
        if (currentGoal.category != null && !currentGoal.category.isEmpty()) {
            spinnerCategory.setText(currentGoal.category, false);
        }

        // Priority
        String p = currentGoal.priority != null ? currentGoal.priority : "medium";
        selectPriority(p);

        // Deadline
        if (currentGoal.deadline != null && !currentGoal.deadline.isEmpty()) {
            selectedDeadline = currentGoal.deadline;
            tvDeadline.setText(currentGoal.deadline);
            tvDeadline.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        }
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;
        resetPriorityUI();
        int white = getResources().getColor(R.color.white, null);
        switch (priority) {
            case "low":
                priorityLow.setBackgroundResource(R.drawable.bg_chip_active);
                priorityLow.setTextColor(white);
                break;
            case "high":
                priorityHigh.setBackgroundResource(R.drawable.bg_chip_active);
                priorityHigh.setTextColor(white);
                break;
            default: // medium
                priorityMedium.setBackgroundResource(R.drawable.bg_chip_active);
                priorityMedium.setTextColor(white);
                break;
        }
    }

    private void resetPriorityUI() {
        int secondary = getResources().getColor(R.color.colorTextSecondary, null);
        priorityLow.setBackgroundResource(R.drawable.bg_chip_inactive);
        priorityLow.setTextColor(secondary);
        priorityMedium.setBackgroundResource(R.drawable.bg_chip_inactive);
        priorityMedium.setTextColor(secondary);
        priorityHigh.setBackgroundResource(R.drawable.bg_chip_inactive);
        priorityHigh.setTextColor(secondary);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // Pre-select existing deadline date if set
        if (selectedDeadline != null && !selectedDeadline.isEmpty()) {
            try {
                String[] parts = selectedDeadline.split("-");
                cal.set(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
            } catch (Exception ignored) {}
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            selectedDeadline = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selected.getTime());
            tvDeadline.setText(day + "/" + (month + 1) + "/" + year);
            tvDeadline.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveGoal() {
        if (currentGoal == null) return;

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc  = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String cat   = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Please enter a goal title");
            etTitle.requestFocus();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        executor.execute(() -> {
            currentGoal.title       = title;
            currentGoal.description = desc;
            currentGoal.category    = cat.isEmpty() ? "Personal Growth" : cat;
            currentGoal.priority    = selectedPriority;
            currentGoal.deadline    = selectedDeadline;
            currentGoal.updatedAt   = today;
            goalDao.updateGoal(currentGoal);

            runOnUiThread(() -> {
                Toast.makeText(this, "Goal updated! ✅", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}
