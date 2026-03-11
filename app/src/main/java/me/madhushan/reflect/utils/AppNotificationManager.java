package me.madhushan.reflect.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.AppNotification;
import me.madhushan.reflect.database.AppNotificationDao;

/**
 * Central utility to post in-app notifications.
 * All DB writes happen off the main thread.
 */
public class AppNotificationManager {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ── Convenience factory methods ──────────────────────────────────────────

    public static void postGoalAchieved(Context ctx, int userId, String goalTitle) {
        post(ctx, userId,
                AppNotification.TYPE_GOAL_ACHIEVED,
                "🎯 Goal Achieved!",
                "You completed your goal: " + goalTitle);
    }

    public static void postHabitStreak(Context ctx, int userId, String habitTitle, int streak) {
        post(ctx, userId,
                AppNotification.TYPE_HABIT_STREAK,
                "🔥 " + streak + "-Day Streak!",
                "Amazing! You've kept up \"" + habitTitle + "\" for " + streak + " days in a row.");
    }

    public static void postReflectionAdded(Context ctx, int userId, String reflectionTitle) {
        post(ctx, userId,
                AppNotification.TYPE_REFLECTION,
                "📝 Reflection Saved",
                "Your reflection \"" + reflectionTitle + "\" has been saved.");
    }

    public static void postHabitReminder(Context ctx, int userId, String habitTitle) {
        post(ctx, userId,
                AppNotification.TYPE_HABIT_REMINDER,
                "⏰ Habit Reminder",
                "Don't forget to complete \"" + habitTitle + "\" today!");
    }

    public static void postWelcome(Context ctx, int userId, String userName) {
        post(ctx, userId,
                AppNotification.TYPE_WELCOME,
                "👋 Welcome to Reflect!",
                "Hi " + userName + "! Start by setting a goal or writing your first reflection.");
    }

    public static void postGeneral(Context ctx, int userId, String title, String message) {
        post(ctx, userId, AppNotification.TYPE_GENERAL, title, message);
    }

    // ── Core writer ──────────────────────────────────────────────────────────

    private static void post(Context ctx, int userId, String type, String title, String message) {
        AppNotification n = new AppNotification();
        n.userId    = userId;
        n.type      = type;
        n.title     = title;
        n.message   = message;
        n.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
        n.isRead    = 0;

        executor.execute(() -> {
            AppNotificationDao dao = AppDatabase.getInstance(ctx.getApplicationContext())
                                                .appNotificationDao();
            dao.insertNotification(n);
        });
    }
}

