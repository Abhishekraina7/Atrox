package com.example.atrox.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `task_table` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `dateString` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `notes` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `notes` ADD COLUMN `deletedTimestamp` INTEGER")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Notes
        database.execSQL("ALTER TABLE `notes` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `notes` ADD COLUMN `isSynced` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `notes` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        // Tasks
        database.execSQL("ALTER TABLE `task_table` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `task_table` ADD COLUMN `isSynced` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `task_table` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
    }
}

@Database(entities = [NoteEntity::class, TaskItem::class], version = 5, exportSchema = false)
@TypeConverters(NoteConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
