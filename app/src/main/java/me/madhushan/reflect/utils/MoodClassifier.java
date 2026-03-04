package me.madhushan.reflect.utils;

import android.content.Context;
import android.util.Log;

import java.util.Locale;

/**
 * MoodClassifier — On-device AI mood detection from journal text.
 *
 * Currently uses a keyword-based approach that works without any model file.
 * To enable TFLite mode: train the model in Google Colab (see colab/mood_classifier_colab.ipynb),
 * add mood_classifier.tflite + mood_vocab.txt to app/src/main/assets/, then
 * add `implementation("org.tensorflow:tensorflow-lite:2.13.0")` to build.gradle.kts
 * and uncomment the TFLite code sections below.
 *
 * Output labels (in order): happy, calm, neutral, sad, anxious
 */
public class MoodClassifier {

    private static final String TAG = "MoodClassifier";

    // Label order MUST match Colab training label encoding
    public static final String[] LABELS = {"happy", "calm", "neutral", "sad", "anxious"};

    // TFLite is disabled until model is trained — keyword fallback always active
    private final boolean modelLoaded = false;

    public MoodClassifier(Context context) {
        Log.i(TAG, "MoodClassifier initialized — using keyword-based mood detection.");
        Log.i(TAG, "To enable AI mode: train model in Colab and add .tflite to assets/");
    }

    /**
     * Predict mood from text.
     * @param text  User's journal entry content
     * @return  One of: "happy", "calm", "neutral", "sad", "anxious"
     */
    public String predict(String text) {
        if (text == null || text.trim().isEmpty()) return "neutral";
        return predictWithKeywords(text);
    }

    /**
     * Returns confidence scores for all moods (0.0 – 1.0).
     * Useful to show a confidence bar in the UI.
     */
    public float[] getScores(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[]{0.2f, 0.2f, 0.2f, 0.2f, 0.2f};
        }
        return keywordScores(text.toLowerCase(Locale.getDefault()));
    }

    /** Returns true if the TFLite model is loaded */
    public boolean isModelLoaded() {
        return modelLoaded;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Keyword-based fallback (no model needed)
    // ──────────────────────────────────────────────────────────────────────────

    private static final String[] HAPPY_WORDS = {
        "happy","joy","excited","amazing","great","wonderful","fantastic","love",
        "celebrate","grateful","thankful","proud","glad","thrilled","excellent",
        "awesome","blessed","positive","bright","smile","laugh","fun","enjoy",
        "achieved","success","accomplished","best","perfect","delighted"
    };
    private static final String[] CALM_WORDS = {
        "calm","peaceful","relaxed","serene","quiet","tranquil","gentle","still",
        "content","comfortable","mindful","balanced","easy","breathe","steady",
        "clarity","centered","present","meditation","rest","slow","flowing"
    };
    private static final String[] SAD_WORDS = {
        "sad","unhappy","cry","crying","tears","down","depressed","lonely","miss",
        "lost","hurt","pain","broken","disappointed","upset","grief","sorrow",
        "empty","hopeless","gloomy","dark","low","miserable","heartbroken","alone"
    };
    private static final String[] ANXIOUS_WORDS = {
        "anxious","worried","nervous","stressed","scared","fear","panic","overwhelmed",
        "tense","uneasy","afraid","dread","pressure","rushing","busy","deadline",
        "exhausted","tired","too much","can't","cannot","struggling","failing","fail",
        "uncertain","insecure","restless","jittery","freaking"
    };

    private String predictWithKeywords(String text) {
        float[] scores = keywordScores(text.toLowerCase(Locale.getDefault()));
        int best = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[best]) best = i;
        }
        return LABELS[best];
    }

    private float[] keywordScores(String lower) {
        float[] scores = new float[5]; // happy, calm, neutral, sad, anxious
        scores[0] = countMatches(lower, HAPPY_WORDS);
        scores[1] = countMatches(lower, CALM_WORDS);
        scores[2] = 0.3f; // slight neutral baseline
        scores[3] = countMatches(lower, SAD_WORDS);
        scores[4] = countMatches(lower, ANXIOUS_WORDS);

        // Normalize
        float sum = 0;
        for (float s : scores) sum += s;
        if (sum < 0.01f) {
            scores[2] = 1f; // pure neutral if no keywords matched
            sum = 1f;
        }
        for (int i = 0; i < scores.length; i++) scores[i] /= sum;
        return scores;
    }

    private float countMatches(String text, String[] keywords) {
        float count = 0;
        for (String kw : keywords) {
            if (text.contains(kw)) count += 1f;
        }
        return count;
    }

    public void close() {
        // Nothing to close — TFLite not loaded
    }
}
