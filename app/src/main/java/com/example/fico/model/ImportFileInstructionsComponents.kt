package com.example.fico.model

import androidx.annotation.DrawableRes

data class ImportFileInstructionsComponents(
    val title : String,
    @DrawableRes val drawableres : Int,
    val description : String,
    val buttonState : Boolean
)
