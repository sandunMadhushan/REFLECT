package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {

    /** Insert a new user. Throws SQLiteConstraintException if email already exists. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    /** Find a user by email and hashed password — used for login. */
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    User findByEmailAndPassword(String email, String passwordHash);

    /** Check if an email already exists — used during registration. */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int emailExists(String email);

    /** Get user by ID — used to restore session. */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);
}

