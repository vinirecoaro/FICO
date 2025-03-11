package com.example.fico.interfaces

import com.example.fico.model.User

interface UserDataInterface {
    suspend fun getUserEmail() : Result<String>
    suspend fun getUserName() : Result<String>
}