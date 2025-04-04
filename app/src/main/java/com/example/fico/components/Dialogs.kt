package com.example.fico.components

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.example.fico.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Dialogs {
    companion object {

        //Title, subtitle, one button
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

        private fun getAlertDialogTextButtonColor(activity : Activity, context : Context) : Int{
            val typedValue = TypedValue()
            val theme: Resources.Theme = activity.theme
            theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
            val colorOnSurfaceVariant = ContextCompat.getColor(context, typedValue.resourceId)
            return colorOnSurfaceVariant
        }
    }
}