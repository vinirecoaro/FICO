package com.example.fico.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.UpdateFromFileExpenseList
import com.example.fico.util.constants.AppConstants
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime

class UploadFile : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val _expenses = intent?.getParcelableArrayListExtra<Expense>("expensesList")
        val installmentExpense : Boolean = intent?.getBooleanExtra("installmentExpense", false) == true

        serviceScope.launch {

            val masterExpenseList : MutableList<UpdateFromFileExpenseList> = mutableListOf()

            if(!installmentExpense){
                if (_expenses != null) {

                    val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(sumAllExpenses(_expenses), 1, serviceScope).await()

                    val expensePerMonthList = ArrangeDataToUpdateToDatabase().joinExpensesOfMonth(_expenses, installmentExpense)


                    for (expense in _expenses){

                        val expensePriceFormatted = BigDecimal(expense.price).toString()

                        val _expense = Expense("", expensePriceFormatted, expense.description, expense.category, expense.date)

                        val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(_expense, installmentExpense, 1)

                        val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(_expense, installmentExpense, 1, serviceScope, false).await()

                        val expenseInfos = UpdateFromFileExpenseList(expenseList, 1, updatedTotalExpense, updatedInformationPerMonth)

                        masterExpenseList.add(expenseInfos)
                    }
                    if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                        val intentConcludedWarning = Intent(AppConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                        sendBroadcast(intentConcludedWarning)
                    }
                }
            } else{
                if (_expenses != null) {

                    val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(sumAllExpenses(_expenses), 1, serviceScope).await()

                    for (expense in _expenses){

                        val expensePriceFormatted = BigDecimal(expense.price).divide(BigDecimal(expense.nOfInstallment)).setScale(8, RoundingMode.HALF_UP).toString()

                        val _expense = Expense("", expensePriceFormatted, expense.description, expense.category, expense.date)

                        val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(_expense, installmentExpense, expense.nOfInstallment.toFloat().toInt())

                        val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(_expense, installmentExpense, expense.nOfInstallment.toFloat().toInt(), serviceScope, false).await()

                        val expenseInfos = UpdateFromFileExpenseList(expenseList, expense.nOfInstallment.toFloat().toInt(), updatedTotalExpense, updatedInformationPerMonth)

                        masterExpenseList.add(expenseInfos)

                    }

                    if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                        val intentConcludedWarning = Intent(AppConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                        sendBroadcast(intentConcludedWarning)
                    }

                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        //Certify to cancel all coroutines when service be destroyed.
        serviceScope.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkIfExistsDateOnDatabse(date: String): Deferred<Boolean> {
        return serviceScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistsDateOnDatabse(date).await()
        }
    }

    fun setUpBudget(budget: String, date: String){
        serviceScope.async (Dispatchers.IO){
            firebaseAPI.setUpBudget(budget, date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getDefaultBudget():Deferred<String>{
        return serviceScope.async(Dispatchers.IO){
            firebaseAPI.getDefaultBudget().await()
        }
    }

    private fun sumAllExpenses(expenseList : MutableList<Expense>) : String{
        var allExpensePrice = BigDecimal(0)
        for (expense in expenseList){
            val expensePrice = BigDecimal(expense.price)
            allExpensePrice = allExpensePrice.add(expensePrice)
        }
        allExpensePrice.setScale(8, RoundingMode.HALF_UP)
        return allExpensePrice.toString()
    }
}