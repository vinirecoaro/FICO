package com.example.fico.presentation.viewmodel

import android.util.Log
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

            val currentTotalExpense = dataStore.getTotalExpense()

            //Verifying expenses that still exists
            val currentExpenseList = dataStore.getExpenseList()

            val expensesThatStillExists = transactionsFunctions.getExpensesThatStillExists(uploadFromFile.expenseIdList, currentExpenseList)

            //Expense id List
            val expenseIdList = mutableListOf<String>()
            expensesThatStillExists.forEach { expenseIdList.add(it.id) }

            //Total expense
            val totalValueFromList = transactionsFunctions.calculateTotalValueFromExpenseList(expensesThatStillExists)

            val updatedTotalExpense = transactionsFunctions.calculateUpdatedTotalExpense(
                currentTotalExpense, "0", 1, totalValueFromList, 1
            )

            //Expense info per month
            val currentExpenseInfoPerMonth = dataStore.getExpenseInfoPerMonth()
            val expenseInfoPerMonth = transactionsFunctions.calculateExpenseInformationPerMonthAfterDeleteUploadsFromFile(
                expensesThatStillExists,
                currentExpenseInfoPerMonth
            )

            //Earning id list
            val currentEarningList = dataStore.getEarningsList()

            val earningIdsThatStillExists = transactionsFunctions.getEarningsIdThatStillExists(uploadFromFile.earningIdList, currentEarningList)


            transactionsRepository.deleteUploadFromFile(
                expenseIdList,
                earningIdsThatStillExists,
                updatedTotalExpense,
                expenseInfoPerMonth,
                uploadFromFile.id
            ).fold(
                onSuccess = {
                    //Expense list
                    dataStore.deleteFromExpenseList(expenseIdList)

                    //Expense info per month
                    dataStore.updateInfoPerMonthExpense(expenseInfoPerMonth)

                    //Update month expenses
                    val monthList = mutableListOf<String>()
                    val updatedExpenseInfoPerMonth = dataStore.getExpenseInfoPerMonth()
                    for(month in updatedExpenseInfoPerMonth){
                        if(month.budget != month.availableNow){
                            monthList.add(month.date)
                        }
                    }
                    dataStore.updateAndResetExpenseMonths(monthList)

                    //Total expense
                    dataStore.updateTotalExpense(updatedTotalExpense)

                    //Earning list
                    dataStore.deleteFromEarningList(earningIdsThatStillExists)

                    //Upload log
                    dataStore.deleteFromUploadsFromFileList(uploadFromFile.id)

                    getUploadsFromFileList()
                },
                onFailure = {
                    Log.e("Error: ", "Error on delete upload from file - ${ it.message.toString() }")
                }
            )
        }
    }

}