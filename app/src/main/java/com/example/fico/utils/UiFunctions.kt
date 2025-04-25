package com.example.fico.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.fico.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UiFunctions {
    companion object{
        fun hideKeyboard(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun defineStrokeColorOnFocused(
            activity : Activity,
            context : Context,
            vararg editTexts : Pair<TextInputLayout, TextInputEditText>)
        {
            for(editText in editTexts){
                val hintColorStateList = getHintTextColorStateList(activity, context)
                editText.first.defaultHintTextColor = hintColorStateList
                editText.second.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        val onFocusedBoxStrokeColor = getOnFocusedBoxStrokeColor(activity, context)
                        editText.first.boxStrokeColor = onFocusedBoxStrokeColor
                    }
                }
            }
        }

        private fun getHintTextColorStateList(activity: Activity, context: Context): ColorStateList {
            val focusedColor = getOnFocusedBoxStrokeColor(activity, context)
            val defaultColor = getColorOnSurfaceVariant(activity, context)

            return ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_focused), // Estado focado
                    intArrayOf()                              // Qualquer outro estado
                ),
                intArrayOf(
                    focusedColor, // Cor quando focado
                    defaultColor  // Cor padrão
                )
            )
        }


        private fun getOnFocusedBoxStrokeColor(
            activity : Activity,
            context : Context
        ) : Int{
            val typedValue = TypedValue()
            val theme: Resources.Theme = activity.theme
            theme.resolveAttribute(R.attr.boxStrokeColor, typedValue, true)
            val boxStrokeColor = ContextCompat.getColor(context, typedValue.resourceId)
            return boxStrokeColor
        }

        private fun getColorOnSurfaceVariant(
            activity : Activity,
            context : Context
        ) : Int{
            val typedValue = TypedValue()
            val theme: Resources.Theme = activity.theme
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
            val colorOnSurfaceVariant = ContextCompat.getColor(context, typedValue.resourceId)
            return colorOnSurfaceVariant
        }
    }
}