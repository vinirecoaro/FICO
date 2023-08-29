package com.example.fico.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.model.Expense
import com.example.fico.model.User
import com.example.fico.service.constants.AppConstants
import com.example.fico.ui.adapters.ExpenseListAdapter
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

    suspend fun setUpBudget(budget: String, date: String) = withContext(Dispatchers.IO){
        information_per_month.child(date).child(AppConstants.DATABASE.BUDGET).setValue(budget)
        information_per_month.child(date).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(budget)
        information_per_month.child(date).child(AppConstants.DATABASE.EXPENSE).setValue("0.00")
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
        var availableNow = CompletableDeferred<String>()
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
        var totalExpense = CompletableDeferred<String>()
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
        return String.format(floatFormat, new)
    }

    fun subOldAndNewValue(expense: Expense, snapshot: DataSnapshot, child: String): String {
        val current = snapshot.child(child).value.toString().toFloat()
        val sub = expense.price.toFloat()
        val new = current - sub
        val floatFormat = "%.${2}f"
        return String.format(floatFormat, new)
    }

    fun getExpenseList(recyclerView : RecyclerView, expenses : MutableList<Expense>){
        expense_list.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for (childSnapshot in snapshot.children){
                        val priceDatabase = childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString().toFloat()
                        val priceFormated = "R$ %.2f".format(priceDatabase).replace(".", ",")
                        val description = childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                        val category = childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                        val dateDatabase = childSnapshot.child(AppConstants.DATABASE.DATE).value.toString()
                        val dateFormated = "${dateDatabase.substring(8,10)}/${dateDatabase.substring(5,7)}/${dateDatabase.substring(0,4)}"
                        val expense = Expense(priceFormated, description, category , dateFormated)
                        expenses.add(expense)
                    }
                }
                val expensesFormated = expenses.reversed()
                val adapter = ExpenseListAdapter(expensesFormated)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

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

}


