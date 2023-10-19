package com.example.fico.ui.fragments

 import SwipeToDeleteCallback
 import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
 import androidx.recyclerview.widget.ItemTouchHelper
 import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.databinding.FragmentExpenseListBinding
import com.example.fico.model.Expense
import com.example.fico.ui.EditExpenseActivity
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.interfaces.OnListItemClick
import com.example.fico.ui.viewmodel.ExpenseListViewModel
 import kotlinx.coroutines.delay
 import kotlinx.coroutines.launch

class ExpenseListFragment : Fragment(){

    private var _binding : FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ExpenseListViewModel>()
    private val expenseListAdapter = ExpenseListAdapter(emptyList())
    private var expenseMonthsList = arrayOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpenseListBinding.inflate(inflater,container,false)
        val rootView = binding.root

        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenseList.adapter = expenseListAdapter

        val swipeToDeleteCallback = SwipeToDeleteCallback(binding.rvExpenseList,viewModel, expenseListAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvExpenseList)

        setUpListeners()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setUpListeners(){
        binding.actvDate.setOnClickListener {
            binding.actvDate.showDropDown()
        }
        binding.actvDate.setOnItemClickListener { parent, view, position, id ->
            val selectedOption = parent.getItemAtPosition(position).toString()
            viewModel.getExpenseList(selectedOption)
        }
        binding.ivClearFilter.setOnClickListener {
            binding.actvDate.setText("")
            viewModel.getExpenseList("")
        }
        lifecycleScope.launch {
            viewModel.expensesLiveData.observe(viewLifecycleOwner, Observer { expenses ->
                expenseListAdapter.updateExpenses(expenses)
                expenseListAdapter.setOnItemClickListener(object : OnListItemClick {
                    override fun onListItemClick(position: Int) {
                        val selectItem = expenses[position]
                        editExpense(selectItem)
                    }
                })
                /*viewModel.getExpenseList(binding.actvDate.text.toString())
                viewModel.getExpenseMonths()*/
            })

            viewModel.expenseMonthsLiveData.observe(viewLifecycleOwner, Observer { expenseMonths ->
                expenseMonthsList = expenseMonths.toTypedArray()
                actvConfig()
            })

        }

    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, expenseMonthsList)
        binding.actvDate.setAdapter(adapter)
    }
    fun editExpense(expense : Expense){
        val intent = Intent(requireContext(), EditExpenseActivity::class.java)
        intent.putExtra("expense", expense)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
            viewModel.getExpenseList(binding.actvDate.text.toString())
            viewModel.getExpenseMonths()
    }



}

