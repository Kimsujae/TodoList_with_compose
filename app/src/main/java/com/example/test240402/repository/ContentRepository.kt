package com.example.test240402.repository

import com.example.test240402.ContentEntity

interface ContentRepository {

    suspend fun insert(item:ContentEntity)
}