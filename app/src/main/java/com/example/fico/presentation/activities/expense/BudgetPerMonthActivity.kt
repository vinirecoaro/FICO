package com.example.fico.presentation.activities.expense

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetPerMonthBinding
import com.example.fico.model.Budget
import com.example.fico.presentation.adapters.BudgetPerMonthAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.BudgetPerMonthViewModel
import com.google.android.material.snackbar.Snackbar
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
        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.edit_budget))

        val newBudget = EditText(this)
        newBudget.hint = getString(R.string.type_new_budget)
        newBudget.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(newBudget)

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
            val saveButton =  (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
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

        val alertDialog = builder.create()
        alertDialog.show()
        return result
    }

}