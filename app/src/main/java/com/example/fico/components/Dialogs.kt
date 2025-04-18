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
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.Transaction
import com.example.fico.model.TransactionCategory
import com.example.fico.presentation.adapters.TransactionListAdapter
import com.example.fico.utils.constants.CategoriesList
import com.example.fico.utils.constants.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.android.ext.android.get
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

        //Title, textField (with formater on typing, for money input), one => function (String) -> Unit
        fun dialogModelTwo(
            activity : Activity,
            context : Context,
            viewFromActivity : View,
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

            val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.til_dialog_input_field)
            textInputLayout.hint = inputFieldHint

            val textInputEditText = dialogView.findViewById<TextInputEditText>(R.id.tiet_dialog_input_field)
            if(dataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                textInputEditText.inputType = InputType.TYPE_CLASS_NUMBER
            }else{
                textInputEditText.inputType = dataType
            }

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

                if(verifyFields(activity, viewFromActivity, textInputEditText)){
                    val newBudgetString = textInputEditText.text.toString()

                    function(newBudgetString)
                }

                saveButton.isEnabled = true
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE)
                    .setTextColor(getAlertDialogTextButtonColor(activity, context))
            }

            return dialog
        }

        //Title, subtitle, two buttons, two functions
        fun dialogModelThree(
            activity : Activity,
            context : Context,
            title : String,
            subtitle : String,
            rightButtonText : String,
            rightButtonFunction : () -> Unit,
            leftButtonText : String,
            leftButtonFunction : () -> Unit
        ) : androidx.appcompat.app.AlertDialog {

            val builder = MaterialAlertDialogBuilder(context)

            builder.setTitle(title)
            builder.setMessage(subtitle)

            builder.setPositiveButton(rightButtonText) { _,_ ->
                rightButtonFunction()
            }

            builder.setNegativeButton(leftButtonText) { _,_ ->
                leftButtonFunction()
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE)
                    .setTextColor(getAlertDialogTextButtonColor(activity, context))
                dialog.getButton(Dialog.BUTTON_NEGATIVE)
                    .setTextColor(getAlertDialogTextButtonColor(activity, context))
            }

            return dialog
        }

        //Title, 2 textField (without formater on typing), 2 textView, one => function (String, String) -> Unit
        fun dialogModelFour(
            activity : Activity,
            context : Context,
            viewFromActivity : View,
            title : String,
            inputFieldOneHint : String,
            inputFieldOneDataType : Int,
            inputFieldTwoHint : String,
            inputFieldTwoDataType : Int,
            textViewOneText : String,
            textViewTwoText : String,
            buttonText : String,
            function : (String, String) -> Unit
        ) : androidx.appcompat.app.AlertDialog {

            val builder = MaterialAlertDialogBuilder(context)

            builder.setTitle(title)

            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_2_input_text_2_text_view, null)

            //Config textInputLayouts and textInputFields

            val textInputLayoutOne = dialogView.findViewById<TextInputLayout>(R.id.til_1_d2it_2tv)
            textInputLayoutOne.hint = inputFieldOneHint

            val textInputEditTextOne = dialogView.findViewById<TextInputEditText>(R.id.et_1_d2it_2tv)
            if(inputFieldOneDataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                textInputEditTextOne.inputType = InputType.TYPE_CLASS_NUMBER
            }else{
                textInputEditTextOne.inputType = inputFieldOneDataType
            }

            textInputEditTextOne.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    if(inputFieldOneDataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                        if (text.isNotEmpty()) {
                            val parsed = text.replace("\\D".toRegex(), "").toLong()
                            val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                            textInputEditTextOne.removeTextChangedListener(this)
                            textInputEditTextOne.setText(formatted)
                            textInputEditTextOne.setSelection(formatted.length)
                            textInputEditTextOne.addTextChangedListener(this)
                        }
                    }
                }
            })

            val textInputLayoutTwo = dialogView.findViewById<TextInputLayout>(R.id.til_2_d2it_2tv)
            textInputLayoutTwo.hint = inputFieldTwoHint

            val textInputEditTextTwo = dialogView.findViewById<TextInputEditText>(R.id.et_2_d2it_2tv)
            if(inputFieldTwoDataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                textInputEditTextTwo.inputType = InputType.TYPE_CLASS_NUMBER
            }else{
                textInputEditTextTwo.inputType = inputFieldTwoDataType
            }

            textInputEditTextTwo.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()
                    if(inputFieldTwoDataType == StringConstants.PERSONALIZED_INPUT_TYPE.MONEY){
                        if (text.isNotEmpty()) {
                            val parsed = text.replace("\\D".toRegex(), "").toLong()
                            val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                            textInputEditTextTwo.removeTextChangedListener(this)
                            textInputEditTextTwo.setText(formatted)
                            textInputEditTextTwo.setSelection(formatted.length)
                            textInputEditTextTwo.addTextChangedListener(this)
                        }
                    }
                }
            })


            //Config textViews

            val textViewOne = dialogView.findViewById<TextView>(R.id.tv_1_d2it_2tv)
            textViewOne.text = textViewOneText

            val textViewTwo = dialogView.findViewById<TextView>(R.id.tv_2_d2it_2tv)
            textViewTwo.text = textViewTwoText

            builder.setView(dialogView)

            builder.setPositiveButton(buttonText) { dialog,_ ->
                val saveButton =  (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                saveButton.isEnabled = false

                if(verifyFields(activity, viewFromActivity, textInputEditTextOne, textInputEditTextTwo)){
                    val inputFieldOneValue = textInputEditTextOne.text.toString()
                    val inputFieldTwoValue = textInputEditTextTwo.text.toString()

                    function(inputFieldOneValue, inputFieldTwoValue)
                }

                saveButton.isEnabled = true
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE)
                    .setTextColor(getAlertDialogTextButtonColor(activity, context))
            }

            return dialog
        }

        //Recurring transaction list, title, recyclerView, function : (Object)
        fun dialogTransactionList(
            activity : Activity,
            context : Context,
            transactionType: String,
            recurringTransactionList : List<Transaction>,
            categoryList: CategoriesList,
            onRecurringExpenseSelectedFunction : (RecurringTransaction) -> Unit,
            onRecurringEarningSelectedFunction : (RecurringTransaction) -> Unit
        ): androidx.appcompat.app.AlertDialog{
            val builder = MaterialAlertDialogBuilder(context)

            if(transactionType ==  StringConstants.DATABASE.RECURRING_EXPENSE){
                builder.setTitle(activity.getString(R.string.dialog_recurring_expense_list_title))
            } else if(transactionType ==  StringConstants.DATABASE.RECURRING_EARNING){
                builder.setTitle(activity.getString(R.string.dialog_recurring_earning_list_title))
            }

            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_recurring_expenses, null)

            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_recurring_expenses_list)

            // Recycler View configuration
            recyclerView.layoutManager = LinearLayoutManager(context)
            val recurringTransactionListAdapter = TransactionListAdapter(categoryList.getExpenseCategoryListFull(), categoryList.getEarningCategoryList())
            recurringTransactionListAdapter.updateTransactions(recurringTransactionList)
            recyclerView.adapter = recurringTransactionListAdapter

            builder.setView(dialogView)

            val dialog = builder.create()

            // Listeners
            recurringTransactionListAdapter.setOnItemClickListener { position ->
                val selectItem = recurringTransactionList[position].toRecurringTransaction()
                if(transactionType ==  StringConstants.DATABASE.RECURRING_EXPENSE){
                    onRecurringExpenseSelectedFunction(selectItem)
                } else if(transactionType ==  StringConstants.DATABASE.RECURRING_EARNING){
                    onRecurringEarningSelectedFunction(selectItem)
                }
                dialog.cancel()
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

        private fun verifyFields(activity : Activity, viewFromActivity : View, vararg fields: EditText): Boolean {
            for (field in fields) {
                if (field.text.toString() == "") {
                    PersonalizedSnackBars.fillField(activity, viewFromActivity, field.hint.toString())
                    return false
                }
            }
            return true
        }

    }
}