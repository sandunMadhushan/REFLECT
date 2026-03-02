package me.madhushan.reflect;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
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
import me.madhushan.reflect.utils.SessionManager;

public class AddGoalActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription;
    private AutoCompleteTextView spinnerCategory;
    private TextView tvDeadline, priorityLow, priorityMedium, priorityHigh;

    private String selectedPriority = "medium";
    private String selectedDeadline = null;

    private GoalDao goalDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        goalDao = AppDatabase.getInstance(this).goalDao();
        sessionManager = new SessionManager(this);
        executor = Executors.newSingleThreadExecutor();

        etTitle       = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvDeadline    = findViewById(R.id.tv_deadline);
        priorityLow   = findViewById(R.id.priority_low);
        priorityMedium = findViewById(R.id.priority_medium);
        priorityHigh  = findViewById(R.id.priority_high);

        // Category dropdown
        String[] categories = {"Personal Growth", "Health & Fitness", "Career & Finance", "Relationships"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(adapter);

        // Priority selection
        priorityLow.setOnClickListener(v -> selectPriority("low"));
        priorityMedium.setOnClickListener(v -> selectPriority("medium"));
        priorityHigh.setOnClickListener(v -> selectPriority("high"));

        // Date picker
        findViewById(R.id.btn_pick_date).setOnClickListener(v -> showDatePicker());

        // Back / Cancel
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        // Save
        findViewById(R.id.btn_save_goal).setOnClickListener(v -> saveGoal());
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;
        resetPriorityUI();
        switch (priority) {
            case "low":
                priorityLow.setBackgroundResource(R.drawable.bg_chip_active);
                priorityLow.setTextColor(getResources().getColor(R.color.white, null));
                priorityLow.setTextSize(13f);
                break;
            case "medium":
                priorityMedium.setBackgroundResource(R.drawable.bg_chip_active);
                priorityMedium.setTextColor(getResources().getColor(R.color.white, null));
                break;
            case "high":
                priorityHigh.setBackgroundResource(R.drawable.bg_chip_active);
                priorityHigh.setTextColor(getResources().getColor(R.color.white, null));
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
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDeadline = sdf.format(selected.getTime());
            tvDeadline.setText(day + "/" + (month + 1) + "/" + year);
            tvDeadline.setTextColor(getResources().getColor(R.color.colorTextPrimary, null));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveGoal() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc  = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String cat   = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Please enter a goal title");
            etTitle.requestFocus();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Goal goal = new Goal();
        goal.userId         = sessionManager.getUserId();
        goal.title          = title;
        goal.description    = desc;
        goal.category       = cat.isEmpty() ? "Personal Growth" : cat;
        goal.priority       = selectedPriority;
        goal.deadline       = selectedDeadline;
        goal.reflectionNotes = "";
        goal.isAchieved     = 0;
        goal.createdAt      = today;
        goal.updatedAt      = today;

        executor.execute(() -> {
            goalDao.insertGoal(goal);
            runOnUiThread(() -> {
                Toast.makeText(this, "Goal saved! 🎯", Toast.LENGTH_SHORT).show();
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

