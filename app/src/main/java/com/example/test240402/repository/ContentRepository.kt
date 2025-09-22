//package com.example.test240402.repository
//
//import com.example.test240402.model.ContentEntity
//import kotlinx.coroutines.flow.Flow
//
//interface ContentRepository {
//
//    fun loadList(): Flow<List<ContentEntity>>
//
//    suspend fun insert(item: ContentEntity)
//
//    suspend fun delete(item: ContentEntity)
//
//    suspend fun modify(item: ContentEntity)
//}