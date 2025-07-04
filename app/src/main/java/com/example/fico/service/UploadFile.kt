package com.example.fico.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.fico.DataStoreManager
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.UpdateTransactionFromFileInfo
import com.example.fico.repositories.TransactionsRepository
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.math.BigDecimal
import java.math.RoundingMode

class UploadFile : Service() {

    private val dataStore : DataStoreManager by inject()
    private val transactionsRepository: TransactionsRepository by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private val arrangeDataToUpdateToDatabase  = ArrangeDataToUpdateToDatabase()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val expenseList = intent?.getParcelableArrayListExtra<Expense>(StringConstants.XLS.EXPENSE_LIST)
        val earningList = intent?.getParcelableArrayListExtra<Earning>(StringConstants.XLS.EARNING_LIST)
        val installmentExpenseList = intent?.getParcelableArrayListExtra<Expense>(StringConstants.XLS.INSTALLMENT_EXPENSE_LIST)

        serviceScope.launch {

            var transactionFromFileInfo = UpdateTransactionFromFileInfo(
                "", mutableListOf(), "0", mutableListOf(),
                mutableListOf(), mutableListOf(), mutableListOf(),
                "", mutableListOf(), ""
            )

            //Input Date time
            transactionFromFileInfo.inputDateTime = FormatValuesToDatabase().dateTimeNow()

            //Total expense
            val notNullExpenseList = expenseList ?: mutableListOf()
            val notNullInstallmentExpenseList = installmentExpenseList ?: mutableListOf()
            val notNullEarningList = earningList ?: mutableListOf()
            val totalExpenseFromFile = sumAllExpenses(notNullExpenseList, notNullInstallmentExpenseList)
            transactionFromFileInfo.totalExpenseFromFile = totalExpenseFromFile // add to log

            val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                dataStore.getTotalExpense(),
                totalExpenseFromFile, 1
            )

            transactionFromFileInfo.updatedTotalExpense = updatedTotalExpense


            //Expense per month
            val expensePerMonthList = arrangeDataToUpdateToDatabase.joinExpensesOfMonth(notNullExpenseList, notNullInstallmentExpenseList)
            transactionFromFileInfo.expensePerMonthList.addAll(expensePerMonthList) // Add to log

            val updatedInformationPerMonth = arrangeDataToUpdateToDatabase.addToInformationPerMonthFromUpdatedFile(
                expensePerMonthList,
                dataStore.getExpenseInfoPerMonth(),
                dataStore.getDefaultBudget()
            )

            transactionFromFileInfo.updatedInformationPerMonth.addAll(updatedInformationPerMonth)


            //Expense list
            val expenseListFormatted = arrangeDataToUpdateToDatabase.addToExpenseListFromFile(notNullExpenseList, notNullInstallmentExpenseList)

            transactionFromFileInfo.expenseList.addAll(expenseListFormatted)


            //Earning list
            val earningListFormatted = arrangeDataToUpdateToDatabase.addToEarningListFromFile(notNullEarningList)

            transactionFromFileInfo.earningList.addAll(earningListFormatted)


            //log for upload from file
            transactionFromFileInfo.expenseIdList.addAll(expenseListFormatted.map { it.id })
            transactionFromFileInfo.earningIdList.addAll(earningListFormatted.map { it.id })


            //Add to database
            val uploadId = transactionsRepository.addTransactionsFromFile(transactionFromFileInfo)
            if(uploadId != null){

                transactionFromFileInfo.id = uploadId

                //Update datastore
                updateDatastore(transactionFromFileInfo)

                val intentConcludedWarning = Intent(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                sendBroadcast(intentConcludedWarning)
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

    private suspend fun updateDatastore(
        transactionFromFileInfo : UpdateTransactionFromFileInfo
    ){

        //Save transactions info
        dataStore.updateTotalExpense(transactionFromFileInfo.updatedTotalExpense)
        dataStore.updateAndResetInfoPerMonthExpense(transactionFromFileInfo.updatedInformationPerMonth)
        dataStore.updateExpenseList(transactionFromFileInfo.expenseList.apply {
            forEach {
                it.paymentDate = FormatValuesFromDatabase().date(it.paymentDate)
                it.purchaseDate = FormatValuesFromDatabase().date(it.purchaseDate)
            }
        })
        val monthList = mutableListOf<String>()
        for(month in transactionFromFileInfo.updatedInformationPerMonth){
            if(month.budget != month.availableNow){
                monthList.add(month.date)
            }
        }
        dataStore.updateAndResetExpenseMonths(monthList)
        for(earning in transactionFromFileInfo.earningList){
            dataStore.updateEarningList(earning)
        }
        val updatedEarningListFromDataStore = dataStore.getEarningsList()
        dataStore.updateAndResetEarningMonthInfoList(updatedEarningListFromDataStore)


        //Save upload log info
        dataStore.updateUploadsFromFileList(transactionFromFileInfo)
    }
}