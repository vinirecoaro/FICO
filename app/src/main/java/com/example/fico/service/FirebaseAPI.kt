package com.example.fico.service

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.fico.model.Expense
import com.example.fico.model.User
import com.example.fico.model.UserData
import com.example.fico.service.constants.AppConstants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*

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
        //Update Expense List
        val reference = expense_list.child(expense.date).child(expense.description)
        val values = HashMap<String, Any>()
        values[AppConstants.DATABASE.PRICE] = expense.price
        values[AppConstants.DATABASE.CATEGORY] = expense.category
        reference.updateChildren(values)

        //Update Total Expense
        total_expense.addValueEventListener(object : ValueEventListener{
            val currentTotalExpense : String = ""
            val addValueTotalExpense : String = ""
            val newTotalExpense : Float = 0.0f
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTotalExpense = snapshot.value.toString()
                val addValueTotalExpense = expense.price.toFloat()
                val newTotalExpense = currentTotalExpense.toFloat() + addValueTotalExpense.toFloat()

            }
            total_expense.setValue(newTotalExpense.toString())

            override fun onCancelled(error: DatabaseError) {

            }

        })


        /*//Update Information per Month - Available Now
        information_per_month.child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).get().addOnSuccessListener {
            val currentAvailableNow = information_per_month.child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).get().toString().toFloat()
            val subValueAvailableNow = expense.price.toFloat()
            val newAvailableNow = currentAvailableNow - subValueAvailableNow
            information_per_month.child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(newAvailableNow.toString())
        }.addOnFailureListener {
            information_per_month.child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(expense.price)
        }

        //Update Information per Month - Budget
        information_per_month.child(AppConstants.DATABASE.BUDGET).setValue("0.00")

        //Update Information per Month - Expenses
        information_per_month.child(AppConstants.DATABASE.EXPENSES).setValue("0.00")*/
    }

}