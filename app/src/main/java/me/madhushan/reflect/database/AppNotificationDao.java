package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNotification(AppNotification notification);

    /** All notifications for user, newest first. */
    @Query("SELECT * FROM app_notifications WHERE userId = :userId ORDER BY createdAt DESC")
    List<AppNotification> getNotificationsForUser(int userId);

    /** Count of unread notifications for user. */
    @Query("SELECT COUNT(*) FROM app_notifications WHERE userId = :userId AND isRead = 0")
    int getUnreadCount(int userId);

    /** Mark a single notification as read. */
    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :notifId")
    void markRead(int notifId);

    /** Mark ALL notifications for user as read. */
    @Query("UPDATE app_notifications SET isRead = 1 WHERE userId = :userId")
    void markAllRead(int userId);

    /** Delete a notification. */
    @Query("DELETE FROM app_notifications WHERE id = :notifId")
    void deleteNotification(int notifId);

    /** Delete all notifications for a user. */
    @Query("DELETE FROM app_notifications WHERE userId = :userId")
    void deleteAllForUser(int userId);
}

