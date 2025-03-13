package com.example.fico.interfaces

import com.example.fico.model.User

interface AuthInterface {
    suspend fun register(user: User, password: String) : Result<User>

    suspend fun login(user: User, password: String) : Result<Boolean>

    suspend fun isLogged() : Result<Boolean>

    suspend fun sendVerificationEmail() : Result<Boolean>
}