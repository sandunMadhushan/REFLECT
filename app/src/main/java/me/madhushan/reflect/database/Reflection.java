package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "reflections",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = { @Index("userId") }
)
public class Reflection {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String title;       // e.g. "Weekly Reflection"
    public String mood;        // "happy", "calm", "neutral", "sad", "anxious"
    public String content;     // Journal text body
    public int isFavorite;     // 0 = no, 1 = yes
    public String createdAt;   // ISO datetime e.g. "2026-03-04 09:41:00"
}

