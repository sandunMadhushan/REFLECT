package me.madhushan.reflect.database;

import androidx.room.Dao;
import androidx.room.Delete;
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

    /** Find a user by email — used for forgot password. */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    /** Update password hash for a user — used for forgot password reset. */
    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE email = :email")
    void updatePassword(String email, String newPasswordHash);

    /** Update full name for a user. */
    @Query("UPDATE users SET fullName = :newName WHERE email = :email")
    void updateName(String email, String newName);

    /** Update full name and password hash for a user. */
    @Query("UPDATE users SET fullName = :newName, passwordHash = :newPasswordHash WHERE email = :email")
    void updateNameAndPassword(String email, String newName, String newPasswordHash);

    /** Delete a user — used for account deletion. */
    @Delete
    void deleteUser(User user);
}

