package com.example.fico.repositories

import com.example.fico.interfaces.AuthInterface
import com.example.fico.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class AuthRepository(private val authInterface: AuthInterface) {
    suspend fun register(user: User, password: String): Result<User> {
        return authInterface.register(user, password)
    }

    suspend fun login(user: User, password: String): Result<Boolean> {
        return authInterface.login(user, password)
    }

}