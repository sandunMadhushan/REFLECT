package me.madhushan.reflect.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodClassifier {

    private static final String TAG = "MoodClassifier";

    public static final String[] LABELS = {"happy", "calm", "neutral", "sad", "anxious"};

    private static final String MODEL_FILE = "mood_classifier.tflite";
    private static final String VOCAB_FILE = "mood_vocab.txt";
    private static final int MAX_SEQ_LEN  = 50;

    private Interpreter tflite;
    private final Map<String, Integer> wordToId = new HashMap<>();
    private boolean modelLoaded = false;

    public MoodClassifier(Context context) {
        try {
            List<String> vocab = loadVocab(context);
            if (!vocab.isEmpty()) {
                for (int i = 0; i < vocab.size(); i++) {
                    wordToId.put(vocab.get(i), i);
                }
                MappedByteBuffer modelBuffer = loadModelFile(context);
                // Use plain constructor — avoids dependency on InterpreterApi
                tflite = new Interpreter(modelBuffer);
                // Quick test run to verify model works at runtime
                int[][] testInput  = new int[1][MAX_SEQ_LEN];
                float[][] testOut  = new float[1][5];
                tflite.run(testInput, testOut);
                modelLoaded = true;
                Log.i(TAG, "✅ TFLite model loaded — AI mood detection active!");
            } else {
                Log.i(TAG, "ℹ️  mood_vocab.txt not found — using keyword fallback");
            }
        } catch (Throwable e) {
            Log.i(TAG, "ℹ️  TFLite model unavailable — using keyword fallback. Reason: " + e.getMessage());
            modelLoaded = false;
            tflite = null;
        }
    }

    public String predict(String text) {
        if (text == null || text.trim().isEmpty()) return "neutral";
        if (modelLoaded) {
            try {
                float[] scores = runModel(text);
                int best = 0;
                for (int i = 1; i < scores.length; i++) {
                    if (scores[i] > scores[best]) best = i;
                }
                return LABELS[best];
            } catch (Throwable e) {
                Log.w(TAG, "Model inference failed, using keyword fallback: " + e.getMessage());
                modelLoaded = false;
            }
        }
        return predictWithKeywords(text);
    }

    public float[] getScores(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[]{0.2f, 0.2f, 0.2f, 0.2f, 0.2f};
        }
        if (modelLoaded) {
            try {
                return runModel(text);
            } catch (Throwable e) {
                Log.w(TAG, "Model inference failed, using keyword fallback: " + e.getMessage());
                modelLoaded = false;
            }
        }
        return keywordScores(text.toLowerCase(Locale.getDefault()));
    }

    public boolean isModelLoaded() { return modelLoaded; }

    public void close() {
        try {
            if (tflite != null) { tflite.close(); tflite = null; }
        } catch (Throwable e) { /* ignore */ }
    }

    // ── TFLite inference ─────────────────────────────────────────────────────

    private float[] runModel(String text) {
        int[] input = encodeText(text);
        int[][] inputArr   = {input};
        float[][] outputArr = new float[1][5];
        tflite.run(inputArr, outputArr);
        return outputArr[0];
    }

    private int[] encodeText(String text) {
        String[] tokens = text.toLowerCase(Locale.getDefault())
                .replaceAll("[^a-z ]", " ").trim().split("\\s+");
        int[] ids = new int[MAX_SEQ_LEN];
        for (int i = 0; i < Math.min(tokens.length, MAX_SEQ_LEN); i++) {
            Integer id = wordToId.get(tokens[i]);
            ids[i] = (id != null) ? id : 1;
        }
        return ids;
    }

    private MappedByteBuffer loadModelFile(Context ctx) throws IOException {
        AssetFileDescriptor fd = ctx.getAssets().openFd(MODEL_FILE);
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    private List<String> loadVocab(Context ctx) {
        List<String> vocab = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(ctx.getAssets().open(VOCAB_FILE)))) {
            String line;
            while ((line = reader.readLine()) != null) vocab.add(line.trim());
        } catch (IOException e) { /* not present — use keyword fallback */ }
        return vocab;
    }

    // ── Keyword fallback (no model needed) ───────────────────────────────────

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
        "exhausted","tired","struggling","failing","fail","uncertain","insecure",
        "restless","jittery","freaking","cannot","can't"
    };

    private String predictWithKeywords(String text) {
        float[] scores = keywordScores(text.toLowerCase(Locale.getDefault()));
        int best = 0;
        for (int i = 1; i < scores.length; i++) if (scores[i] > scores[best]) best = i;
        return LABELS[best];
    }

    private float[] keywordScores(String lower) {
        float[] scores = new float[5];
        scores[0] = countMatches(lower, HAPPY_WORDS);
        scores[1] = countMatches(lower, CALM_WORDS);
        scores[2] = 0.3f;
        scores[3] = countMatches(lower, SAD_WORDS);
        scores[4] = countMatches(lower, ANXIOUS_WORDS);
        float sum = 0;
        for (float s : scores) sum += s;
        if (sum < 0.01f) { scores[2] = 1f; sum = 1f; }
        for (int i = 0; i < scores.length; i++) scores[i] /= sum;
        return scores;
    }

    private float countMatches(String text, String[] keywords) {
        float count = 0;
        for (String kw : keywords) if (text.contains(kw)) count++;
        return count;
    }
}

