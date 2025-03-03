package com.example.fico.interfaces

import com.example.fico.model.User

interface AuthInterface {
    suspend fun register(user: User, password: String) : Result<User>
}