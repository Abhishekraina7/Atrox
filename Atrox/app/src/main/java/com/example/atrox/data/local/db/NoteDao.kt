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

    @Query("UPDATE notes SET isDeleted = 1, deletedTimestamp = :timestamp, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: Long)

    @Query("UPDATE notes SET isDeleted = 0, deletedTimestamp = null, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restoreNote(id: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun permanentlyDeleteNoteById(id: String)

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedTimestamp < :expirationTimestamp")
    suspend fun deleteExpiredNotes(expirationTimestamp: Long)

    // Sync queries
    @Query("SELECT * FROM notes WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedNotes(userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE userId = :userId")
    suspend fun getAllNotesForUser(userId: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markNoteAsSynced(id: String, updatedAt: Long)
}
