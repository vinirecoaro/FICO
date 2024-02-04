package com.example.fico.ui.fragments.expense.expense_home

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.ui.viewmodel.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

            binding.tvTotalExpensesValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.tvTotalExpensesValue.transformationMethod = PasswordTransformationMethod()
            binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.ic_visibility_off_24, 0)
            setUpListeners()

        return rootView
    }

    private fun setUpListeners(){
        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.ShowHideValue(binding.tvTotalExpensesValue)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        getTotalExpense()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getTotalExpense(){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val totalExpense = viewModel.getTotalExpense().await()
                binding.tvTotalExpensesValue.text = totalExpense
            }catch (exception:Exception){
            } }
    }

}