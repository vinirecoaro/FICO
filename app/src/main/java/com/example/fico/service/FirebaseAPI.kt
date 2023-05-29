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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

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

    fun currentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun createUser(user: User) : Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(user.email, user.password)
    }

    fun login(user: User) : Task<AuthResult> {
        return auth.signInWithEmailAndPassword(user.email, user.password)
    }

    fun sendEmailVerification(): Task<Void>? {
        return auth.currentUser?.sendEmailVerification()
    }

    fun stateListener(){
        return auth.addAuthStateListener {  }
    }

    fun logoff(){
        return auth.signOut()
    }

    fun verifyIfUserExists(): Task<SignInMethodQueryResult> {
        return auth.fetchSignInMethodsForEmail(currentUser()?.email.toString())
    }

    fun addNewUserOnDatabase() {
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES_LIST).setValue("")
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).setValue("")
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.TOTAL_EXPENSE).setValue("0.00")
    }

    fun addExpense(expense: Expense){
        updateExpenseList(expense)
        updateTotalExpense(expense.price)
       //updateInformationPerMonth(expense)
    }

    fun setUpBudget(budget: String){

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkIfExistsDateOnDatabse(date: String): CompletableFuture<Boolean> {
        val futureResult = CompletableFuture<Boolean>()

        information_per_month.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                futureResult.complete(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                futureResult.complete(false)
            }
        })

        return futureResult
    }

    fun updateTotalExpense(value: String){
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

    fun updateExpenseList(expense: Expense){
        val reference = expense_list.child(expense.date).child(expense.description)
        val values = HashMap<String, Any>()
        values[AppConstants.DATABASE.PRICE] = expense.price
        values[AppConstants.DATABASE.CATEGORY] = expense.category
        reference.updateChildren(values)
    }

    fun updateInformationPerMonth(expense: Expense){
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

}


