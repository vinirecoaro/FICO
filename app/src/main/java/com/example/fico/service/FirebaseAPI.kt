package com.example.fico.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.model.Expense
import com.example.fico.model.User
import com.example.fico.service.constants.AppConstants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class FirebaseAPI private constructor() {

    private object HOLDER {
        val INSTANCE = FirebaseAPI()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    }

    companion object {
        val instance: FirebaseAPI by lazy { HOLDER.INSTANCE }
        private val auth: FirebaseAuth by lazy { HOLDER.mAuth }
        private val database: FirebaseDatabase by lazy { HOLDER.mDatabase }
        private val rootRef = database.getReference(AppConstants.DATABASE.USERS)
        private val total_expense = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.TOTAL_EXPENSE)
        private val information_per_month = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH)
        private val expense_list = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES_LIST)
        private val default_values = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.DEFAULT_VALUES)

    }

    suspend fun currentUser(): FirebaseUser? = withContext(Dispatchers.IO){
        return@withContext auth.currentUser
    }

    suspend fun createUser(user: User) : Task<AuthResult> = withContext(Dispatchers.IO){
        return@withContext auth.createUserWithEmailAndPassword(user.email, user.password)
    }

    suspend fun login(user: User) : Task<AuthResult> = withContext(Dispatchers.IO){
        return@withContext auth.signInWithEmailAndPassword(user.email, user.password)
    }

    suspend fun sendEmailVerification(): Task<Void>?  = withContext(Dispatchers.IO){
        return@withContext auth.currentUser?.sendEmailVerification()
    }

    suspend fun stateListener() = withContext(Dispatchers.IO){
        return@withContext auth.addAuthStateListener {  }
    }

    suspend fun logoff() = withContext(Dispatchers.IO){
        return@withContext auth.signOut()
    }

    suspend fun verifyIfUserExists(): Task<SignInMethodQueryResult>  = withContext(Dispatchers.IO){
        return@withContext auth.fetchSignInMethodsForEmail(currentUser()?.email.toString())
    }

    suspend fun addNewUserOnDatabase() = withContext(Dispatchers.IO)
    {
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES_LIST).setValue("")
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).setValue("")
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.TOTAL_EXPENSE).setValue("0.00")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun addExpense(expense: Expense, inputTime : String){
        updateExpenseList(expense, inputTime)
        updateTotalExpense(expense.price)
        updateInformationPerMonth(expense)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun editExpense(oldExpense: Expense, newExpense : Expense, inputTime : String){
        deleteExpense(oldExpense)
        updateExpenseList(newExpense, inputTime)
        updateTotalExpense(newExpense.price)
        updateInformationPerMonth(newExpense)
    }

    suspend fun deleteExpense(oldExpense : Expense) = withContext(Dispatchers.IO){
        updateTotalExpense(oldExpense.price)
        updateInformationPerMonth(oldExpense)
        val oldExpenseReference = expense_list.child(oldExpense.id)
        oldExpenseReference.removeValue()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun addInstallmentExpense(expense: Expense, inputTime : String, nOfInstallments : Int) = withContext(Dispatchers.IO){
        for (i in 0 until nOfInstallments){
            val month = expense.date.substring(5,7).toInt()
            var newMonth = month + i
            var year = expense.date.substring(0,4).toInt()
            var sumYear : Int = 0
            var day = expense.date.substring(8,10).toInt()
            var newDescription = expense.description + " Parcela ${i+1}"
            if(newMonth > 12 ){
                if(newMonth % 12 == 0){
                    sumYear = newMonth/12 - 1
                    newMonth -= 12*sumYear
                }else{
                    sumYear = newMonth/12
                    newMonth -= 12*sumYear
                }
                if(newMonth == 2){
                    if (day > 28){
                        day = 28
                    }
                }
                year += sumYear
            }
            var newMonthFormatted = newMonth.toString()
            if(newMonth < 10){
                newMonthFormatted = "0$newMonth"
            }
            var dayFormatted = day.toString()
            if(day < 10){
                dayFormatted = "0$day"
            }

            val date = "$year-$newMonthFormatted-$dayFormatted"
            val newExpense = Expense("",expense.price, newDescription, expense.category, date)

            val dateInformationPerMonth = "$year-$newMonthFormatted"
            val existDate = checkIfExistsDateOnDatabse(dateInformationPerMonth)

            if(existDate){
                updateExpenseList(newExpense, inputTime)
                updateTotalExpense(newExpense.price)
                updateInformationPerMonth(newExpense)
            }else{
                setUpBudget(getDefaultBudget(false),dateInformationPerMonth)
                updateExpenseList(newExpense, inputTime)
                updateTotalExpense(newExpense.price)
                updateInformationPerMonth(newExpense)
            }

        }
    }

    suspend fun setUpBudget(budget: String, date: String) = withContext(Dispatchers.IO){
        information_per_month.child(date).child(AppConstants.DATABASE.BUDGET).setValue(budget)
        information_per_month.child(date).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(budget)
        information_per_month.child(date).child(AppConstants.DATABASE.EXPENSE).setValue("0.00")
    }

    suspend fun setDefaultBudget(budget: String) : Boolean = withContext(Dispatchers.IO){
        val result = CompletableDeferred<Boolean>()
        try {
            default_values.child(AppConstants.DATABASE.DEFAULT_BUDGET).setValue(budget)
            result.complete(true)
        }catch (e:Exception){
            result.complete(false)
        }
        return@withContext result.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getDefaultBudget(formatted : Boolean = true) : String = withContext(Dispatchers.IO){
        val defaultBudget = CompletableDeferred<String>()
        default_values.child(AppConstants.DATABASE.DEFAULT_BUDGET).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value.toString().toFloat()
                if(formatted){
                    defaultBudget.complete("R$%.2f".format(value).replace(".", ","))
                }else{
                    defaultBudget.complete(value.toString())
                }


            }
            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
        return@withContext defaultBudget.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistsDateOnDatabse(date: String): Boolean = withContext(Dispatchers.IO) {
        val futureResult = CompletableDeferred<Boolean>()

        information_per_month.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                futureResult.complete(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                futureResult.complete(false)
            }
        })

        return@withContext futureResult.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistsOnDatabse(reference: DatabaseReference): Boolean = withContext(Dispatchers.IO) {
        val futureResult = CompletableDeferred<Boolean>()

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                futureResult.complete(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                futureResult.complete(false)
            }
        })

        return@withContext futureResult.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget(): Boolean {
        return checkIfExistsOnDatabse(default_values.child(AppConstants.DATABASE.DEFAULT_BUDGET))
    }


    private suspend fun updateTotalExpense(value: String) = withContext(Dispatchers.IO){
        total_expense.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTotalExpense = snapshot.value.toString().toFloat()
                val addValue = value.toFloat()
                val newValue = currentTotalExpense + addValue
                total_expense.setValue(newValue.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun updateExpenseList(expense: Expense, inputTime : String) = withContext(Dispatchers.IO){
        var control = false;
        while (!control){
            if(expense.id == ""){
                val expenseId = generateRandomAddress(5)
                val reference = expense_list.child("${expense.date}-${inputTime}${expenseId}")
                val exists = checkIfExistsOnDatabse(reference)
                if(!exists){
                    reference.child(AppConstants.DATABASE.PRICE).setValue(expense.price)
                    reference.child(AppConstants.DATABASE.DESCRIPTION).setValue(expense.description)
                    reference.child(AppConstants.DATABASE.DATE).setValue(expense.date)
                    reference.child(AppConstants.DATABASE.CATEGORY).setValue(expense.category)
                    control = true
                }
            }else{
                val reference = expense_list.child(expense.id)
                val exists = checkIfExistsOnDatabse(reference)
                if(!exists){
                    reference.child(AppConstants.DATABASE.PRICE).setValue(expense.price)
                    reference.child(AppConstants.DATABASE.DESCRIPTION).setValue(expense.description)
                    reference.child(AppConstants.DATABASE.DATE).setValue(expense.date)
                    reference.child(AppConstants.DATABASE.CATEGORY).setValue(expense.category)
                    control = true
                }
            }


        }

    }

    private suspend fun updateInformationPerMonth(expense: Expense)= withContext(Dispatchers.IO){
        information_per_month.child(expense.date.substring(0,7)).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedExpense = sumOldAndNewValue(expense, snapshot, AppConstants.DATABASE.EXPENSE)
                information_per_month.child(expense.date.substring(0,7)).child(AppConstants.DATABASE.EXPENSE).setValue(updatedExpense)
                val updatedAvailable = subOldAndNewValue(expense, snapshot, AppConstants.DATABASE.AVAILABLE_NOW)
                information_per_month.child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(updatedAvailable)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getAvailableNow(date: String) : String = withContext(Dispatchers.IO) {
        val availableNow = CompletableDeferred<String>()
        information_per_month.child(date).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val availableValue = snapshot.child(AppConstants.DATABASE.AVAILABLE_NOW).getValue(String::class.java)?.toFloat()
                    availableNow.complete("R$%.2f".format(availableValue).replace(".", ","))
                }else{
                    availableNow.complete("---")
                }

            }
            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
        return@withContext availableNow.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getTotalExpense() : String = withContext(Dispatchers.IO){
        val totalExpense = CompletableDeferred<String>()
        total_expense.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value.toString().toFloat()
                totalExpense.complete("R$%.2f".format(value).replace(".", ","))

            }
            override fun onCancelled(error: DatabaseError) {

            }
        }
        )
        return@withContext totalExpense.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getMonthExpense(date: String): String = withContext(Dispatchers.IO) {
        val deferredExpense = CompletableDeferred<String>()

        information_per_month.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val monthExpenseValue = snapshot.child(AppConstants.DATABASE.EXPENSE).getValue(String::class.java)
                        ?.toFloat()
                    deferredExpense.complete("R$%.2f".format(monthExpenseValue).replace(".", ","))
                } else {
                    deferredExpense.complete("---")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                deferredExpense.completeExceptionally(error.toException())
            }
        })

        return@withContext deferredExpense.await()
    }


    fun sumOldAndNewValue(expense: Expense, snapshot: DataSnapshot, child: String): String {
        val current = snapshot.child(child).value.toString().toFloat()
        val add = expense.price.toFloat()
        val new = current + add
        val floatFormat = "%.${2}f"
        return String.format(floatFormat, new).replace(",",".")
    }

    fun subOldAndNewValue(expense: Expense, snapshot: DataSnapshot, child: String): String {
        val current = snapshot.child(child).value.toString().toFloat()
        val sub = expense.price.toFloat()
        val new = current - sub
        val floatFormat = "%.${2}f"
        return String.format(floatFormat, new).replace(",",".")
    }

    suspend fun getExpenseList(filter: String = ""): List<Expense> = withContext(Dispatchers.IO) {
        return@withContext suspendCoroutine { continuation ->
            var isCompleted = false
            expense_list.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<Expense>()
                    if (snapshot.exists()) {
                        if (filter == "") {
                            for (childSnapshot in snapshot.children) {
                                val id = childSnapshot.key.toString()
                                val priceDatabase = childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString().toFloat()
                                val priceFormatted = "R$ %.2f".format(priceDatabase).replace(".", ",")
                                val description = childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                                val category = childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                                val dateDatabase = childSnapshot.child(AppConstants.DATABASE.DATE).value.toString()
                                var dateFormatted = "${dateDatabase.substring(8, 10)}/${dateDatabase.substring(5, 7)}/${dateDatabase.substring(0, 4)}"
                                val expense = Expense(id, priceFormatted, description, category, dateFormatted)
                                expenses.add(expense)
                            }
                        } else {
                            for (childSnapshot in snapshot.children) {
                                val dateDatabase = childSnapshot.child(AppConstants.DATABASE.DATE).value.toString()
                                val dateFromDatabase = "${dateDatabase.substring(0, 4)}-${dateDatabase.substring(5, 7)}"
                                val dateFromFilter = formatDateForDatabase(filter)
                                if (dateFromDatabase == dateFromFilter) {
                                    val id = childSnapshot.key.toString()
                                    val priceDatabase = childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString().toFloat()
                                    val priceFormatted = "R$ %.2f".format(priceDatabase).replace(".", ",")
                                    val description = childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                                    val category = childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                                    val dateFormatted = "${dateDatabase.substring(8, 10)}/${dateDatabase.substring(5, 7)}/${dateDatabase.substring(0, 4)}"
                                    val expense = Expense(id, priceFormatted, description, category, dateFormatted)
                                    expenses.add(expense)
                                }
                            }
                        }
                    }
                    if (!isCompleted) { // Verifica se já foi retomado
                        isCompleted = true
                        continuation.resume(expenses)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isCompleted) { // Verifica se já foi retomado
                        isCompleted = true
                        continuation.resume(emptyList())
                    }
                }
            })
        }
    }

    suspend fun getExpenseMonths() : List<String> = suspendCoroutine{ continuation ->
        var isCompleted = false
        information_per_month.addValueEventListener(object : ValueEventListener{
            val expenseMonths = mutableListOf<String>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for(month in snapshot.children){
                    val formattedDate = formatDateForFilterOnExpenseList(month.key.toString())
                    expenseMonths.add(formattedDate)
                }
                if (!isCompleted) { // Verifica se já foi retomado
                    isCompleted = true
                    continuation.resume(expenseMonths)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isCompleted) { // Verifica se já foi retomado
                    isCompleted = true
                    continuation.resume(emptyList())
                }
            }

        })
    }

    private fun generateRandomAddress(size: Int): String {
        val caracteresPermitidos = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = Random(System.currentTimeMillis())
        val sequenciaAleatoria = StringBuilder(size)

        for (i in 0 until size) {
            val index = random.nextInt(caracteresPermitidos.length)
            sequenciaAleatoria.append(caracteresPermitidos[index])
        }

        return sequenciaAleatoria.toString()
    }

    private fun formatDateForFilterOnExpenseList(date: String) : String{
        var formattedDate = ""
        val month = date.substring(5,7)
        if(month == "01"){
            formattedDate = "Janeiro - ${date.substring(0,4)}"
        } else if(month == "02"){
            formattedDate = "Fevereiro - ${date.substring(0,4)}"
        } else if(month == "03"){
            formattedDate = "Março - ${date.substring(0,4)}"
        } else if(month == "04"){
            formattedDate = "Abril - ${date.substring(0,4)}"
        } else if(month == "05"){
            formattedDate = "Maio - ${date.substring(0,4)}"
        } else if(month == "07"){
            formattedDate = "Julho - ${date.substring(0,4)}"
        } else if(month == "08"){
            formattedDate = "Agosto - ${date.substring(0,4)}"
        } else if(month == "09"){
            formattedDate = "Setembro - ${date.substring(0,4)}"
        } else if(month == "10"){
            formattedDate = "Outubro - ${date.substring(0,4)}"
        } else if(month == "11"){
            formattedDate = "Novembro - ${date.substring(0,4)}"
        } else if(month == "12"){
            formattedDate = "Dezembro - ${date.substring(0,4)}"
        }
        return formattedDate
    }

    private fun formatDateForDatabase(date: String) : String{
        var formattedDate = ""
        val stringParts = date.split(" ")
        val month = stringParts[0]
        val year = stringParts[2]

        if(month == "Janeiro"){
            formattedDate = "$year-01"
        } else if(month == "Fevereiro"){
            formattedDate = "$year-02"
        }  else if(month == "Março"){
            formattedDate = "$year-03"
        } else if(month == "Abril"){
            formattedDate = "$year-04"
        } else if(month == "Maio"){
            formattedDate = "$year-05"
        } else if(month == "Junho"){
            formattedDate = "$year-06"
        } else if(month == "Julho"){
            formattedDate = "$year-07"
        } else if(month == "Agosto"){
            formattedDate = "$year-08"
        } else if(month == "Setembro"){
            formattedDate = "$year-09"
        } else if(month == "Outubro"){
            formattedDate = "$year-10"
        } else if(month == "Novembro"){
            formattedDate = "$year-11"
        } else if(month == "Dezembro"){
            formattedDate = "$year-12"
        }
        return formattedDate
    }


}


