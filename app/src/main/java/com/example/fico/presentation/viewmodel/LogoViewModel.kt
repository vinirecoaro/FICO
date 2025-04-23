package com.example.fico.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.Earning
import com.example.fico.repositories.AuthRepository
import com.example.fico.repositories.TransactionsRepository
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LogoViewModel(
    private val authRepository: AuthRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _getBlockAppState = MutableLiveData<Boolean>()
    val getBlockAppState : LiveData<Boolean> = _getBlockAppState

    suspend fun isLogged() : Deferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
            withContext(Dispatchers.IO){
                try {
                    authRepository.isLogged().fold(
                        onSuccess = { isLogged ->
                            if(isLogged){
                                result.complete(true)
                            }else{
                                result.complete(false)
                            }
                        },
                        onFailure = {
                            onError(StringConstants.MESSAGES.IS_LOGGED_ERROR)
                            result.complete(false)
                        }
                    )
                }catch (e : Exception){
                    onError(StringConstants.MESSAGES.IS_LOGGED_ERROR)
                    result.complete(false)
                }
            }
        return result
    }

    var onError: (String) -> Unit = {}

    fun getBlockAppStateFromDataStore(){
        viewModelScope.async(Dispatchers.IO) {
            val state = dataStore.getBlockAppState()
            _getBlockAppState.postValue(state)
        }
    }

}