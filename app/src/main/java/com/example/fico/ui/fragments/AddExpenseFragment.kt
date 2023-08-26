package com.example.fico.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseSetBudgetSharedViewModel
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class AddExpenseFragment : Fragment(), OnButtonClickListener{

    private var _binding : FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")
    private val viewModel by viewModels<AddExpenseViewModel>()
    private val sharedViewModel: AddExpenseSetBudgetSharedViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater,container,false)
        val rootView = binding.root
        setUpListeners()
        actvConfig()
        binding.etDate.setText(viewModel.getCurrentlyDate())
        binding.etDate.inputType = InputType.TYPE_NULL
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        binding.btSave.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                    val day = binding.etDate.text.toString().substring(0, 2)
                    val month = binding.etDate.text.toString().substring(3, 5)
                    val year = binding.etDate.text.toString().substring(6, 10)
                    val modifiedDate = "$year-$month-$day"
                    val checkDate = "$year-$month"

                    val formatNum = DecimalFormat("#.##")
                    val formatedNum = formatNum.format(binding.etPrice.text.toString().toFloat())
                    val existsDate = viewModel.checkIfExistsDateOnDatabse(checkDate).await()
                    if (existsDate) {
                        viewModel.addExpense(
                            formatedNum.toString(),
                            binding.etDescription.text.toString(),
                            binding.actvCategory.text.toString(),
                            modifiedDate
                        )
                        binding.etPrice.setText("")
                        binding.etDescription.setText("")
                        binding.actvCategory.setText("")
                    } else {
                        binding.btSave.visibility = View.GONE
                        binding.dpDateExpense.visibility = View.GONE
                        binding.fragSetBudget.visibility = View.VISIBLE
                        val setMonthBudget = SetMonthBudget.newInstance(
                            formatedNum.toString(),
                            binding.etDescription.text.toString(),
                            binding.actvCategory.text.toString(),
                            modifiedDate
                        )

                        childFragmentManager.beginTransaction()
                            .replace(binding.fragSetBudget.id, setMonthBudget)
                            .commit()

                    }
            }

            }
        }

        sharedViewModel.price.observe(viewLifecycleOwner) { price ->
            binding.btSave.visibility = View.VISIBLE
            binding.etPrice.setText(price)
        }

        sharedViewModel.description.observe(viewLifecycleOwner) { desc ->
            binding.etDescription.setText(desc)
        }

        sharedViewModel.category.observe(viewLifecycleOwner) { cat ->
            binding.actvCategory.setText(cat)
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
        }

        binding.etDate.setOnClickListener {
            binding.fragSetBudget.visibility = View.GONE
            binding.btSave.visibility = View.VISIBLE
            binding.dpDateExpense.visibility = View.VISIBLE
            binding.dpDateExpense.setOnDateChangedListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDate.setText(selectedDate)
            }
        }

        binding.etDate.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }
    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryOptions)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun verifyFields(vararg text: EditText) : Boolean{
        for (i in text){
            if (i.text.toString() == "" || i == null){
                Snackbar.make(binding.btSave,"Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    override fun onSaveButtonFragmentClick() {
        binding.btSave.performClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
