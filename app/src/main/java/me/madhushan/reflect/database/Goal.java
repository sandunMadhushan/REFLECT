package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "goals",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = { @Index("userId") }
)
public class Goal {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String title;
    public String description;
    public String category;      // personal, health, career, relationships
    public String priority;      // low, medium, high
    public String deadline;      // ISO date e.g. "2026-12-31"
    public String reflectionNotes;
    public int isAchieved;       // 0 = active, 1 = achieved
    public String createdAt;
    public String updatedAt;
}

