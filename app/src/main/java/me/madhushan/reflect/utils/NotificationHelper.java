package me.madhushan.reflect.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import me.madhushan.reflect.R;

/**
 * Central gatekeeper for all app notifications.
 *
 * A notification is sent ONLY when:
 *   1. System permission is granted (POST_NOTIFICATIONS on Android 13+,
 *      or areNotificationsEnabled() on Android 12 and below)
 *   2. The user has turned the in-app notification toggle ON
 *
 * Usage:
 *   NotificationHelper.send(context, "Title", "Body text");
 */
public class NotificationHelper {

    public static final String CHANNEL_ID   = "reflect_reminders";
    public static final String CHANNEL_NAME = "Reflect Reminders";
    public static final int    NOTIF_ID     = 1001;

    /** Create the notification channel (call once, e.g. from Application.onCreate). */
    public static void createChannel(Context context) {
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(
                CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(CHANNEL_NAME)
                .setDescription("Daily reflection reminders and goal updates")
                .build();
        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }

    /**
     * Send a notification only if:
     *   - System notification permission is granted
     *   - In-app toggle is ON (saved in SessionManager)
     */
    public static void send(Context context, String title, String body) {
        SessionManager session = new SessionManager(context);

        // Gate 1: in-app toggle must be ON
        if (!session.getNotificationsEnabled()) return;

        // Gate 2: system permission must be granted
        if (!isSystemPermissionGranted(context)) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, builder.build());
        } catch (SecurityException e) {
            // Permission was revoked between the check and notify — safe to ignore
        }
    }

    /** Returns true if the system allows notifications for this app. */
    public static boolean isSystemPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    /**
     * Returns true if notifications should be sent — both system permission
     * is granted AND the in-app toggle is ON.
     */
    public static boolean canSendNotifications(Context context) {
        SessionManager session = new SessionManager(context);
        return session.getNotificationsEnabled() && isSystemPermissionGranted(context);
    }
}

