package com.hardik.messageapp.domain.repository

import com.hardik.messageapp.domain.model.SearchItem
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun search(query: String): Flow<List<SearchItem>>
}