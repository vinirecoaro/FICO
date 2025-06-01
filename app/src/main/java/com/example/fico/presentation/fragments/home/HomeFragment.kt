package com.example.fico.presentation.fragments.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.presentation.fragments.home.balance.HomeAllBalanceFragment
import com.example.fico.presentation.fragments.home.balance.HomeMonthBalanceFragment
import com.example.fico.presentation.fragments.home.expenses.HomeAllExpensesFragment
import com.example.fico.presentation.fragments.home.expenses.HomeMonthExpensesFragment
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.ui_personalizations.Effects.Companion.fadeIn
import com.example.fico.utils.ui_personalizations.Effects.Companion.fadeOut
import org.koin.android.ext.android.inject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by inject()
    private lateinit var underline: View
    private lateinit var bntExpenses: TextView
    private lateinit var bntBalance: TextView
    private lateinit var bntEarnings: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        underline = binding.underline
        bntExpenses = binding.tvBtnExpenses
        bntBalance = binding.tvBtnBalance
        bntEarnings = binding.tvBtnEarnings

        // Set underline position on the first button
        binding.llButtonsLayout.post {
            moveUnderlineTo(bntExpenses)
        }

        setUpListeners()

        // Show default fragment on initialization
        replaceChildFragment(HomeMonthExpensesFragment())

        return rootView
    }

    override fun onResume() {
        super.onResume()
        binding.btgExpensesDataRange.check(binding.btMonthlyExpenses.id)
        binding.btMonthlyExpenses.isClickable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        bntExpenses.setOnClickListener {
            moveUnderlineTo(bntExpenses)
            viewModel.setTransactionType(StringConstants.DATABASE.EXPENSE)
            binding.btgExpensesDataRange.visibility = View.VISIBLE
            binding.btgExpensesDataRange.check(binding.btMonthlyExpenses.id)
            binding.btMonthlyExpenses.isClickable = false
            binding.ivFragmentHomeArrow.visibility = View.VISIBLE
            replaceChildFragment(HomeMonthExpensesFragment())
        }
        bntBalance.setOnClickListener {
            moveUnderlineTo(bntBalance)
            viewModel.setTransactionType(StringConstants.DATABASE.BALANCE)
            binding.btgExpensesDataRange.visibility = View.VISIBLE
            binding.btgExpensesDataRange.check(binding.btMonthlyExpenses.id)
            binding.btMonthlyExpenses.isClickable = false
            binding.ivFragmentHomeArrow.visibility = View.VISIBLE
            replaceChildFragment(HomeMonthBalanceFragment())
        }
        bntEarnings.setOnClickListener {
            moveUnderlineTo(bntEarnings)
            viewModel.setTransactionType(StringConstants.DATABASE.EARNING)
            binding.btgExpensesDataRange.visibility = View.GONE
            binding.ivFragmentHomeArrow.visibility = View.GONE
            replaceChildFragment(HomeEarningsFragment())
        }
        binding.btgExpensesDataRange.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.btMonthlyExpenses.id -> {
                        binding.btMonthlyExpenses.isClickable = false
                        binding.btGeneralExpenses.isClickable = true
                        if(viewModel.getTransactionType() != null){
                            if(viewModel.getTransactionType() == StringConstants.DATABASE.EXPENSE){
                                replaceChildFragment(HomeMonthExpensesFragment())
                            }else if(viewModel.getTransactionType() == StringConstants.DATABASE.BALANCE){
                                replaceChildFragment(HomeMonthBalanceFragment())
                            }
                        }
                    }

                    binding.btGeneralExpenses.id -> {
                        binding.btMonthlyExpenses.isClickable = true
                        binding.btGeneralExpenses.isClickable = false
                        if(viewModel.getTransactionType() != null){
                            if(viewModel.getTransactionType() == StringConstants.DATABASE.EXPENSE){
                                replaceChildFragment(HomeAllExpensesFragment())
                            }else if(viewModel.getTransactionType() == StringConstants.DATABASE.BALANCE){
                                replaceChildFragment(HomeAllBalanceFragment())
                            }
                        }
                    }
                }
            }
        }

        viewModel.arrowState.observe(viewLifecycleOwner){ state ->
            if(state){
                binding.btgExpensesDataRange.fadeIn()
                binding.ivFragmentHomeArrow.animate().rotation(0f).setDuration(200).start()
            }else{
                binding.btgExpensesDataRange.fadeOut()
                binding.ivFragmentHomeArrow.animate().rotation(90f).setDuration(200).start()
            }
        }

        binding.ivFragmentHomeArrow.setOnClickListener {
            val state = viewModel.arrowState.value
            if(state!!){
                viewModel.setArrowState(false)
            }else{
                viewModel.setArrowState(true)
            }
        }

    }

    private fun moveUnderlineTo(button: TextView) {
        val parentOffset = IntArray(2)
        val buttonOffset = IntArray(2)

        binding.clButtonContainer.getLocationOnScreen(parentOffset)
        button.getLocationOnScreen(buttonOffset)

        val relativeX = buttonOffset[0] - parentOffset[0]

        underline.animate()
            .translationX(relativeX.toFloat())
            .setDuration(200)
            .start()

        underline.layoutParams.width = button.width
        underline.requestLayout()
    }

    private fun replaceChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(binding.fragmentTransactionsInfo.id, fragment)
            .commit()
    }


}