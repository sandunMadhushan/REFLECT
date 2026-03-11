package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VisionBoardItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertItem(VisionBoardItem item);

    @Update
    void updateItem(VisionBoardItem item);

    @Delete
    void deleteItem(VisionBoardItem item);

    /** All vision board items for a user, newest first. */
    @Query("SELECT * FROM vision_board_items WHERE userId = :userId ORDER BY createdAt DESC")
    List<VisionBoardItem> getItemsForUser(int userId);

    /** Single item by id. */
    @Query("SELECT * FROM vision_board_items WHERE id = :id LIMIT 1")
    VisionBoardItem getItemById(int id);

    /** Count items for a user. */
    @Query("SELECT COUNT(*) FROM vision_board_items WHERE userId = :userId")
    int getCountForUser(int userId);
}

