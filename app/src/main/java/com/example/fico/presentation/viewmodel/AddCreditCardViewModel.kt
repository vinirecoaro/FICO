package com.example.fico.presentation.viewmodel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.ColorOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class AddCreditCardViewModel(
) : ViewModel() {

    val colorOptions = listOf(
        ColorOption("Vermelho", Color.RED),
        ColorOption("Verde", Color.GREEN),
        ColorOption("Azul", Color.BLUE),
        ColorOption("Amarelo", Color.YELLOW),
        ColorOption("Magenta", Color.MAGENTA)
    )


}