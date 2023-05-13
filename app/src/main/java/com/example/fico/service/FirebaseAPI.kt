package com.example.fico.service

import com.example.fico.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class FirebaseAPI private constructor() {

    private object HOLDER {
        val INSTANCE = FirebaseAPI()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    }

    companion object {
        val instance: FirebaseAPI by lazy { HOLDER.INSTANCE }
        val auth: FirebaseAuth by lazy { HOLDER.mAuth }
        val database: FirebaseDatabase by lazy { HOLDER.mDatabase }
    }

    /*companion object {
        @Volatile
        private var instance: FirebaseAPI? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FirebaseAPI().also { instance = it }
            }
    }*/

    fun createUser(user: User) : Task<AuthResult> {
        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email, user.password)
    }

    fun login(user: User) : Task<AuthResult> {
        return FirebaseAuth.getInstance().signInWithEmailAndPassword(user.email, user.password)
    }

    fun sendEmailVerification(): Task<Void>? {
        return FirebaseAPI.auth.currentUser?.sendEmailVerification()
    }

    fun stateListener(){
        val auth: FirebaseAuth = Firebase.auth
        return auth.addAuthStateListener {  }
    }

    fun logoff(){
        return FirebaseAuth.getInstance().signOut()
    }

}