package me.madhushan.reflect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import me.madhushan.reflect.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private String currentTab = "home";

    private ImageView navHomeIcon, navGoalsIcon, navJournalIcon, navProfileIcon;
    private TextView  navHomeLabel, navGoalsLabel, navJournalLabel, navProfileLabel;

    // Notification permission launcher
    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                sessionManager.setNotificationsEnabled(isGranted);
                sessionManager.markNotifDialogShown();
            });

    // FAB launchers — refresh the current fragment on RESULT_OK
    private final ActivityResultLauncher<Intent> addReflectionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) refreshCurrentFragment();
            });

    private final ActivityResultLauncher<Intent> addGoalLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) refreshCurrentFragment();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Nav bar view references
        navHomeIcon     = findViewById(R.id.nav_home_icon);
        navHomeLabel    = findViewById(R.id.nav_home_label);
        navGoalsIcon    = findViewById(R.id.nav_goals_icon);
        navGoalsLabel   = findViewById(R.id.nav_goals_label);
        navJournalIcon  = findViewById(R.id.nav_journal_icon);
        navJournalLabel = findViewById(R.id.nav_journal_label);
        navProfileIcon  = findViewById(R.id.nav_profile_icon);
        navProfileLabel = findViewById(R.id.nav_profile_label);

        // Nav click listeners
        findViewById(R.id.nav_home)   .setOnClickListener(v -> switchToTab("home"));
        findViewById(R.id.nav_goals)  .setOnClickListener(v -> switchToTab("goals"));
        findViewById(R.id.nav_journal).setOnClickListener(v -> switchToTab("journal"));
        findViewById(R.id.nav_profile).setOnClickListener(v -> switchToTab("profile"));

        // Centre FAB — context-aware, uses launchers so fragment refreshes on return
        findViewById(R.id.nav_add).setOnClickListener(v -> {
            if (currentTab.equals("journal")) {
                addReflectionLauncher.launch(new Intent(this, AddReflectionActivity.class));
            } else {
                addGoalLauncher.launch(new Intent(this, AddGoalActivity.class));
            }
        });

        // Back press — return to Home if not already there; otherwise do nothing (don't exit)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (!currentTab.equals("home")) switchToTab("home");
            }
        });

        // Load initial tab (supports deep-link via open_tab extra)
        if (savedInstanceState == null) {
            String openTab = getIntent().getStringExtra("open_tab");
            switchToTab(openTab != null ? openTab : "home");
        }

        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String openTab = intent.getStringExtra("open_tab");
        switchToTab(openTab != null ? openTab : "home");
    }

    /** Public so any Fragment can trigger a tab switch (e.g. HomeFragment "View All"). */
    public void switchToTab(String tab) {
        currentTab = tab;

        Fragment fragment;
        switch (tab) {
            case "goals":   fragment = new HabitTrackerFragment(); break;
            case "journal": fragment = new JournalFragment();      break;
            case "profile": fragment = new ProfileFragment();      break;
            default:        fragment = new HomeFragment();         break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .commit();

        updateNavHighlight(tab);
    }

    /**
     * Called when returning from AddGoalActivity or AddReflectionActivity via the centre FAB.
     * Tells the currently displayed fragment to reload its data.
     */
    private void refreshCurrentFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof HomeFragment)              ((HomeFragment)              f).loadData();
        else if (f instanceof HabitTrackerFragment) ((HabitTrackerFragment)      f).loadData();
        else if (f instanceof JournalFragment)      ((JournalFragment)           f).loadData();
    }

    private void updateNavHighlight(String tab) {
        int primary = getResources().getColor(R.color.primary,   null);
        int hint    = getResources().getColor(R.color.text_hint, null);

        navHomeIcon.setColorFilter(   tab.equals("home")    ? primary : hint);
        navHomeLabel.setTextColor(    tab.equals("home")    ? primary : hint);
        navHomeLabel.setTypeface(null, tab.equals("home")   ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        navGoalsIcon.setColorFilter(  tab.equals("goals")   ? primary : hint);
        navGoalsLabel.setTextColor(   tab.equals("goals")   ? primary : hint);
        navGoalsLabel.setTypeface(null, tab.equals("goals") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        navJournalIcon.setColorFilter(  tab.equals("journal")   ? primary : hint);
        navJournalLabel.setTextColor(   tab.equals("journal")   ? primary : hint);
        navJournalLabel.setTypeface(null, tab.equals("journal") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        navProfileIcon.setColorFilter(  tab.equals("profile")   ? primary : hint);
        navProfileLabel.setTextColor(   tab.equals("profile")   ? primary : hint);
        navProfileLabel.setTypeface(null, tab.equals("profile") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (sessionManager.hasNotifDialogBeenShown()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                sessionManager.setNotificationsEnabled(true);
                sessionManager.markNotifDialogShown();
            } else {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            sessionManager.setNotificationsEnabled(
                    NotificationManagerCompat.from(this).areNotificationsEnabled());
            sessionManager.markNotifDialogShown();
        }
    }
}
