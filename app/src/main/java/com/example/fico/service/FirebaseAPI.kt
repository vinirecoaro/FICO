package com.example.fico.service

import com.example.fico.model.Expense
import com.example.fico.model.User
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
        val auth: FirebaseAuth by lazy { HOLDER.mAuth }
        private val database: FirebaseDatabase by lazy { HOLDER.mDatabase }
        val rootRef = database.getReference(AppConstants.DATABASE.USERS)
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
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).setValue("0.00")
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.TOTAL_EXPENSE).setValue("0.00")
    }

    fun addExpense(expense: Expense){
        val reference = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES_LIST).child(expense.date).child(expense.description)
        val values = HashMap<String, Any>()
        values[AppConstants.DATABASE.PRICE] = expense.price
        values[AppConstants.DATABASE.CATEGORY] = expense.category
        reference.updateChildren(values)
        val currentTotalExpense = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.TOTAL_EXPENSE).get().toString().toFloat()
        val addValueTotalExpense = expense.price.toFloat()
        val newTotalExpense = currentTotalExpense + addValueTotalExpense
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.TOTAL_EXPENSE).setValue(newTotalExpense.toString())
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).get().addOnSuccessListener {
            val currentAvailableNow = rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).get().toString().toFloat()
            val subValueAvailableNow = expense.price.toFloat()
            val newAvailableNow = currentAvailableNow - subValueAvailableNow
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(newAvailableNow.toString())
        }.addOnFailureListener {
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).child(expense.date.substring(0,7)).child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(expense.price)
        }
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).child(AppConstants.DATABASE.BUDGET).setValue("0.00")
        rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.INFORMATION_PER_MONTH).child(AppConstants.DATABASE.EXPENSES).setValue("0.00")
    }

}