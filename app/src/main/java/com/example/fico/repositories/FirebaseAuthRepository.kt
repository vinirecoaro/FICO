package com.example.fico.repositories

import com.example.fico.interfaces.AuthInterface
import com.example.fico.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepository(private val auth: FirebaseAuth) : AuthInterface {

    override suspend fun register(user: User, password: String): Result<User> {
        return try{
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val newUser = User(user.name, user.email, firebaseUser.uid)
                Result.success(newUser)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}