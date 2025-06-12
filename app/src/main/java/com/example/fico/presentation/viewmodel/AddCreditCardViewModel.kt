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
import kotlinx.coroutines.withContext

class AddCreditCardViewModel(
    private val dataStore : DataStoreManager,
    private val creditCardRepository : CreditCardRepository,
) : ViewModel() {

    private val _addCreditCardResult = MutableLiveData<Boolean>()
    val addCreditCardResult : LiveData<Boolean> = _addCreditCardResult
    private val _editCreditCardResult = MutableLiveData<Boolean>()
    val editCreditCardResult : LiveData<Boolean> = _editCreditCardResult
    private val _creditCardColors = MutableLiveData<CreditCardColors>()
    private var activityMode = StringConstants.GENERAL.ADD_MODE
    private var editingEventId = ""

    fun setActivityMode(mode : String){
        activityMode = mode
    }

    fun getActivityMode() : String {
        return activityMode
    }

    fun setEditingEventId(id : String){
        editingEventId = id
    }

    fun getEditingEventId() : String{
        return editingEventId
    }

    fun getCreditCardColorOptions() : List<CreditCardColors>{
        val colorOptions = listOf(
            CreditCardColors(R.string.dark_red, Color.rgb(100, 0, 0), Color.WHITE),
            CreditCardColors(R.string.dark_green, Color.rgb(0, 100, 0), Color.WHITE),
            CreditCardColors(R.string.dark_blue, Color.rgb(0, 0, 100), Color.WHITE),
            CreditCardColors(R.string.dark_yellow, Color.rgb(255, 193, 7), Color.BLACK),
            CreditCardColors(R.string.magenta, Color.MAGENTA, Color.WHITE),
            CreditCardColors(R.string.purple, Color.rgb(103, 58, 183), Color.WHITE),
            CreditCardColors(R.string.black, Color.BLACK, Color.WHITE),
            CreditCardColors(R.string.white, Color.WHITE, Color.BLACK),
            CreditCardColors(R.string.orange, Color.rgb(255, 152, 0), Color.WHITE),
            CreditCardColors(R.string.light_pink, Color.rgb(255, 167, 197), Color.BLACK),
            CreditCardColors(R.string.light_blue, Color.rgb(123, 194, 250), Color.BLACK)
        )

        return colorOptions
    }

    fun setCreditCardColors(backgroundColorNameRes: Int, backgroundColor: Int, textColor: Int){
        _creditCardColors.value = CreditCardColors(backgroundColorNameRes, backgroundColor, textColor)
    }

    fun getCreditCardColors() : CreditCardColors{
        return _creditCardColors.value ?: CreditCardColors(R.string.white, Color.WHITE, Color.BLACK)
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
}