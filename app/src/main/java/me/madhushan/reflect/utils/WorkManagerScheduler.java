package me.madhushan.reflect.utils;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Schedules / cancels the daily reminder WorkManager job.
 *
 * Call schedule() after login and when notifications are enabled.
 * Call cancel()   on logout or when notifications are disabled.
 */
public class WorkManagerScheduler {

    private static final String WORK_TAG = "reflect_daily_reminder";

    /**
     * Schedule a daily notification at approximately 8:00 PM.
     * Uses KEEP policy — won't replace an already-scheduled job.
     */
    public static void schedule(Context ctx) {
        // Calculate initial delay to fire at 20:00 today (or tomorrow if already past)
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 20);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();
        if (target.getTimeInMillis() <= now) {
            // Already past 8 PM — schedule for 8 PM tomorrow
            target.add(Calendar.DAY_OF_YEAR, 1);
        }
        long initialDelay = target.getTimeInMillis() - now;

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class,
                1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build();

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,  // Don't reset if already scheduled
                request);
    }

    /**
     * Schedule immediately, replacing any existing schedule.
     * Use this when the user first enables notifications.
     */
    public static void scheduleNow(Context ctx) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyReminderWorker.class,
                1, TimeUnit.DAYS)
                .addTag(WORK_TAG)
                .build();

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE, // Replace existing
                request);
    }

    /** Cancel all daily reminders (called on logout or when user turns off notifications). */
    public static void cancel(Context ctx) {
        WorkManager.getInstance(ctx).cancelAllWorkByTag(WORK_TAG);
    }
}

