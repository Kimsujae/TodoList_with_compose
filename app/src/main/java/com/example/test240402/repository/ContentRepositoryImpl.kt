package com.example.test240402.repository

import com.example.test240402.ContentEntity
import com.example.test240402.data.dao.ContentDao
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor (private val contentDao: ContentDao): ContentRepository {
    override suspend fun insert(item: ContentEntity) {
        contentDao.insert(item)
    }
}