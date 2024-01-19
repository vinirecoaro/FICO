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
                    for (expense in _expenses){

                        val expensePriceFormatted = BigDecimal(expense.price).toString()

                        val _expense = Expense("", expensePriceFormatted, expense.description, expense.category, expense.date)

                        val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(_expense, installmentExpense, 1)

                        val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(sumAllExpenses(_expenses), 1, serviceScope).await()

                        val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(_expense, installmentExpense, 1, serviceScope, false).await()

                        val expenseInfos = UpdateFromFileExpenseList(expenseList, 1, updatedTotalExpense, updatedInformationPerMonth)

                        masterExpenseList.add(expenseInfos)
                        //firebaseAPI.addExpense2(expenseList, 1, updatedTotalExpense, updatedInformationPerMonth)

                        /*val dateToCheck = expense.date.substring(0,7)
                        val existDate = checkIfExistsDateOnDatabse(dateToCheck)
                        if(existDate.await()){
                            val _expense = Expense("", expense.price, expense.description, expense.category, expense.date)
                            val timeNow = LocalTime.now()
                            var hour = timeNow.hour.toString()
                            var minute = timeNow.minute.toString()
                            var second = timeNow.second.toString()
                            if(timeNow.hour < 10){
                                hour = "0${timeNow.hour}"
                            }
                            if(timeNow.minute < 10){
                                minute = "0${timeNow.minute}"
                            }
                            if(timeNow.second < 10){
                                second = "0${timeNow.second}"
                            }
                            val inputTime = "${hour}-${minute}-${second}"
                            firebaseAPI.addExpense2(_expense, inputTime)
                            delay(100)
                        }else{
                            setUpBudget(
                                getDefaultBudget().await(),
                                dateToCheck)
                            val _expense = Expense("", expense.price, expense.description, expense.category, expense.date)
                            val timeNow = LocalTime.now()
                            var hour = timeNow.hour.toString()
                            var minute = timeNow.minute.toString()
                            var second = timeNow.second.toString()
                            if(timeNow.hour < 10){
                                hour = "0${timeNow.hour}"
                            }
                            if(timeNow.minute < 10){
                                minute = "0${timeNow.minute}"
                            }
                            if(timeNow.second < 10){
                                second = "0${timeNow.second}"
                            }
                            val inputTime = "${hour}-${minute}-${second}"
                            firebaseAPI.addExpense2(_expense, inputTime)
                            delay(100)
                        }*/
                    }
                    if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                        val intentConcludedWarning = Intent(AppConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                        sendBroadcast(intentConcludedWarning)
                    }
                }
            } else{
                if (_expenses != null) {
                    for (expense in _expenses){

                        val expensePriceFormatted = BigDecimal(expense.price).toString()

                        val _expense = Expense("", expensePriceFormatted, expense.description, expense.category, expense.date)

                        val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(_expense, installmentExpense, expense.nOfInstallment.toFloat().toInt())

                        val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(sumAllExpenses(_expenses), 1, serviceScope).await()

                        val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(_expense, installmentExpense, expense.nOfInstallment.toFloat().toInt(), serviceScope, false).await()

                        val expenseInfos = UpdateFromFileExpenseList(expenseList, expense.nOfInstallment.toFloat().toInt(), updatedTotalExpense, updatedInformationPerMonth)

                        masterExpenseList.add(expenseInfos)

                        //firebaseAPI.addExpense2(expenseList, expense.nOfInstallment.toInt(), updatedTotalExpense, updatedInformationPerMonth)

                        /*val denominator = BigDecimal(expense.price)
                        val divisor = BigDecimal(expense.nOfInstallment)
                        val expenseInstallment = denominator.divide(divisor, 8, RoundingMode.HALF_UP).toString()
                        val _expense = Expense("", expenseInstallment, expense.description, expense.category, expense.date, expense.nOfInstallment)
                        val timeNow = LocalTime.now()
                        var hour = timeNow.hour.toString()
                        var minute = timeNow.minute.toString()
                        var second = timeNow.second.toString()
                        if(timeNow.hour < 10){
                            hour = "0${timeNow.hour}"
                        }
                        if(timeNow.minute < 10){
                            minute = "0${timeNow.minute}"
                        }
                        if(timeNow.second < 10){
                            second = "0${timeNow.second}"
                        }
                        val inputTime = "${hour}-${minute}-${second}"
                        firebaseAPI.addInstallmentExpense(_expense, inputTime, _expense.nOfInstallment.toFloat().toInt())
                        delay(100)*/
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