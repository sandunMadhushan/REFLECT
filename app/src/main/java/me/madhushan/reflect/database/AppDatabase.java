package me.madhushan.reflect.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = { User.class, Goal.class, Reflection.class, Habit.class }, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract ReflectionDao reflectionDao();
    public abstract HabitDao habitDao();

    /** Migration from v1 (users only) → v2 (adds goals table). */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `goals` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`userId` INTEGER NOT NULL," +
                    "`title` TEXT," +
                    "`description` TEXT," +
                    "`category` TEXT," +
                    "`priority` TEXT," +
                    "`deadline` TEXT," +
                    "`reflectionNotes` TEXT," +
                    "`isAchieved` INTEGER NOT NULL DEFAULT 0," +
                    "`createdAt` TEXT," +
                    "`updatedAt` TEXT," +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_userId` ON `goals` (`userId`)");
        }
    };

    /** Migration from v2 → v3 (adds reflections table). */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `reflections` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`userId` INTEGER NOT NULL," +
                    "`title` TEXT," +
                    "`mood` TEXT," +
                    "`content` TEXT," +
                    "`isFavorite` INTEGER NOT NULL DEFAULT 0," +
                    "`createdAt` TEXT," +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_reflections_userId` ON `reflections` (`userId`)");
        }
    };

    /** Migration from v3 → v4 (adds habits table). */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `habits` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`userId` INTEGER NOT NULL," +
                    "`name` TEXT," +
                    "`frequency` TEXT," +
                    "`activeDays` TEXT," +
                    "`linkedGoalId` INTEGER NOT NULL DEFAULT 0," +
                    "`reminderEnabled` INTEGER NOT NULL DEFAULT 0," +
                    "`reminderTime` TEXT," +
                    "`createdAt` TEXT," +
                    "`updatedAt` TEXT," +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_userId` ON `habits` (`userId`)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "reflect_db"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

