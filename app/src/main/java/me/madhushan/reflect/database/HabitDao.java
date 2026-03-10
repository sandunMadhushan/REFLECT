package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertHabit(Habit habit);

    @Update
    void updateHabit(Habit habit);

    @Delete
    void deleteHabit(Habit habit);

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    Habit getHabitById(int habitId);

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    List<Habit> getHabitsForUser(int userId);

    @Query("SELECT COUNT(*) FROM habits WHERE userId = :userId")
    int getTotalHabitsCount(int userId);

    @Query("UPDATE habits SET streakCount = :streak, updatedAt = :updatedAt WHERE id = :habitId")
    void updateStreak(int habitId, int streak, String updatedAt);
}

