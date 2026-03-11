package me.madhushan.reflect.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.madhushan.reflect.MainActivity;
import me.madhushan.reflect.R;
import me.madhushan.reflect.database.AppDatabase;
import me.madhushan.reflect.database.Habit;
import me.madhushan.reflect.database.HabitCompletionDao;
import me.madhushan.reflect.database.HabitDao;
import me.madhushan.reflect.database.ReflectionDao;

/**
 * WorkManager Worker — runs once a day even when the app is closed.
 * Sends system push notifications for:
 *   1. Daily reflection reminder (if user hasn't written one today)
 *   2. Incomplete habits reminder (if habits are not all done today)
 *   3. Streak danger (if user had a streak but missed yesterday)
 */
public class DailyReminderWorker extends Worker {

    private static final int NOTIF_ID_REFLECTION = 2001;
    private static final int NOTIF_ID_HABITS      = 2002;
    private static final int NOTIF_ID_STREAK       = 2003;

    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SessionManager session = new SessionManager(ctx);

        // Only send if user is logged in and system permission is granted
        if (!session.isLoggedIn()) return Result.success();
        if (!NotificationHelper.isSystemPermissionGranted(ctx)) return Result.success();

        int userId = session.getUserId();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis() - 86_400_000L));

        AppDatabase db = AppDatabase.getInstance(ctx);
        ReflectionDao reflectionDao = db.reflectionDao();
        HabitDao habitDao = db.habitDao();
        HabitCompletionDao completionDao = db.habitCompletionDao();

        // ── 1. Daily Reflection Reminder ────────────────────────────────────
        // Fire if user hasn't written any reflection today
        List<me.madhushan.reflect.database.Reflection> todayReflections =
                reflectionDao.getReflectionsFromDate(userId, today);
        if (todayReflections == null || todayReflections.isEmpty()) {
            sendNotification(ctx, NOTIF_ID_REFLECTION,
                    "📝 Time to Reflect",
                    "Take a moment to journal your thoughts. A daily reflection keeps you on track.",
                    "journal");
        }

        // ── 2. Incomplete Habits Reminder ───────────────────────────────────
        List<Habit> habits = habitDao.getHabitsForUser(userId);
        if (!habits.isEmpty()) {
            int totalHabits = habits.size();
            int completedToday = completionDao.getCompletedCountForUserOnDate(userId, today);
            int remaining = totalHabits - completedToday;
            if (remaining > 0) {
                String msg = remaining == 1
                        ? "You have 1 habit left to complete today. Keep the momentum!"
                        : "You have " + remaining + " habits left to complete today. Don't break the chain!";
                sendNotification(ctx, NOTIF_ID_HABITS,
                        "🔥 Complete Your Habits",
                        msg,
                        "home");
            }

            // ── 3. Streak Danger Warning ─────────────────────────────────────
            // If user has a streak on any habit but didn't complete it yesterday
            for (Habit habit : habits) {
                if (habit.streakCount >= 2) {
                    me.madhushan.reflect.database.HabitCompletion yesterdayComp =
                            completionDao.getCompletionForDate(habit.id, yesterday);
                    if (yesterdayComp == null) {
                        sendNotification(ctx, NOTIF_ID_STREAK,
                                "⚠️ Streak at Risk!",
                                "Your " + habit.streakCount + "-day streak on \"" +
                                        habit.title + "\" is at risk. Complete it today!",
                                "home");
                        break; // Only one streak warning per day
                    }
                }
            }
        }

        return Result.success();
    }

    /**
     * Sends a system push notification that appears in the status bar
     * even when the app is completely closed.
     *
     * @param tab  "home" or "journal" — deep-links the user to the right tab on tap
     */
    private void sendNotification(Context ctx, int notifId, String title, String body, String tab) {
        // Create tap intent — opens MainActivity at the right tab
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra("open_tab", tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(ctx).notify(notifId, builder.build());
        } catch (SecurityException e) {
            // Permission was revoked — safe to ignore
        }
    }
}

