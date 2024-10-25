package com.example.fico.presentation.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetPerMonthBinding
import com.example.fico.model.Budget
import com.example.fico.presentation.adapters.BudgetPerMonthAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.BudgetPerMonthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.DecimalFormat
import java.text.NumberFormat

class BudgetPerMonthActivity : AppCompatActivity() {

    private val binding by lazy { ActivityBudgetPerMonthBinding.inflate(layoutInflater) }
    private val viewModel : BudgetPerMonthViewModel by inject()
    private val budgetPerMonthListAdapter = BudgetPerMonthAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.budgetPerMonthToolbar.title = getString(R.string.budget_per_month_activity_title)
        binding.budgetPerMonthToolbar.setTitleTextColor(Color.WHITE)

        binding.rvBudgetPerMonth.layoutManager = LinearLayoutManager(this)
        binding.rvBudgetPerMonth.adapter = budgetPerMonthListAdapter

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.budgetPerMonthToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.getBudgetPerMonth()
        setUpListeners()
    }

    private fun setUpListeners(){
        lifecycleScope.launch {

            viewModel.budgetPerMonthList.observe(this@BudgetPerMonthActivity, Observer {budgetList ->
                budgetPerMonthListAdapter.updateList(budgetList)
                budgetPerMonthListAdapter.notifyDataSetChanged()
                budgetPerMonthListAdapter.setOnItemClickListener(object : OnListItemClick {
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onListItemClick(position: Int) {
                        val selectItem = budgetList[position]
                        editBudget(selectItem)
                    }
                })
            })
        }

        binding.budgetPerMonthToolbar.setNavigationOnClickListener {
            finish()
        }
        viewModel.editBudgetResult.observe(this){ result ->
            if(result){
                viewModel.getBudgetPerMonth()
                Snackbar.make(binding.rvBudgetPerMonth, getString(R.string.redefine_month_budget_success_message), Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(binding.rvBudgetPerMonth, getString(R.string.redefine_month_budget_failure_message), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun editBudget(budget : Budget) : CompletableDeferred<Boolean>{
        val result = CompletableDeferred<Boolean>()
        val builder = MaterialAlertDialogBuilder(this)

        builder.setTitle(getString(R.string.edit_budget))

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.month_budget_input_field_for_alert_dialog, null)

        val newBudget = dialogView.findViewById<TextInputEditText>(R.id.et_month_budget_ad)
        builder.setView(dialogView)

        newBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                    newBudget.removeTextChangedListener(this)
                    newBudget.setText(formatted)
                    newBudget.setSelection(formatted.length)
                    newBudget.addTextChangedListener(this)
                }
            }
        })

        builder.setPositiveButton(getString(R.string.save)) { dialog, which ->
            val saveButton =  (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            lifecycleScope.launch {
                if(newBudget.text.toString() != ""){

                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(newBudget.text.toString())
                    val formatNum = DecimalFormat("#.##")
                    val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                    val formatedNum = formatNum.format(numClean/100)
                    val formattedNumString = formatedNum.toString().replace(",",".")

                    viewModel.editBudget(formattedNumString, budget)

                }
            }
            saveButton.isEnabled = true
        }

        builder.setNegativeButton(R.string.cancel) { dialog, which ->

        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
        return result
    }

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = this.theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(this, typedValue.resourceId)
        return colorOnSurfaceVariant
    }

}