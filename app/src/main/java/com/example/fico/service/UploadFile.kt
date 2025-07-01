package com.example.fico.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.fico.DataStoreManager
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Expense
import com.example.fico.model.UpdateFromFileExpenseList
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import java.math.RoundingMode

class UploadFile : Service() {

    private val dataStore : DataStoreManager by inject()
    private val firebaseAPI : FirebaseAPI by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private val arrangeDataToUpdateToDatabase  = ArrangeDataToUpdateToDatabase()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val expenseList = intent?.getParcelableArrayListExtra<Expense>(StringConstants.XLS.EXPENSE_LIST)
        val earningList = intent?.getParcelableArrayListExtra<Expense>(StringConstants.XLS.EARNING_LIST)
        val installmentExpenseList = intent?.getParcelableArrayListExtra<Expense>(StringConstants.XLS.INSTALLMENT_EXPENSE_LIST)

        serviceScope.launch {

            var masterExpenseList = UpdateFromFileExpenseList(
                mutableListOf(),1,"0", mutableListOf())

            //Total expense
            val notNullExpenseList = expenseList ?: mutableListOf()
            val notNullInstallmentExpenseList = installmentExpenseList ?: mutableListOf()
            val totalExpenseFromFile = sumAllExpenses(notNullExpenseList, notNullInstallmentExpenseList)

            val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                dataStore.getTotalExpense(),
                totalExpenseFromFile,
                1
            )

            masterExpenseList.updatedTotalExpense = updatedTotalExpense

            //Expense per month
            val expensePerMonthList = arrangeDataToUpdateToDatabase.joinExpensesOfMonth(notNullExpenseList, notNullInstallmentExpenseList)

            val updatedInformationPerMonth = arrangeDataToUpdateToDatabase.addToInformationPerMonthFromUpdatedFile(
                expensePerMonthList,
                dataStore.getExpenseInfoPerMonth(),
                dataStore.getDefaultBudget()
            )

            for (info in updatedInformationPerMonth){
                masterExpenseList.updatedInformationPerMonth.add(info)
            }

            Log.e("e", expensePerMonthList.toString())

            return@launch

            /*if (expenseList != null) {

                val expenseList = arrangeDataToUpdateToDatabase.addToExpenseListFromFileCommonExpense(expenseList)

               for(expenseFromExpenseList in expenseList){
                   masterExpenseList.expenseList.add(expenseFromExpenseList)
               }

                if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                    val intentConcludedWarning = Intent(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                    sendBroadcast(intentConcludedWarning)
                }
            }*/

            //Installment expense
            /*if (installmentExpenseList != null) {

                val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                    dataStore.getTotalExpense(),
                    sumAllExpenses(installmentExpenseList),
                    1,
                )

                masterExpenseList.updatedTotalExpense = updatedTotalExpense

                val expensePerMonthList = arrangeDataToUpdateToDatabase.joinExpensesOfMonth(installmentExpenseList, installmentExpense)

                val updatedInformationPerMonth = arrangeDataToUpdateToDatabase.addToInformationPerMonthFromUpdatedFile(
                    expensePerMonthList,
                    dataStore.getExpenseInfoPerMonth(),
                    dataStore.getDefaultBudget()
                )

                for (info in updatedInformationPerMonth){
                    masterExpenseList.updatedInformationPerMonth.add(info)
                }

                for (expense in installmentExpenseList){

                    val expensePriceFormatted = BigDecimal(expense.price).divide(BigDecimal(expense.nOfInstallment), 8, RoundingMode.HALF_UP).toString()

                    val _expense = Expense("", expensePriceFormatted, expense.description, expense.category, expense.paymentDate, expense.purchaseDate, expense.inputDateTime)

                    val expenseList = arrangeDataToUpdateToDatabase.addToExpenseList(_expense, installmentExpense, expense.nOfInstallment.toFloat().toInt(), false)

                    for(expenseFromExpenseList in expenseList){
                        masterExpenseList.expenseList.add(expenseFromExpenseList)
                    }

                }

                if(firebaseAPI.addExpenseFromFile(masterExpenseList)){
                    val intentConcludedWarning = Intent(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                    sendBroadcast(intentConcludedWarning)
                }

            }*/

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

    private fun sumAllExpenses(expenseList : MutableList<Expense>, installmentExpenseList : MutableList<Expense>) : String{
        var allExpensePrice = BigDecimal(0)
        for (expense in expenseList){
            val expensePrice = BigDecimal(expense.price)
            allExpensePrice = allExpensePrice.add(expensePrice)
        }
        for (expense in installmentExpenseList){
            val expensePrice = BigDecimal(expense.price)
            allExpensePrice = allExpensePrice.add(expensePrice)
        }
        allExpensePrice.setScale(8, RoundingMode.HALF_UP)
        return allExpensePrice.toString()
    }
}