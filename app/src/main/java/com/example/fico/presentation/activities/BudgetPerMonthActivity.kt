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
import com.example.fico.components.Dialogs
import com.example.fico.components.PersonalizedSnackBars
import com.example.fico.databinding.ActivityBudgetPerMonthBinding
import com.example.fico.model.Budget
import com.example.fico.presentation.adapters.BudgetPerMonthAdapter
import com.example.fico.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.BudgetPerMonthViewModel
import com.example.fico.utils.internet.ConnectionFunctions
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
                        editBudgetDialog(selectItem)
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
    private fun editBudgetDialog(budget : Budget){
        val dialog = Dialogs.dialogModelTwo(
            this,
            this,
            getString(R.string.edit_budget),
            R.layout.month_budget_input_field_for_alert_dialog,
            R.id.et_month_budget_ad,
            getString(R.string.save)
        ) { newBudgetString -> editBudget(budget, newBudgetString) }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun editBudget(budget : Budget, newBudget : String){
        if(hasInternetConnection()){
            lifecycleScope.launch {
                if(newBudget != ""){

                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(newBudget)
                    val formatNum = DecimalFormat("#.##")
                    val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                    val formatedNum = formatNum.format(numClean/100)
                    val formattedNumString = formatedNum.toString().replace(",",".")

                    viewModel.editBudget(formattedNumString, budget)

                }
            }
        }else{
            PersonalizedSnackBars.noInternetConnection(binding.rvBudgetPerMonth, this).show()
        }
    }
    
    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions().internetConnectionVerification(this)
    }

}