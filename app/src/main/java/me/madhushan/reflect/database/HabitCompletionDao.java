package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HabitCompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCompletion(HabitCompletion completion);

    @Delete
    void deleteCompletion(HabitCompletion completion);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completedDate = :date LIMIT 1")
    HabitCompletion getCompletionForDate(int habitId, String date);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedDate DESC")
    List<HabitCompletion> getCompletionsForHabit(int habitId);

    /** Count of habits completed today for a given set of habit IDs (via userId join). */
    @Query("SELECT COUNT(DISTINCT hc.habitId) FROM habit_completions hc " +
           "INNER JOIN habits h ON hc.habitId = h.id " +
           "WHERE h.userId = :userId AND hc.completedDate = :date")
    int getCompletedCountForUserOnDate(int userId, String date);

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND completedDate = :date")
    void deleteCompletionForDate(int habitId, String date);

    /** Consecutive day streak for a habit (count back from today). */
    @Query("SELECT completedDate FROM habit_completions WHERE habitId = :habitId ORDER BY completedDate DESC")
    List<String> getAllCompletionDates(int habitId);
}

