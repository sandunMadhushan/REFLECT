package me.madhushan.reflect;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines all achievements and evaluates which ones are unlocked
 * based on live stats passed in from the database.
 */
public class AchievementEngine {

    public static class Achievement {
        public final String id;
        public final String category;     // "Streaks", "Reflections", "Goals", "Habits"
        public final String title;
        public final String description;
        public final String iconName;     // matches drawable suffix e.g. "check_circle"
        public final int    xpReward;
        public final int    targetValue;  // value needed to unlock
        public       int    currentValue; // user's current progress
        public       boolean unlocked;

        public Achievement(String id, String category, String title, String description,
                           String iconName, int xpReward, int targetValue) {
            this.id          = id;
            this.category    = category;
            this.title       = title;
            this.description = description;
            this.iconName    = iconName;
            this.xpReward    = xpReward;
            this.targetValue = targetValue;
        }
    }

    public static class Stats {
        public int maxHabitStreak;       // highest streak across all habits
        public int totalReflections;     // total journal entries
        public int totalGoals;           // total goals created
        public int completedGoals;       // goals marked achieved
        public int totalHabits;          // total habits created
        public int habitCompletionsTotal;// total habit completions ever
        public int daysUsed;             // days since account created (approx)
    }

    /** Returns all achievements, evaluated against the given stats. */
    public static List<Achievement> evaluate(Stats s) {
        List<Achievement> list = new ArrayList<>();

        // ── Streaks category ──────────────────────────────────────────────
        Achievement a1 = new Achievement("streak_3",  "Streaks", "Beginner",     "3 Day Streak",   "check_circle",     50,  3);
        Achievement a2 = new Achievement("streak_7",  "Streaks", "Consistent",   "7 Day Streak",   "bolt",            100,  7);
        Achievement a3 = new Achievement("streak_30", "Streaks", "Habit Master", "30 Day Streak",  "calendar_month",  300, 30);
        Achievement a4 = new Achievement("streak_100","Streaks", "Unstoppable",  "100 Day Streak", "diamond",         750, 100);
        for (Achievement a : new Achievement[]{a1, a2, a3, a4}) {
            a.currentValue = s.maxHabitStreak;
            a.unlocked     = s.maxHabitStreak >= a.targetValue;
            list.add(a);
        }

        // ── Reflections category ──────────────────────────────────────────
        Achievement r1 = new Achievement("reflect_1",  "Reflections", "First Entry",  "Write 1 Journal",      "edit_note",    25,  1);
        Achievement r2 = new Achievement("reflect_10", "Reflections", "Storyteller",  "10 Entries Total",     "menu_book",   100, 10);
        Achievement r3 = new Achievement("reflect_50", "Reflections", "Novelist",     "50 Entries Total",     "psychology",  400, 50);
        Achievement r4 = new Achievement("reflect_365","Reflections", "Enlightened",  "365 Entries Total",    "self_improvement", 1000, 365);
        for (Achievement a : new Achievement[]{r1, r2, r3, r4}) {
            a.currentValue = s.totalReflections;
            a.unlocked     = s.totalReflections >= a.targetValue;
            list.add(a);
        }

        // ── Goals category ────────────────────────────────────────────────
        Achievement g1 = new Achievement("goal_1",  "Goals", "Goal Setter",  "Create 1 Goal",     "flag",         25,  1);
        Achievement g2 = new Achievement("goal_5",  "Goals", "Achiever",     "Complete 5 Goals",  "emoji_events", 200,  5);
        Achievement g3 = new Achievement("goal_10", "Goals", "Champion",     "Complete 10 Goals", "military_tech",500, 10);
        Achievement g4 = new Achievement("goal_25", "Goals", "Legend",       "Complete 25 Goals", "star",        1500, 25);
        for (Achievement a : new Achievement[]{g1, g2}) {
            a.currentValue = (a.id.equals("goal_1")) ? s.totalGoals : s.completedGoals;
            a.unlocked     = a.currentValue >= a.targetValue;
            list.add(a);
        }
        for (Achievement a : new Achievement[]{g3, g4}) {
            a.currentValue = s.completedGoals;
            a.unlocked     = s.completedGoals >= a.targetValue;
            list.add(a);
        }

        // ── Habits category ───────────────────────────────────────────────
        Achievement h1 = new Achievement("habit_first",  "Habits", "First Habit",    "Create 1 Habit",        "favorite",    25,  1);
        Achievement h2 = new Achievement("habit_done10", "Habits", "On a Roll",      "Complete 10 Habits",    "check_circle",100, 10);
        Achievement h3 = new Achievement("habit_done100","Habits", "Powerhouse",     "Complete 100 Habits",   "fitness_center",500,100);
        Achievement h4 = new Achievement("habit_done365","Habits", "Habit Machine",  "Complete 365 Habits",   "calendar_month",2000,365);
        h1.currentValue = s.totalHabits;         h1.unlocked = s.totalHabits >= 1;
        h2.currentValue = s.habitCompletionsTotal; h2.unlocked = s.habitCompletionsTotal >= 10;
        h3.currentValue = s.habitCompletionsTotal; h3.unlocked = s.habitCompletionsTotal >= 100;
        h4.currentValue = s.habitCompletionsTotal; h4.unlocked = s.habitCompletionsTotal >= 365;
        list.add(h1); list.add(h2); list.add(h3); list.add(h4);

        return list;
    }

    /** Total XP = sum of xpReward for all unlocked achievements */
    public static int calcXp(List<Achievement> achievements) {
        int xp = 0;
        for (Achievement a : achievements) if (a.unlocked) xp += a.xpReward;
        return xp;
    }

    /** Level = 1 + xp / 100 (roughly) */
    public static int calcLevel(int xp) {
        return Math.max(1, 1 + xp / 100);
    }
}

