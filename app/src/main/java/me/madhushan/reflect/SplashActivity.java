package me.madhushan.reflect;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import me.madhushan.reflect.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2800;
    private TextView tvProgressPercent;
    private View progressFill;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen – hide status bar & navigation
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        sessionManager = new SessionManager(this);

        // ── If already logged in, skip splash entirely ──────────────────────
        if (sessionManager.isLoggedIn()) {
            goTo(MainActivity.class);
            return; // don't setContentView or start animation
        }

        // ── No session — show splash, then go to Login ───────────────────────
        setContentView(R.layout.activity_splash);

        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        progressFill      = findViewById(R.id.progress_fill);
        View progressContainer = findViewById(R.id.progress_container);

        // Wait until layout is measured so we know the track width
        progressContainer.post(this::startProgressAnimation);

        // Decide where to go after splash
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> goTo(LoginActivity.class),
                SPLASH_DURATION_MS
        );
    }

    private void startProgressAnimation() {
        // The progress track is the FrameLayout parent of progressFill
        View progressTrack = (View) progressFill.getParent();
        int trackWidth = progressTrack.getWidth();

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(SPLASH_DURATION_MS - 300);
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            tvProgressPercent.setText(getString(R.string.splash_progress_format, progress));
            progressFill.getLayoutParams().width = (int) (trackWidth * progress / 100f);
            progressFill.requestLayout();
        });
        animator.start();
    }

    private void goTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
