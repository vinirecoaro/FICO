package com.example.fico.utils

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

class UiFunctions {
    companion object{
        fun hideKeyboard(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun clearEditText(vararg textInputs: EditText){
            for (textInput in textInputs) {
                textInput.text?.clear()
            }
        }

        fun setImageBasedOnTheme(context: Context, forDarkMode : Int, forLightMode : Int) : Int{
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    return forDarkMode
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    return forLightMode
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }
            return forLightMode
        }
    }
}