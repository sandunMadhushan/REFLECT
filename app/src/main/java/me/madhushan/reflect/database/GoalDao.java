package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGoal(Goal goal);

    @Update
    void updateGoal(Goal goal);

    @Delete
    void deleteGoal(Goal goal);

    /** Get a single goal by ID. */
    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    Goal getGoalById(int goalId);

    /** All goals for a user, newest first. */
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    List<Goal> getGoalsForUser(int userId);

    /** Active (not achieved) goals count. */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isAchieved = 0")
    int getActiveGoalsCount(int userId);

    /** Completed (achieved) goals count. */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isAchieved = 1")
    int getCompletedGoalsCount(int userId);

    /** Total goals count. */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId")
    int getTotalGoalsCount(int userId);

    /** Most recent N goals (for recent activity feed). */
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY updatedAt DESC LIMIT :limit")
    List<Goal> getRecentGoals(int userId, int limit);

    /** Goals updated on a specific date (for chart). */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND updatedAt LIKE :datePrefix || '%'")
    int getActivityCountForDate(int userId, String datePrefix);
}

