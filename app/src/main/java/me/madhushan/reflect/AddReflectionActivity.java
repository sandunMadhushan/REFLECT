package me.madhushan.reflect;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import me.madhushan.reflect.utils.MoodClassifier;
import me.madhushan.reflect.utils.SessionManager;

public class AddReflectionActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private String selectedMood = "calm";
    private ReflectionDao reflectionDao;
    private SessionManager sessionManager;
    private ExecutorService executor;
    private MoodClassifier moodClassifier;

    private FrameLayout bgHappy, bgCalm, bgNeutral, bgSad, bgAnxious;

    private LinearLayout aiResultRow, aiBarsRow;
    private TextView tvAiDetectedMood, tvAiConfidence, tvBtnDetectLabel;
    private ProgressBar barHappy, barCalm, barNeutral, barSad, barAnxious;
    private TextView pctHappy, pctCalm, pctNeutral, pctSad, pctAnxious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reflection);

        sessionManager  = new SessionManager(this);
        reflectionDao   = AppDatabase.getInstance(this).reflectionDao();
        executor        = Executors.newSingleThreadExecutor();
        moodClassifier  = new MoodClassifier(this);

        etTitle   = findViewById(R.id.et_reflection_title);
        etContent = findViewById(R.id.et_reflection_content);

        bgHappy   = findViewById(R.id.mood_happy_bg);
        bgCalm    = findViewById(R.id.mood_calm_bg);
        bgNeutral = findViewById(R.id.mood_neutral_bg);
        bgSad     = findViewById(R.id.mood_sad_bg);
        bgAnxious = findViewById(R.id.mood_anxious_bg);

        aiResultRow      = findViewById(R.id.ai_result_row);
        aiBarsRow        = findViewById(R.id.ai_bars_row);
        tvAiDetectedMood = findViewById(R.id.tv_ai_detected_mood);
        tvAiConfidence   = findViewById(R.id.tv_ai_confidence);
        tvBtnDetectLabel = findViewById(R.id.tv_btn_detect_label);
        barHappy         = findViewById(R.id.bar_happy);
        barCalm          = findViewById(R.id.bar_calm);
        barNeutral       = findViewById(R.id.bar_neutral);
        barSad           = findViewById(R.id.bar_sad);
        barAnxious       = findViewById(R.id.bar_anxious);
        pctHappy         = findViewById(R.id.pct_happy);
        pctCalm          = findViewById(R.id.pct_calm);
        pctNeutral       = findViewById(R.id.pct_neutral);
        pctSad           = findViewById(R.id.pct_sad);
        pctAnxious       = findViewById(R.id.pct_anxious);

        TextView tvAiLabel = findViewById(R.id.tv_ai_label);
        tvAiLabel.setText(moodClassifier.isModelLoaded()
                ? getString(R.string.ai_mode_tflite)
                : getString(R.string.ai_mode_smart));

        selectMood("calm");

        findViewById(R.id.mood_happy).setOnClickListener(v   -> selectMood("happy"));
        findViewById(R.id.mood_calm).setOnClickListener(v    -> selectMood("calm"));
        findViewById(R.id.mood_neutral).setOnClickListener(v -> selectMood("neutral"));
        findViewById(R.id.mood_sad).setOnClickListener(v     -> selectMood("sad"));
        findViewById(R.id.mood_anxious).setOnClickListener(v -> selectMood("anxious"));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_ai_detect).setOnClickListener(v -> detectMoodFromText());
        findViewById(R.id.btn_save_reflection).setOnClickListener(v -> saveReflection());
    }

    private void detectMoodFromText() {
        String content = etContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, getString(R.string.ai_write_first), Toast.LENGTH_SHORT).show();
            return;
        }

        tvBtnDetectLabel.setText(getString(R.string.ai_detecting));
        findViewById(R.id.btn_ai_detect).setEnabled(false);

        executor.execute(() -> {
            float[] scores  = moodClassifier.getScores(content);
            String detected = MoodClassifier.LABELS[argmax(scores)];
            float confidence = scores[argmax(scores)];

            runOnUiThread(() -> {
                selectMood(detected);

                aiResultRow.setVisibility(View.VISIBLE);
                aiBarsRow.setVisibility(View.VISIBLE);

                String emoji = moodToEmoji(detected);
                tvAiDetectedMood.setText(emoji + " " + capitalize(detected));
                tvAiConfidence.setText(String.format(Locale.getDefault(), "%.0f%%", confidence * 100));

                setBar(barHappy,   pctHappy,   scores[0]);
                setBar(barCalm,    pctCalm,    scores[1]);
                setBar(barNeutral, pctNeutral, scores[2]);
                setBar(barSad,     pctSad,     scores[3]);
                setBar(barAnxious, pctAnxious, scores[4]);

                tvBtnDetectLabel.setText(getString(R.string.ai_redetect));
                findViewById(R.id.btn_ai_detect).setEnabled(true);

                Toast.makeText(this,
                        emoji + " " + capitalize(detected) +
                        " " + String.format(Locale.getDefault(), "(%.0f%%)", confidence * 100),
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void setBar(ProgressBar bar, TextView pct, float score) {
        int val = Math.round(score * 100);
        bar.setProgress(val);
        pct.setText(String.format(Locale.getDefault(), "%d%%", val));
    }

    private int argmax(float[] arr) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[best]) best = i;
        }
        return best;
    }

    private String moodToEmoji(String mood) {
        switch (mood) {
            case "happy":   return "😄";
            case "calm":    return "😌";
            case "sad":     return "😔";
            case "anxious": return "😰";
            default:        return "😐";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void selectMood(String mood) {
        selectedMood = mood;
        int greenBg  = R.drawable.bg_circle_green;
        int blueBg   = R.drawable.bg_circle_blue;
        int purpleBg = R.drawable.bg_circle_purple;

        bgHappy.setBackgroundResource(greenBg);
        bgCalm.setBackgroundResource(blueBg);
        bgNeutral.setBackgroundResource(blueBg);
        bgSad.setBackgroundResource(blueBg);
        bgAnxious.setBackgroundResource(purpleBg);

        FrameLayout selected = null;
        switch (mood) {
            case "happy":   selected = bgHappy;   break;
            case "calm":    selected = bgCalm;    break;
            case "neutral": selected = bgNeutral; break;
            case "sad":     selected = bgSad;     break;
            case "anxious": selected = bgAnxious; break;
        }
        FrameLayout[] all = {bgHappy, bgCalm, bgNeutral, bgSad, bgAnxious};
        for (FrameLayout f : all) {
            if (f == selected) {
                f.setScaleX(1.15f);
                f.setScaleY(1.15f);
                f.setElevation(8f);
            } else {
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

        int userId = sessionManager.getUserId();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Reflection r = new Reflection();
        r.userId     = userId;
        r.title      = title;
        r.mood       = selectedMood;
        r.content    = content;
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
        if (moodClassifier != null) moodClassifier.close();
    }
}

