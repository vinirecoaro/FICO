package com.example.fico.service

import com.example.fico.model.Expense
import com.example.fico.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
        val rootRef = database.getReference("users")
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
        rootRef.child(auth.currentUser?.uid.toString()).setValue("")
    }

    fun addExpense(expense: Expense){
        val reference = rootRef.child(auth.currentUser?.uid.toString()).child(expense.date).child(expense.description)
        val values = HashMap<String, Any>()
        values["price"] = expense.price
        values["category"] = expense.category
        reference.updateChildren(values)
    }

}