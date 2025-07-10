package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.TransactionsFunctions
import com.example.fico.model.UploadTransactionFromFileInfo
import com.example.fico.presentation.activities.import_transactions.ImportTransactionsHistoryUiState
import com.example.fico.repositories.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImportTransactionsHistoryViewModel(
    private val dataStore: DataStoreManager,
    private val transactionsRepository: TransactionsRepository
) : ViewModel() {

    val transactionsFunctions = TransactionsFunctions()
    private val _uiState = MutableStateFlow<ImportTransactionsHistoryUiState>(ImportTransactionsHistoryUiState.Loading)
    val uiState: StateFlow<ImportTransactionsHistoryUiState> = _uiState

    fun getUploadsFromFileList() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ImportTransactionsHistoryUiState.Loading

            val updates = dataStore.getUploadsFromFileList().sortedByDescending { it.inputDateTime }

            _uiState.value = if (updates.isEmpty()) {
                ImportTransactionsHistoryUiState.Empty
            } else {
                ImportTransactionsHistoryUiState.Success(updates)
            }
        }
    }

    fun deleteUploadFromFile(uploadFromFile : UploadTransactionFromFileInfo){
        viewModelScope.launch(Dispatchers.IO) {

            //Verifying expenses that still exists
            val currentTotalExpense = dataStore.getTotalExpense()

            val currentExpenseList = dataStore.getExpenseList()

            val expenseThatStillExists = transactionsFunctions.getExpensesThatStillExists(uploadFromFile.expenseIdList, currentExpenseList)

            val expenseIdList = mutableListOf<String>()
            expenseThatStillExists.forEach { expenseIdList.add(it.id) }

            //Total expense
            val totalValueFromList = transactionsFunctions.calculateTotalValueFromExpenseList(expenseThatStillExists)

            val updatedTotalExpense = transactionsFunctions.calculateUpdatedTotalExpense(
                currentTotalExpense, "0", 1, totalValueFromList, 1
            )

            transactionsRepository.deleteUploadFromFile(
                expenseIdList,
                uploadFromFile.earningIdList,
                updatedTotalExpense,
                mutableListOf()
            ).fold(
                onSuccess = {},
                onFailure = {}
            )
        }
    }

}