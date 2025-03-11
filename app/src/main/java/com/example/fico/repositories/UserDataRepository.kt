package com.example.fico.repositories

import com.example.fico.interfaces.UserDataInterface

class UserDataRepository(private val userDataInterface: UserDataInterface) {
    suspend fun getUserEmail(): Result<String> {
        return userDataInterface.getUserEmail()
    }
    suspend fun getUserName(): Result<String> {
        return userDataInterface.getUserName()
    }
}