package com.example.fico.presentation.activities.import_transactions

import com.example.fico.model.UpdateTransactionFromFileInfo

sealed class ImportTransactionsHistoryUiState {
    object Loading : ImportTransactionsHistoryUiState()
    data class Success(val data: List<UpdateTransactionFromFileInfo>) : ImportTransactionsHistoryUiState()
    object Empty : ImportTransactionsHistoryUiState()
    data class Error(val message: String) : ImportTransactionsHistoryUiState()
}