package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReflectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReflection(Reflection reflection);

    @Update
    void updateReflection(Reflection reflection);

    @Delete
    void deleteReflection(Reflection reflection);

    @Query("SELECT * FROM reflections WHERE id = :reflectionId LIMIT 1")
    Reflection getReflectionById(int reflectionId);

    @Query("SELECT * FROM reflections WHERE userId = :userId ORDER BY createdAt DESC")
    List<Reflection> getReflectionsForUser(int userId);

    @Query("SELECT * FROM reflections WHERE userId = :userId AND createdAt >= :dateStart ORDER BY createdAt DESC")
    List<Reflection> getReflectionsFromDate(int userId, String dateStart);

    @Query("SELECT * FROM reflections WHERE userId = :userId AND isFavorite = 1 ORDER BY createdAt DESC")
    List<Reflection> getFavoriteReflections(int userId);

    @Query("SELECT COUNT(*) FROM reflections WHERE userId = :userId")
    int getTotalReflectionsCount(int userId);
}

