package me.madhushan.reflect;

/**
 * HabitModel — simple data class for a single habit item.
 *
 * No Room / database annotations — pure Java POJO for the UI layer.
 *
 * Place this file at:
 *   app/src/main/java/me/madhushan/reflect/HabitModel.java
 */
public class HabitModel {

    /** Display name shown as the habit title, e.g. "Morning Meditation". */
    public String name;

    /** Short description shown as the habit subtitle, e.g. "15 minutes · Daily". */
    public String description;

    /** Number of consecutive days the habit has been kept, e.g. 7. */
    public int streak;

    /** Whether the habit has been completed today. */
    public boolean completed;

    /**
     * Constructor.
     *
     * @param name        Habit title
     * @param description Habit subtitle / description
     * @param streak      Current streak (days)
     * @param completed   Completed today?
     */
    public HabitModel(String name, String description, int streak, boolean completed) {
        this.name        = name;
        this.description = description;
        this.streak      = streak;
        this.completed   = completed;
    }
}

