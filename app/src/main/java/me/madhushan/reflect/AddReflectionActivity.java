package me.madhushan.reflect;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Reflection;
import me.madhushan.reflect.database.ReflectionDao;
import me.madhushan.reflect.utils.SessionManager;

public class AddReflectionActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private String selectedMood = "calm"; // default
    private ReflectionDao reflectionDao;
    private SessionManager sessionManager;
    private ExecutorService executor;

    // Mood background frames for selection highlight
    private FrameLayout bgHappy, bgCalm, bgNeutral, bgSad, bgAnxious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reflection);

        sessionManager = new SessionManager(this);
        reflectionDao  = AppDatabase.getInstance(this).reflectionDao();
        executor       = Executors.newSingleThreadExecutor();

        etTitle   = findViewById(R.id.et_reflection_title);
        etContent = findViewById(R.id.et_reflection_content);

        bgHappy   = findViewById(R.id.mood_happy_bg);
        bgCalm    = findViewById(R.id.mood_calm_bg);
        bgNeutral = findViewById(R.id.mood_neutral_bg);
        bgSad     = findViewById(R.id.mood_sad_bg);
        bgAnxious = findViewById(R.id.mood_anxious_bg);

        // Select calm by default
        selectMood("calm");

        // Mood click listeners
        findViewById(R.id.mood_happy).setOnClickListener(v   -> selectMood("happy"));
        findViewById(R.id.mood_calm).setOnClickListener(v    -> selectMood("calm"));
        findViewById(R.id.mood_neutral).setOnClickListener(v -> selectMood("neutral"));
        findViewById(R.id.mood_sad).setOnClickListener(v     -> selectMood("sad"));
        findViewById(R.id.mood_anxious).setOnClickListener(v -> selectMood("anxious"));

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Save
        findViewById(R.id.btn_save_reflection).setOnClickListener(v -> saveReflection());
    }

    private void selectMood(String mood) {
        selectedMood = mood;
        int greenBg    = R.drawable.bg_circle_green;
        int blueBg     = R.drawable.bg_circle_blue;
        int purpleBg   = R.drawable.bg_circle_purple;

        // Reset all
        bgHappy.setBackgroundResource(greenBg);
        bgCalm.setBackgroundResource(blueBg);
        bgNeutral.setBackgroundResource(blueBg);
        bgSad.setBackgroundResource(blueBg);
        bgAnxious.setBackgroundResource(purpleBg);

        // Highlight selected with primary tint border (reuse bg_card_primary gives too much fill;
        // we scale it up slightly as a visual cue via elevation)
        FrameLayout selected = null;
        switch (mood) {
            case "happy":   selected = bgHappy;   break;
            case "calm":    selected = bgCalm;    break;
            case "neutral": selected = bgNeutral; break;
            case "sad":     selected = bgSad;     break;
            case "anxious": selected = bgAnxious; break;
        }
        if (selected != null) {
            selected.setScaleX(1.15f);
            selected.setScaleY(1.15f);
            selected.setElevation(8f);
        }
        // Reset scale for others
        FrameLayout[] all = {bgHappy, bgCalm, bgNeutral, bgSad, bgAnxious};
        for (FrameLayout f : all) {
            if (f != selected) {
                f.setScaleX(1f);
                f.setScaleY(1f);
                f.setElevation(0f);
            }
        }
    }

    private void saveReflection() {
        String title   = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }
        if (content.isEmpty()) {
            etContent.setError("Please write something");
            etContent.requestFocus();
            return;
        }

        int userId  = sessionManager.getUserId();
        String now  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Reflection r = new Reflection();
        r.userId    = userId;
        r.title     = title;
        r.mood      = selectedMood;
        r.content   = content;
        r.isFavorite = 0;
        r.createdAt  = now;

        executor.execute(() -> {
            reflectionDao.insertReflection(r);
            runOnUiThread(() -> {
                Toast.makeText(this, "Reflection saved!", Toast.LENGTH_SHORT).show();
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



