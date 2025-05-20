package com.example.fico.presentation.fragments.home.expenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fico.databinding.FragmentHomeExpensesBinding

class HomeExpensesFragment : Fragment() {

    private var _binding : FragmentHomeExpensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeExpensesBinding.inflate(inflater, container, false)
        val rootView = binding.root

        replaceChildFragment(HomeMonthExpensesFragment())

        setupListeners()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        binding.btgExpensesDataRange.check(binding.btMonthlyExpenses.id)
        binding.btMonthlyExpenses.isClickable = false
    }

    private fun setupListeners(){
        binding.btgExpensesDataRange.addOnButtonCheckedListener{ group, checkedId, isChecked ->
            if(isChecked){
                when(checkedId){
                    binding.btMonthlyExpenses.id -> {
                        binding.btMonthlyExpenses.isClickable = false
                        binding.btGeneralExpenses.isClickable = true
                        replaceChildFragment(HomeMonthExpensesFragment())
                    }
                    binding.btGeneralExpenses.id -> {
                        binding.btMonthlyExpenses.isClickable = true
                        binding.btGeneralExpenses.isClickable = false
                        replaceChildFragment(HomeAllExpensesFragment())
                    }
                }
            }
        }
    }

    private fun replaceChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(binding.fcvExpensesInfo.id, fragment)
            .commit()
    }
}