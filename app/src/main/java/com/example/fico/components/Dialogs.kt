package com.example.fico.components

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.example.fico.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat

class Dialogs {
    companion object {

        //Title, subtitle, one button, one function
        fun dialogModelOne(
            activity : Activity,
            context : Context,
            title : String,
            subtitle : String,
            buttonText : String,
            function : () -> Unit
        ) : androidx.appcompat.app.AlertDialog {

            val builder = MaterialAlertDialogBuilder(context)

            builder.setTitle(title)
            builder.setMessage(subtitle)
            builder.setPositiveButton(buttonText) { _,_ ->
                function()
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE)
                    .setTextColor(getAlertDialogTextButtonColor(activity, context))
            }

            return dialog
        }

        //Title, layout, textField for currency, function (String) -> Unit
        fun dialogModelTwo(
            activity : Activity,
            context : Context,
            title : String,
            layoutId : Int,
            textInputId : Int,
            buttonText : String,
            function : (String) -> Unit
        ) : androidx.appcompat.app.AlertDialog {

            val builder = MaterialAlertDialogBuilder(context)

            builder.setTitle(title)

            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(layoutId, null)
            val newBudget = dialogView.findViewById<TextInputEditText>(textInputId)
            builder.setView(dialogView)

            newBudget.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    if (text.isNotEmpty()) {
                        val parsed = text.replace("\\D".toRegex(), "").toLong()
                        val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                        newBudget.removeTextChangedListener(this)
                        newBudget.setText(formatted)
                        newBudget.setSelection(formatted.length)
                        newBudget.addTextChangedListener(this)
                    }
                }
            })

            builder.setPositiveButton(buttonText) { dialog,_ ->
                val saveButton =  (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.isEnabled = false

                val newBudgetString = newBudget.text.toString()

                function(newBudgetString)

                saveButton.isEnabled = true
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE)
                    .setTextColor(getAlertDialogTextButtonColor(activity, context))
            }

            return dialog
        }

        private fun getAlertDialogTextButtonColor(
            activity : Activity,
            context : Context
        ) : Int{
            val typedValue = TypedValue()
            val theme: Resources.Theme = activity.theme
            theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
            val colorOnSurfaceVariant = ContextCompat.getColor(context, typedValue.resourceId)
            return colorOnSurfaceVariant
        }
    }
}