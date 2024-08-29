package com.example.fico.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.fico.DataStoreManager
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Expense
import com.example.fico.model.UpdateFromFileExpenseList
import com.example.fico.shared.constants.StringConstants
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode

class UploadFile(private val firebaseAPI : FirebaseAPI, private val dataStore:DataStoreManager) : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private val arrangeDataToUpdateToDatabase  = ArrangeDataToUpdateToDatabase()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val _expenses = intent?.getParcelableArrayListExtra<Expense>("expensesList")
        val installmentExpense : Boolean = intent?.getBooleanExtra("installmentExpense", false) == true

        serviceScope.launch {

            var masterExpenseList = UpdateFromFileExpenseList(
                mutableListOf(),1,"0", mutableListOf())

            if(!installmentExpense){
                if (_expenses != null) {

                    val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                        dataStore.getTotalExpense(),
                        sumAllExpenses(_expenses),
                        1
                    )

                    masterExpenseList.updatedTotalExpense = updatedTotalExpense

                    val expensePerMonthList = arrangeDataToUpdateToDatabase.joinExpensesOfMonth(_expenses, installmentExpense)

                    val updatedInformationPerMonth = arrangeDataToUpdateToDatabase.addToInformationPerMonthFromUpdatedFile(
                        expensePerMonthList,
                        dataStore.getExpenseInfoPerMonth(),
                        dataStore.getDefaultBudget()
                    )

                    for (info in updatedInformationPerMonth){
                        masterExpenseList.updatedInformationPerMonth.add(info)
                    }

                    val expenseList = arrangeDataToUpdateToDatabase.addToExpenseListFromFileCommonExpense(_expenses)

                   for(expenseFromExpenseList in expenseList){
                       masterExpenseList.expenseList.add(expenseFromExpenseList)
                   }

                    if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                        val intentConcludedWarning = Intent(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                        sendBroadcast(intentConcludedWarning)
                    }
                }
            } else{
                if (_expenses != null) {

                    val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                        dataStore.getTotalExpense(),
                        sumAllExpenses(_expenses),
                        1,
                    )

                    masterExpenseList.updatedTotalExpense = updatedTotalExpense

                    val expensePerMonthList = arrangeDataToUpdateToDatabase.joinExpensesOfMonth(_expenses, installmentExpense)

                    val updatedInformationPerMonth = arrangeDataToUpdateToDatabase.addToInformationPerMonthFromUpdatedFile(
                        expensePerMonthList,
                        dataStore.getExpenseInfoPerMonth(),
                        dataStore.getDefaultBudget()
                    )

                    for (info in updatedInformationPerMonth){
                        masterExpenseList.updatedInformationPerMonth.add(info)
                    }

                    for (expense in _expenses){

                        val expensePriceFormatted = BigDecimal(expense.price).divide(BigDecimal(expense.nOfInstallment), 8, RoundingMode.HALF_UP).toString()

                        val _expense = Expense("", expensePriceFormatted, expense.description, expense.category, expense.paymentDate, expense.purchaseDate, expense.inputDateTime)

                        val expenseList = arrangeDataToUpdateToDatabase.addToExpenseList(_expense, installmentExpense, expense.nOfInstallment.toFloat().toInt())

                        for(expenseFromExpenseList in expenseList){
                            masterExpenseList.expenseList.add(expenseFromExpenseList)
                        }

                    }

                    if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                        val intentConcludedWarning = Intent(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
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