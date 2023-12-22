package com.example.fico.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.constants.AppConstants
import kotlinx.coroutines.*
import java.time.LocalTime

class UploadFile : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val _expenses = intent?.getParcelableArrayListExtra<Expense>("expensesList")

        serviceScope.launch {

            if (_expenses != null) {
                for (expense in _expenses){
                    val dateToCheck = expense.date.substring(0,7)
                    val existDate = checkIfExistsDateOnDatabse(dateToCheck)
                    if(existDate.await()){
                        val _expense = Expense("", expense.price, expense.description, expense.category, expense.date)
                        val timeNow = LocalTime.now()
                        val inputTime = "${timeNow.hour}-${timeNow.minute}-${timeNow.second}"
                        firebaseAPI.addExpense(_expense, inputTime)
                        delay(100)
                    }else{
                        setUpBudget(
                            getDefaultBudget(formatted = false).await(),
                            dateToCheck)
                        val _expense = Expense("", expense.price, expense.description, expense.category, expense.date)
                        val timeNow = LocalTime.now()
                        val inputTime = "${timeNow.hour}-${timeNow.minute}-${timeNow.second}"
                        firebaseAPI.addExpense(_expense, inputTime)
                        delay(100)
                    }
                }
                val intentConcludedWarning = Intent(AppConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
                sendBroadcast(intentConcludedWarning)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // Certifique-se de cancelar todas as coroutines quando o serviço for destruído
        serviceScope.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkIfExistsDateOnDatabse(date: String): Deferred<Boolean> {
        return serviceScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistsDateOnDatabse(date)
        }
    }

    fun setUpBudget(budget: String, date: String){
        serviceScope.async (Dispatchers.IO){
            firebaseAPI.setUpBudget(budget, date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getDefaultBudget(formatted : Boolean = true):Deferred<String>{
        return serviceScope.async(Dispatchers.IO){
            firebaseAPI.getDefaultBudget(formatted)
        }
    }
}