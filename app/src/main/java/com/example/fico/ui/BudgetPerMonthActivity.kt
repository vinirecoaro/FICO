package com.example.fico.ui

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetPerMonthBinding
import com.example.fico.model.Budget
import com.example.fico.ui.adapters.BudgetPerMonthAdapter
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.interfaces.OnListItemClick
import com.example.fico.ui.viewmodel.BudgetPerMonthViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class BudgetPerMonthActivity : AppCompatActivity() {

    private val bindind by lazy { ActivityBudgetPerMonthBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<BudgetPerMonthViewModel>()
    private val budgetPerMonthListAdapter = BudgetPerMonthAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bindind.root)
        setTitle("Budget por mês")

        bindind.rvBudgetPerMonth.layoutManager = LinearLayoutManager(this)
        bindind.rvBudgetPerMonth.adapter = budgetPerMonthListAdapter

        setUpListeners()
    }

    private fun setUpListeners(){
        lifecycleScope.launch {
            viewModel.getBudgetPerMonth()

            viewModel.budgetPerMonthList.observe(this@BudgetPerMonthActivity, Observer {budgetList ->
                budgetPerMonthListAdapter.updateList(budgetList)
                budgetPerMonthListAdapter.notifyDataSetChanged()
                budgetPerMonthListAdapter.setOnItemClickListener(object : OnListItemClick {
                    override fun onListItemClick(position: Int) {
                        val selectItem = budgetList[position]
                        editBudget(selectItem)
                    }
                })
            })
        }
    }

    private fun editBudget(budget : Budget) : CompletableDeferred<Boolean>{
        val result = CompletableDeferred<Boolean>()
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Editar Budget")

        val newBudget = EditText(this)
        newBudget.hint = "Digite o novo Budget"
        builder.setView(newBudget)

        builder.setPositiveButton("Salvar") { dialog, which ->
            lifecycleScope.launch {
                if(newBudget.text.toString() != ""){
                    if(viewModel.editBudget(newBudget.text.toString(), budget).await()){
                        val rootView: View? = findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Default Budget definido com sucesso", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            result.complete(true)
                        }
                    }else{
                        val rootView: View? = findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Falha ao definir o Default Budget", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            result.complete(false)
                        }
                    }
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
        return result
    }

}