package com.example.atrox.data.notes

import androidx.room.TypeConverter
import com.example.atrox.domain.model.NoteCategory

class NoteConverters {
    @TypeConverter
    fun fromCategory(category: NoteCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(name: String): NoteCategory {
        return try {
            NoteCategory.valueOf(name)
        } catch (e: Exception) {
            NoteCategory.PERSONAL
        }
    }
}
