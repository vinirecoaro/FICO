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
import com.example.fico.presentation.fragments.home.expenses.HomeExpensesFragment
import com.example.fico.presentation.fragments.home.expenses.HomeMonthExpensesFragment

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var underline : View
    private lateinit var bntExpenses : TextView
    private lateinit var bntBalance : TextView
    private lateinit var bntEarnings : TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        replaceChildFragment(HomeExpensesFragment())

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){
        bntExpenses.setOnClickListener {
            moveUnderlineTo(bntExpenses)
            replaceChildFragment(HomeExpensesFragment())
        }
        bntBalance.setOnClickListener {
            moveUnderlineTo(bntBalance)
            replaceChildFragment(HomeBalanceFragment())
        }
        bntEarnings.setOnClickListener {
            moveUnderlineTo(bntEarnings)
            replaceChildFragment(HomeEarningsFragment())
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