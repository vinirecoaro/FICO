package com.example.fico.presentation.activities.import_transactions

import com.example.fico.model.UploadTransactionFromFileInfo

sealed class ImportTransactionsHistoryUiState {
    object Loading : ImportTransactionsHistoryUiState()
    data class Success(val data: List<UploadTransactionFromFileInfo>) : ImportTransactionsHistoryUiState()
    object Empty : ImportTransactionsHistoryUiState()
    data class Error(val message: String) : ImportTransactionsHistoryUiState()
}