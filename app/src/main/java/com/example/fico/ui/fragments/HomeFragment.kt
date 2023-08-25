package com.example.fico.ui.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        val rootView = binding.root
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
        getAvailableNow()
        getTotalExpense()
        getMonthExpense()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAvailableNow(){
        viewModel.getAvailableNow(viewModel.getCurrentlyDate()).thenAccept{availableNowText ->
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                if(availableNowText.substring(2,7).replace(",",".").toFloat() < 0){
                    val myColor = ContextCompat.getColor(requireContext(), R.color.red)
                    binding.tvAvailableThisMonthValue.setTextColor(myColor)
                    binding.tvAvailableThisMonthValue.text = availableNowText
                }else {
                    binding.tvAvailableThisMonthValue.text = availableNowText
                }
            }
        }.exceptionally { throwable ->
            return@exceptionally null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getTotalExpense(){
        viewModel.getTotalExpense().thenAccept { totalExpense ->
            val handler = Handler(Looper.getMainLooper())
            handler.post{
                binding.tvTotalExpensesValue.text = totalExpense
            }
        }.exceptionally { throwable ->
            return@exceptionally null }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMonthExpense() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val monthExpense = viewModel.getMonthExpense(viewModel.getCurrentlyDate()).await()
                binding.tvTotalExpensesThisMonthValue.text = monthExpense
            } catch (exception: Exception) {
            }
        }
    }

}