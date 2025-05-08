package com.example.fico.presentation.fragments.home

import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.model.BarChartParams
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.utils.custom_component.RoundedBarChartRenderer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeViewModel by inject()
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

        // Inicialmente posiciona a underline sob o primeiro botão
        binding.llButtonsLayout.post {
            moveUnderlineTo(bntExpenses)
        }

        setUpListeners()

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
        bntExpenses.setOnClickListener { moveUnderlineTo(bntExpenses) }
        bntBalance.setOnClickListener { moveUnderlineTo(bntBalance) }
        bntEarnings.setOnClickListener { moveUnderlineTo(bntEarnings) }

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



}