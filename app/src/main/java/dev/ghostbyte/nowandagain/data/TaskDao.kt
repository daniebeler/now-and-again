package dev.ghostbyte.nowandagain.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE archived = 0")
    fun observeActive(): Flow<List<dev.ghostbyte.nowandagain.data.Task>>

    @Insert
    suspend fun insert(task: dev.ghostbyte.nowandagain.data.Task): Long

    @Update
    suspend fun update(task: dev.ghostbyte.nowandagain.data.Task)

    @Delete
    suspend fun delete(task: dev.ghostbyte.nowandagain.data.Task)

    @Query("SELECT * FROM tasks WHERE archived = 0")
    suspend fun getActive(): List<dev.ghostbyte.nowandagain.data.Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): dev.ghostbyte.nowandagain.data.Task?
}