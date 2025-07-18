package com.example.fico.presentation.viewmodel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.R
import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.repositories.CreditCardRepository
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreditCardViewModel(
    private val dataStore : DataStoreManager,
    private val creditCardRepository : CreditCardRepository,
) : ViewModel() {

    private val _addCreditCardResult = MutableLiveData<Boolean>()
    val addCreditCardResult : LiveData<Boolean> = _addCreditCardResult
    private val _editCreditCardResult = MutableLiveData<Boolean>()
    val editCreditCardResult : LiveData<Boolean> = _editCreditCardResult
    private val _deleteCreditCardResult = MutableLiveData<Boolean>()
    val deleteCreditCardResult : LiveData<Boolean> = _deleteCreditCardResult
    private val _setCreditCardAsDefaultResult = MutableLiveData<Boolean>()
    val setCreditCardAsDefaultResult : LiveData<Boolean> = _setCreditCardAsDefaultResult
    private val _creditCardColors = MutableLiveData<CreditCardColors>()
    private var activityMode = StringConstants.GENERAL.ADD_MODE
    private val editingCreditCard = MutableLiveData<CreditCard>()
    private val defaultCreditCardId = MutableLiveData<String>()

    fun setActivityMode(mode : String){
        activityMode = mode
    }

    fun getActivityMode() : String {
        return activityMode
    }

    fun setEditingCreditCard(creditCard : CreditCard){
        editingCreditCard.value = creditCard
    }

    fun getEditingCreditCard() : CreditCard{
        return editingCreditCard.value!!
    }

    fun setDefaultCreditCardId(creditCardId : String){
        defaultCreditCardId.value = creditCardId
    }

    fun getDefaultCreditCardId() : String{
        return defaultCreditCardId.value!!
    }

    fun getCreditCardColorOptions() : List<CreditCardColors>{
        return CreditCardColors.entries
    }

    fun setCreditCardColors(creditCardColors : CreditCardColors){
        _creditCardColors.value = creditCardColors
    }

    fun getCreditCardColors() : CreditCardColors{
        return _creditCardColors.value ?: CreditCardColors.WHITE
    }

    suspend fun addCreditCard(
        cardNickName : String,
        expirationDay : Int,
        closingDay : Int,
        creditCardColors : CreditCardColors
    ){
        viewModelScope.launch(Dispatchers.IO){
            val creditCard = CreditCard(
                nickName = cardNickName,
                expirationDay = expirationDay,
                closingDay = closingDay,
                colors = creditCardColors
            )

            creditCardRepository.addCreditCard(creditCard).fold(
                onSuccess = {

                    dataStore.updateCreditCardList(creditCard)

                    _addCreditCardResult.postValue(true)
                },
                onFailure = {
                    _addCreditCardResult.postValue(false)
                }
            )
        }
    }

    suspend fun editCreditCard(
        id : String,
        cardNickName : String,
        expirationDay : Int,
        closingDay : Int,
        creditCardColors : CreditCardColors
    ){
        viewModelScope.launch(Dispatchers.IO){
            val creditCard = CreditCard(
                id = id,
                nickName = cardNickName,
                expirationDay = expirationDay,
                closingDay = closingDay,
                colors = creditCardColors
            )

            creditCardRepository.editCreditCard(creditCard).fold(
                onSuccess = {

                    dataStore.updateCreditCardList(creditCard)

                    _editCreditCardResult.postValue(true)
                },
                onFailure = {
                    _editCreditCardResult.postValue(false)
                }
            )
        }
    }

    suspend fun deleteCreditCard(creditCard : CreditCard){
        viewModelScope.launch(Dispatchers.IO){
            creditCardRepository.deleteCreditCard(creditCard).fold(
                onSuccess = {
                    if(creditCard.id == defaultCreditCardId.value){
                        setCreditCardAsDefault("")
                    }
                    dataStore.deleteFromCreditCardList(creditCard)
                    _deleteCreditCardResult.postValue(true)
                },
                onFailure = {
                    _deleteCreditCardResult.postValue(false)
                }
            )
        }
    }

    suspend fun setCreditCardAsDefault(creditCardId : String){
        viewModelScope.launch(Dispatchers.IO){
            creditCardRepository.setCreditCardAsDefault(creditCardId).fold(
                onSuccess = {
                    dataStore.setDefaultCreditCardId(creditCardId)
                    _setCreditCardAsDefaultResult.postValue(true)
                },
                onFailure = {
                    _setCreditCardAsDefaultResult.postValue(false)
                }
            )
        }
    }
}