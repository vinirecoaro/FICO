package com.example.fico.repositories

import com.example.fico.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class UserRepository(private val firebaseAuthRepository: FirebaseAuthRepository) {

    suspend fun getUserEmail(): Result<String> {
        return firebaseAuthRepository.getUserEmail()
    }

    suspend fun login(user: User): Task<AuthResult> {
        return firebaseAuthRepository.login(user)
    }

    suspend fun logout() {
        firebaseAuthRepository.logoff()
    }

}