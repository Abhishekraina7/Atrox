package com.example.atrox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC, timestamp DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND LOWER(title) LIKE '%' || LOWER(:searchQuery) || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchNotesByTitle(searchQuery: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteById(id: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, deletedTimestamp = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: Long)

    @Query("UPDATE notes SET isDeleted = 0, deletedTimestamp = null WHERE id = :id")
    suspend fun restoreNote(id: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun permanentlyDeleteNoteById(id: String)

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedTimestamp < :expirationTimestamp")
    suspend fun deleteExpiredNotes(expirationTimestamp: Long)
}
