package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "habit_completions",
    foreignKeys = @ForeignKey(
        entity = Habit.class,
        parentColumns = "id",
        childColumns = "habitId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = { @Index("habitId") }
)
public class HabitCompletion {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int habitId;
    public String completedDate;  // ISO date "yyyy-MM-dd"
}

