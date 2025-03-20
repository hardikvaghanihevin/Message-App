package com.hardik.messageapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DummyViewModel @Inject constructor() : ViewModel() {
    private val _dummyData = MutableStateFlow<List<String>>(emptyList())
    val dummyData: StateFlow<List<String>> = _dummyData.asStateFlow()

    init { fetchDummyData() }

    private fun fetchDummyData() {
        viewModelScope.launch {
            flow {
                val dummyList = List(100) { "Item ${it + 1}" }
                emit(dummyList)
            }.flowOn(Dispatchers.IO).collect{ _dummyData.value = it }

        }
    }

    fun removeItem(item: String) {
        _dummyData.value = _dummyData.value.filterNot { it == item }
    }

    fun editItem(item: String) {
        _dummyData.value = _dummyData.value.map { if (it == item) "$item edit" else it }
    }
}