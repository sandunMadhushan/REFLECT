package me.madhushan.reflect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "reflect_onboarding";
    private static final String KEY_COMPLETED = "onboarding_completed";

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private MaterialButton btnNext;
    private TextView tvSkip;

    private static final int PAGE_COUNT = 3;
    private static final int[] PAGE_LAYOUTS = {
            R.layout.fragment_onboarding_1,
            R.layout.fragment_onboarding_2,
            R.layout.fragment_onboarding_3
    };

    /** Call this from SplashActivity to check if onboarding was already shown */
    public static boolean isCompleted(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_COMPLETED, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager   = findViewById(R.id.view_pager);
        dotsLayout  = findViewById(R.id.dots_layout);
        btnNext     = findViewById(R.id.btn_next);
        tvSkip      = findViewById(R.id.tv_skip);

        viewPager.setAdapter(new OnboardingPagerAdapter());
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == PAGE_COUNT - 1) {
                    btnNext.setText(R.string.onboarding_get_started);
                    btnNext.setIconResource(0); // remove arrow icon on last page
                    tvSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText(R.string.onboarding_next);
                    btnNext.setIconResource(R.drawable.ic_arrow_forward);
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < PAGE_COUNT - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());

        // Back press: previous page or exit onboarding
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int current = viewPager.getCurrentItem();
                if (current > 0) {
                    viewPager.setCurrentItem(current - 1);
                } else {
                    finish();
                }
            }
        });
    }

    private void setupDots(int currentPage) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < PAGE_COUNT; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == currentPage ? dpToPx(24) : dpToPx(8),
                    dpToPx(8)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == currentPage
                    ? R.drawable.bg_dot_active
                    : R.drawable.bg_dot_inactive);
            dotsLayout.addView(dot);
        }
    }

    private void finishOnboarding() {
        // Mark onboarding as completed
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_COMPLETED, true)
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ── Simple ViewPager2 adapter ─────────────────────────────────────────────
    private static class OnboardingPagerAdapter
            extends RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder> {

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(PAGE_LAYOUTS[viewType], parent, false);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) { }

        @Override
        public int getItemCount() { return PAGE_COUNT; }

        @Override
        public int getItemViewType(int position) { return position; }

        static class PageViewHolder extends RecyclerView.ViewHolder {
            PageViewHolder(@NonNull View itemView) { super(itemView); }
        }
    }
}

