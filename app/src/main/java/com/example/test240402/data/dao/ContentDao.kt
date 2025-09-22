//package com.example.test240402.data.dao
//
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import com.example.test240402.model.ContentEntity
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface ContentDao {
//
//    @Query("SELECT * FROM content")
//    fun selectAll(): Flow<List<ContentEntity>>
//
//    @Delete
//    suspend fun delete(item: ContentEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(item: ContentEntity)
//
//}