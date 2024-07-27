package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.util.constants.DateFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class EditExpenseViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {


    private val _editExpenseResult = MutableLiveData<Boolean>()
    val editExpenseResult : LiveData<Boolean> = _editExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditExpense(
        oldExpense: Expense,
        price: String,
        description: String,
        category: String,
        paymentDate: String,
        purchaseData : String,
        installment : Boolean,
        nOfInstallments: Int = 1
    )  {
        viewModelScope.async(Dispatchers.IO) {

            var oldExpensePaymentDate = FormatValuesToDatabase().expenseDate(oldExpense.paymentDate)
            var oldExpensePurchaseDate = FormatValuesToDatabase().expenseDate(oldExpense.purchaseDate)

            var oldExpenseNOfInstallment = 1

            if(installment){
                oldExpenseNOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(oldExpense.id).toInt()
                oldExpensePaymentDate = FormatValuesToDatabase().expenseDate(FormatValuesFromDatabase().installmentExpenseInitialDate(oldExpense.id,oldExpense.paymentDate))
            }

            val oldExpenseFormatted = Expense(
                oldExpense.id,
                oldExpense.price,
                FormatValuesFromDatabase().installmentExpenseDescription(oldExpense.description),
                oldExpense.category,
                oldExpensePaymentDate,
                oldExpensePurchaseDate,
                "",
                oldExpenseNOfInstallment.toString()
            )

            val removeFromExpenseList = ArrangeDataToUpdateToDatabase().removeFromExpenseList(oldExpenseFormatted, viewModelScope).await()

            val newExpensePrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)
            val newExpensePaymentDate = FormatValuesToDatabase().expenseDate(paymentDate)
            val newExpensePurchaseDate = FormatValuesToDatabase().expenseDate(purchaseData)
            val formattedInputDate = "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val newExpense = Expense(id = "", newExpensePrice, description, category, newExpensePaymentDate, newExpensePurchaseDate, formattedInputDate)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(newExpense.price, nOfInstallments, viewModelScope, oldExpenseFormatted.price, oldExpenseNOfInstallment).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(newExpense, installment, nOfInstallments, viewModelScope, true, oldExpenseFormatted).await()

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(newExpense, installment, nOfInstallments)

            firebaseAPI.editExpense(expenseList, updatedTotalExpense,updatedInformationPerMonth, removeFromExpenseList, oldExpenseNOfInstallment).fold(
                onSuccess = {

                    //Update Total Expense on DataStore
                    val oldExpensePriceFullPrice = BigDecimal(oldExpense.price)
                    val newExpensePriceFullPrice = BigDecimal(FormatValuesToDatabase().expensePrice(price,1))
                    val currentTotalExpenseDataStore = BigDecimal(dataStore.getTotalExpense())
                    val updatedTotalExpenseDataStore = currentTotalExpenseDataStore.subtract(oldExpensePriceFullPrice).add(newExpensePriceFullPrice).setScale(8, RoundingMode.HALF_UP)
                    dataStore.updateTotalExpense(updatedTotalExpenseDataStore.toString())

                    //Update expenseList and infoPerMonth on dataStore
                    val updatedExpenseListDataStore = dataStore.getExpenseList().toMutableList()
                    val updatedInfoPerMonthDataStore = dataStore.getExpenseInfoPerMonth().toMutableSet()
                        //Remove old expenses
                    removeFromExpenseList.forEach { expenseId ->
                        //Remove expenses from expense list
                        updatedExpenseListDataStore.removeAll{it.id == expenseId}
                    }
                        //Remove old expense values from info per month list
                    val oldExpenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(
                        oldExpenseFormatted,
                        installment,
                        oldExpenseFormatted.nOfInstallment.toInt()
                    )
                    oldExpenseList.forEach {oldExpense ->
                        val oldExpenseDateYYYYmm = DateFunctions().YYYYmmDDtoYYYYmm(oldExpense.paymentDate)
                        val infoOfMonth = updatedInfoPerMonthDataStore.find { it.date == oldExpenseDateYYYYmm }
                        val currentMonthExpenseOldExpense = BigDecimal(infoOfMonth!!.monthExpense).setScale(8, RoundingMode.HALF_UP)
                        val currentAvailableNowOldExpense = BigDecimal(infoOfMonth.availableNow).setScale(8, RoundingMode.HALF_UP)
                        val updatedMonthExpenseOldExpense = currentMonthExpenseOldExpense.subtract(BigDecimal(oldExpense.price))
                        val updatedAvailableNowOldExpense = currentAvailableNowOldExpense.add(BigDecimal(oldExpense.price))
                        val updatedInfoOfMonth = InformationPerMonthExpense(
                            infoOfMonth.date,
                            updatedAvailableNowOldExpense.toString(),
                            infoOfMonth.budget,
                            updatedMonthExpenseOldExpense.toString()
                        )
                        updatedInfoPerMonthDataStore.removeAll{it.date == updatedInfoOfMonth.date}
                        updatedInfoPerMonthDataStore.add(updatedInfoOfMonth)
                    }
                        //Add new expenses
                    expenseList.forEach { newExpenseDataStore ->
                        val formattedExpense = Expense(
                            newExpenseDataStore.id,
                            newExpenseDataStore.price,
                            newExpenseDataStore.description,
                            newExpenseDataStore.category,
                            FormatValuesFromDatabase().date(newExpenseDataStore.paymentDate),
                            FormatValuesFromDatabase().date(newExpenseDataStore.purchaseDate),
                            newExpenseDataStore.inputDateTime
                        )
                        updatedExpenseListDataStore.add(formattedExpense)
                        //Add new expense values to info per month list
                        //TODO Need to test
                        val newExpenseDateYYYYmm = DateFunctions().YYYYmmDDtoYYYYmm(newExpenseDataStore.paymentDate)
                        val infoOfMonth = updatedInfoPerMonthDataStore.find { it.date == newExpenseDateYYYYmm }
                        if(infoOfMonth != null){
                            val currentMonthExpenseNewExpense = BigDecimal(infoOfMonth.monthExpense).setScale(8, RoundingMode.HALF_UP)
                            val currentAvailableNowNewExpense = BigDecimal(infoOfMonth.availableNow).setScale(8, RoundingMode.HALF_UP)
                            val updatedMonthExpenseNewExpense = currentMonthExpenseNewExpense.add(BigDecimal(newExpenseDataStore.price))
                            val updatedAvailableNowNewExpense = currentAvailableNowNewExpense.subtract(BigDecimal(newExpenseDataStore.price))
                            val updatedInfoOfMonth = InformationPerMonthExpense(
                                infoOfMonth.date,
                                updatedMonthExpenseNewExpense.toString(),
                                infoOfMonth.budget,
                                updatedAvailableNowNewExpense.toString()
                            )
                            updatedInfoPerMonthDataStore.removeAll{it.date == updatedInfoOfMonth.date}
                            updatedInfoPerMonthDataStore.add(updatedInfoOfMonth)
                        }else{
                            //TODO make the case when doesn't exist date
                        }
                    }
                    //Update expense list on dataStore
                    dataStore.updateAndResetExpenseList(updatedExpenseListDataStore)



                    //Update expense months on dataStore
                    //TODO

                    _editExpenseResult.postValue(true)
                },
                onFailure = {
                    _editExpenseResult.postValue(false)
                }
            )
        }
    }

}

