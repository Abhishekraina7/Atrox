package com.example.atrox.data.tasks

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_table")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_table WHERE dateString = :date")
    fun getTasksForDate(date: String): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)
    
    @Query("DELETE FROM task_table WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
}
