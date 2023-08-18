package com.example.fico.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import java.text.DecimalFormat

class AddExpenseFragment : Fragment(), OnButtonClickListener {

    private var _binding : FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")
    private val viewModel by viewModels<AddExpenseViewModel>()

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
            val day = binding.etDate.text.toString().substring(0, 2)
            val month = binding.etDate.text.toString().substring(3, 5)
            val year = binding.etDate.text.toString().substring(6, 10)
            val modifiedDate = "$year-$month-$day"
            val checkDate = "$year-$month"

            val formatNum = DecimalFormat("#.##")
            val formatedNum = formatNum.format(binding.etPrice.text.toString().toFloat())

            viewModel.checkIfExistsDateOnDatabse(checkDate).thenAccept { exists ->
                if (exists) {
                    viewModel.addExpense(
                        formatedNum.toString(),
                        binding.etDescription.text.toString(),
                        binding.actvCategory.text.toString(),
                        modifiedDate
                    )
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

    override fun onSaveButtonFragmentClick() {
        binding.btSave.performClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
