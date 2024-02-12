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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
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
    private var barChartEntries : ArrayList<BarEntry> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.tvTotalExpensesValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.tvTotalExpensesValue.transformationMethod = PasswordTransformationMethod()
        binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.ic_visibility_off_24,
            0)
        setUpListeners()
        initExpenseEachMonthChartEmpty()

        return rootView
    }

    private fun setUpListeners(){
        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.ShowHideValue(binding.tvTotalExpensesValue)
        }
        viewModel.infoPerMonthLiveData.observe(viewLifecycleOwner){ infoPerMonthList ->
            var i = 1f
            for (infoPerMonth in infoPerMonthList){
                val monthExpense = infoPerMonth.monthExpense.toFloat()
                barChartEntries.add(BarEntry(i, monthExpense))
                i += 1f
            }
            initExpenseEachMonthChart()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        getTotalExpense()
        viewModel.getInfoPerMonth()
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

    private fun initExpenseEachMonthChart(){
        val barChart = binding.bcExpenseEachMonth

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 20f))
        entries.add(BarEntry(2f, 15f))
        entries.add(BarEntry(3f, 30f))
        entries.add(BarEntry(4f, 25f))
        entries.add(BarEntry(5f, 35f))

        // Crie um conjunto de dados com a lista de entradas
        val dataSet = BarDataSet(entries, "Label") // "Label" é o nome da legenda
        //dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        // Personalize as cores do conjunto de dados
        dataSet.color = Color.BLUE

        // Crie um objeto BarData e defina o conjunto de dados
        val barData = BarData(dataSet)

        // Configure o espaçamento entre as barras
        barData.barWidth = 0.5f

        // Defina os dados para o gráfico de barras
        barChart.data = barData
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)

        // Atualize o gráfico
        barChart.invalidate()

    }

    private fun initExpenseEachMonthChartEmpty(){
        val barChart = binding.bcExpenseEachMonth

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 20f))
        entries.add(BarEntry(2f, 15f))
        entries.add(BarEntry(3f, 30f))
        entries.add(BarEntry(4f, 25f))
        entries.add(BarEntry(5f, 35f))

        // Crie um conjunto de dados com a lista de entradas
        val dataSet = BarDataSet(entries, "Label") // "Label" é o nome da legenda
        //dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        // Personalize as cores do conjunto de dados
        dataSet.color = Color.BLUE

        // Crie um objeto BarData e defina o conjunto de dados
        val barData = BarData(dataSet)

        // Configure o espaçamento entre as barras
        barData.barWidth = 0.5f

        // Defina os dados para o gráfico de barras
        barChart.data = barData
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)

        // Atualize o gráfico
        barChart.invalidate()

    }
}