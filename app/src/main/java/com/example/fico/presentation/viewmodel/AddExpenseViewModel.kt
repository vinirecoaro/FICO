package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.dataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.util.constants.AppConstants
import com.example.fico.util.constants.DateFunctions
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpenseViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _paymentDay = MutableLiveData<String>()
    val paymentDayLiveData: LiveData<String> = _paymentDay
    private val _addExpenseResult = MutableLiveData<Boolean>()
    val addExpenseResult: LiveData<Boolean> = _addExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(
        price: String,
        description: String,
        category: String,
        paymentDate: String,
        purchaseDate: String,
        installment: Boolean,
        nOfInstallments: Int = 1
    ) {
        viewModelScope.async(Dispatchers.IO) {

            val formattedPaymentDate = FormatValuesToDatabase().expenseDate(paymentDate)

            val formattedPurchaseDate = FormatValuesToDatabase().expenseDate(purchaseDate)

            val formattedInputDate =
                "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val formattedPrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)

            val expense = Expense(
                "",
                formattedPrice,
                description,
                category,
                formattedPaymentDate,
                formattedPurchaseDate,
                formattedInputDate
            )

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(
                expense,
                installment,
                nOfInstallments
            )

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(
                formattedPrice,
                nOfInstallments,
                viewModelScope
            ).await()

            val updatedInformationPerMonth =
                ArrangeDataToUpdateToDatabase().addToInformationPerMonth(
                    expense,
                    installment,
                    nOfInstallments,
                    viewModelScope,
                    false
                ).await()

            var result =
                firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
            result.fold(
                onSuccess = {
                    //Update expense List
                    val expenseListFormatted = mutableListOf<Expense>()
                    expenseList.forEach { expense ->
                        expenseListFormatted.add(
                            Expense(
                                expense.id,
                                expense.price,
                                expense.description,
                                expense.category,
                                FormatValuesFromDatabase().date(expense.paymentDate),
                                FormatValuesFromDatabase().date(expense.purchaseDate),
                                expense.inputDateTime,
                                expense.nOfInstallment
                            )
                        )
                    }
                    dataStore.updateExpenseList(expenseListFormatted)
                    //Update expense Months
                    val expenseMonths = mutableListOf<String>()
                    updatedInformationPerMonth.forEach {
                        expenseMonths.add(
                            it.date
                        )
                    }
                    dataStore.updateExpenseMonths(expenseMonths)
                    //Update observable
                    _addExpenseResult.postValue(true)
                },
                onFailure = {
                    _addExpenseResult.postValue(false)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget(): Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.checkIfExistDefaultBudget()
        }
    }

    suspend fun setDefaultBudget(budget: String): Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val bigNum = BigDecimal(budget)
            val formattedBudget = bigNum.setScale(8, RoundingMode.HALF_UP).toString()
            firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }

    fun getDefaultPaymentDay() {
        viewModelScope.launch {
            val result = firebaseAPI.getDefaultPaymentDay()
            _paymentDay.value = result
        }
    }


}