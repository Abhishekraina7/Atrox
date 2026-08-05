package com.example.atrox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeletedItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTombstone(tombstone: DeletedItemEntity)

    @Query("SELECT * FROM deleted_items WHERE userId = :userId")
    suspend fun getTombstonesForUser(userId: String): List<DeletedItemEntity>

    @Query("DELETE FROM deleted_items WHERE itemId = :itemId")
    suspend fun deleteTombstone(itemId: String)
}
