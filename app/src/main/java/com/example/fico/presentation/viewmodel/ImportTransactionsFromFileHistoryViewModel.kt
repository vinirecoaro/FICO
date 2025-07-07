package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.UpdateTransactionFromFileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportTransactionsFromFileHistoryViewModel(
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _uploadsFromFileList = MutableStateFlow<List<UpdateTransactionFromFileInfo>>(emptyList())
    val uploadsFromFileList: StateFlow<List<UpdateTransactionFromFileInfo>> = _uploadsFromFileList.asStateFlow()

    fun getUploadsFromFileList(){
        viewModelScope.launch(Dispatchers.IO) {
            val updatesFromFileList = dataStore.getUploadsFromFileList()
            _uploadsFromFileList.value = updatesFromFileList
        }
    }

}