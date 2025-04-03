package com.example.fico.components

import android.app.AlertDialog
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Dialogs {
    companion object {

        //Title, subtitle, one button
        fun dialogModelOne(
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
            return dialog
        }
    }
}