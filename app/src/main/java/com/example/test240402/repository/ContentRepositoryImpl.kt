package com.example.test240402.repository

import com.example.test240402.model.ContentEntity
import com.example.test240402.data.dao.ContentDao
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(private val contentDao: ContentDao) :
    ContentRepository {

    override fun loadList() = contentDao.selectAll()

    override suspend fun insert(item: ContentEntity) {
        contentDao.insert(item)
    }


    override suspend fun delete(item: ContentEntity) {
        contentDao.delete(item)
    }

    override suspend fun modify(item: ContentEntity) {
        contentDao.insert(item)
    }
}