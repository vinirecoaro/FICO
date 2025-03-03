package com.example.fico.repositories

import com.example.fico.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAuthRepository(private val auth: FirebaseAuth) {

    suspend fun currentUser(): FirebaseUser? = withContext(Dispatchers.IO) {
        return@withContext auth.currentUser
    }

    suspend fun createUser(user: User): Task<AuthResult> = withContext(Dispatchers.IO) {
        return@withContext auth.createUserWithEmailAndPassword(user.email, user.password)
    }

    suspend fun login(user: User): Task<AuthResult> = withContext(Dispatchers.IO) {
        return@withContext auth.signInWithEmailAndPassword(user.email, user.password)
    }

    suspend fun logoff() = withContext(Dispatchers.IO) {
        return@withContext auth.signOut()
    }

    suspend fun getUserEmail(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val email = currentUser()?.email ?: throw Exception("User not logged in")
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}