package com.example.fico.components

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.example.fico.R
import com.example.fico.utils.constants.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

        //Title, textField, one => function (String) -> Unit
        fun dialogModelTwo(
            activity : Activity,
            context : Context,
            title : String,
            inputFieldHint : String,
            dataType : Int,
            buttonText : String,
            function : (String) -> Unit
        ) : androidx.appcompat.app.AlertDialog {

            val builder = MaterialAlertDialogBuilder(context)

            builder.setTitle(title)

            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_input_field, null)

            val textInputEditText = dialogView.findViewById<TextInputEditText>(R.id.tiet_dialog_input_field)
            if(dataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                textInputEditText.inputType = InputType.TYPE_CLASS_NUMBER
            }else{
                textInputEditText.inputType = dataType
            }

            val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.til_dialog_input_field)
            textInputLayout.hint = inputFieldHint

            builder.setView(dialogView)

            textInputEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    if(dataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                        if (text.isNotEmpty()) {
                            val parsed = text.replace("\\D".toRegex(), "").toLong()
                            val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                            textInputEditText.removeTextChangedListener(this)
                            textInputEditText.setText(formatted)
                            textInputEditText.setSelection(formatted.length)
                            textInputEditText.addTextChangedListener(this)
                        }
                    }
                }
            })

            builder.setPositiveButton(buttonText) { dialog,_ ->
                val saveButton =  (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.isEnabled = false

                val newBudgetString = textInputEditText.text.toString()

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