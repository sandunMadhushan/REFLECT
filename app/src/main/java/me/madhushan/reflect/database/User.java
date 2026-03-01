package me.madhushan.reflect.database;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "users",
    indices = { @Index(value = "email", unique = true) }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fullName;
    public String email;
    public String passwordHash; // SHA-256 hash — never plain text
}

