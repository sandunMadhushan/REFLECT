package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "habits",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = { @Index("userId") }
)
public class Habit {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String title;
    public String description;    // e.g. "15 minutes", "2 Liters", etc.
    public String iconName;       // e.g. "self_improvement", "water_drop", "book_2", "fitness_center"
    public String iconColor;      // e.g. "indigo", "emerald", "pink", "orange"
    public String frequency;      // "daily", "weekly", "specific"
    public String activeDays;     // e.g. "1111111" (7 chars, Mon–Sun)
    public int streakCount;       // current streak in days
    public String createdAt;
    public String updatedAt;
}

