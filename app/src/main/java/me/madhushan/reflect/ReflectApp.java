package me.madhushan.reflect;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import me.madhushan.reflect.utils.NotificationHelper;
import me.madhushan.reflect.utils.SessionManager;
import me.madhushan.reflect.utils.WorkManagerScheduler;

public class ReflectApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        NotificationHelper.createChannel(this);

        // Re-schedule daily background reminder if user is already logged in.
        // WorkManager persists across reboots automatically.
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            WorkManagerScheduler.schedule(this);
        }
    }
}
