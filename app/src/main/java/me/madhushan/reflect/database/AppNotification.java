package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * In-app notification record.
 * Created whenever a meaningful event occurs (goal achieved, habit streak, etc.).
 */
@Entity(tableName = "app_notifications")
public class AppNotification {

    public static final String TYPE_GOAL_ACHIEVED  = "goal_achieved";
    public static final String TYPE_HABIT_STREAK   = "habit_streak";
    public static final String TYPE_REFLECTION      = "reflection";
    public static final String TYPE_HABIT_REMINDER  = "habit_reminder";
    public static final String TYPE_WELCOME         = "welcome";
    public static final String TYPE_GENERAL         = "general";

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    /** One of the TYPE_* constants above. */
    public String type;

    public String title;
    public String message;

    /** ISO date-time string: yyyy-MM-dd HH:mm:ss */
    public String createdAt;

    /** 0 = unread, 1 = read */
    public int isRead;
}

