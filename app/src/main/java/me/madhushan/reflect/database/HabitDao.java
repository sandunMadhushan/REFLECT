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

    /** All habits for a user, newest first. */
    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    List<Habit> getHabitsForUser(int userId);

    /** Single habit by ID. */
    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    Habit getHabitById(int habitId);

    /** Count of habits for a user. */
    @Query("SELECT COUNT(*) FROM habits WHERE userId = :userId")
    int getHabitCount(int userId);
}

