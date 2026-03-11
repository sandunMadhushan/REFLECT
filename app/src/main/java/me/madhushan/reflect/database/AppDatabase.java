package me.madhushan.reflect.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = { User.class, Goal.class, Reflection.class, Habit.class, HabitCompletion.class, VisionBoardItem.class, AppNotification.class }, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract GoalDao goalDao();
    public abstract ReflectionDao reflectionDao();
    public abstract HabitDao habitDao();
    public abstract HabitCompletionDao habitCompletionDao();
    public abstract VisionBoardItemDao visionBoardItemDao();
    public abstract AppNotificationDao appNotificationDao();

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

    /** Migration from v3 → v4 (adds habits and habit_completions tables). */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `habits` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`userId` INTEGER NOT NULL," +
                    "`title` TEXT," +
                    "`description` TEXT," +
                    "`iconName` TEXT," +
                    "`iconColor` TEXT," +
                    "`frequency` TEXT," +
                    "`activeDays` TEXT," +
                    "`streakCount` INTEGER NOT NULL DEFAULT 0," +
                    "`createdAt` TEXT," +
                    "`updatedAt` TEXT," +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_userId` ON `habits` (`userId`)");

            db.execSQL("CREATE TABLE IF NOT EXISTS `habit_completions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`habitId` INTEGER NOT NULL," +
                    "`completedDate` TEXT," +
                    "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_completions_habitId` ON `habit_completions` (`habitId`)");
        }
    };

    /** Migration from v4 → v5 (adds vision_board_items table). */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `vision_board_items` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`userId` INTEGER NOT NULL," +
                    "`title` TEXT," +
                    "`category` TEXT," +
                    "`note` TEXT," +
                    "`imageUri` TEXT," +
                    "`createdAt` TEXT," +
                    "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_vision_board_items_userId` ON `vision_board_items` (`userId`)");
        }
    };

    /** Migration from v5 → v6 (adds app_notifications table). */
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `app_notifications` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`userId` INTEGER NOT NULL," +
                    "`type` TEXT," +
                    "`title` TEXT," +
                    "`message` TEXT," +
                    "`createdAt` TEXT," +
                    "`isRead` INTEGER NOT NULL)");
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

