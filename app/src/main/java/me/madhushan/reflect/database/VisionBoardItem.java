package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A single card on the user's Vision Board.
 * Each item has a title, category, optional note, and an image stored
 * as a local file path (URI string) chosen from the device gallery.
 */
@Entity(
    tableName = "vision_board_items",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = { @Index("userId") }
)
public class VisionBoardItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int    userId;
    public String title;        // e.g. "Dream Home"
    public String category;     // "Life Goal", "Career", "Health", "Travel", "Mindset", "Finance"
    public String note;         // optional motivational note / description
    public String imageUri;     // content URI string of the picked image (nullable = placeholder used)
    public String createdAt;    // ISO datetime
}

