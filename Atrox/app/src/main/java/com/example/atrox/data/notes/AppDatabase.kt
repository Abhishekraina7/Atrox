package com.example.atrox.data.notes

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.atrox.data.tasks.TaskItem
import com.example.atrox.data.tasks.TaskDao

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `task_table` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `dateString` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
    }
}

@Database(entities = [NoteEntity::class, TaskItem::class], version = 3, exportSchema = false)
@TypeConverters(NoteConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
