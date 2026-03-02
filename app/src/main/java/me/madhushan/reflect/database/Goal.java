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

    /** Comma-separated reflection notes, or empty string. */
    public String reflectionNotes;

    /** 0 = in progress, 1 = achieved */
    public int isAchieved;

    public String createdAt;   // ISO date string e.g. "2026-03-02"

    public String updatedAt;
}

