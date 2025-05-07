package com.example.fico.presentation.fragments.home

import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.databinding.FragmentExpenseMonthInfoHomeBinding
import com.example.fico.presentation.adapters.ExpenseMonthsListAdapter
import com.example.fico.interfaces.OnExpenseMonthSelectedListener
import com.example.fico.model.BarChartParams
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.custom_component.RoundedBarChartRenderer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat

class HomeExpensesFragment : Fragment() {

    private var _binding : FragmentExpenseMonthInfoHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeViewModel by inject()
    private lateinit var adapter : ExpenseMonthsListAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpenseMonthInfoHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        adapter = ExpenseMonthsListAdapter(requireContext(),emptyList())
        binding.rvExpenseMonths.adapter = adapter

        initExpenseEachMonthChartEmpty()

        setUpListeners()

        initEmptyChart(binding.pcExpensePerCategory, binding.pcMonthExpense, binding.pcAvailableNow)

        // Blur total value field configuration
        binding.tvTotalExpensesValue.setLayerType(TextView.LAYER_TYPE_SOFTWARE, null)

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        getAvailableNow()
        getMonthExpense()
        initMonthExpenseChart()
        initAvailableNowChart()
        viewModel.getCategoriesWithMoreExpense()
        viewModel.getExpenseMonths()
        viewModel.getTotalExpense()
        viewModel.getExpenseBarChartParams()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is HomeFragmentState.Loading -> {
                        binding.clInfo.visibility = View.GONE
                        binding.clHomeExpensesEmptyState.visibility = View.GONE
                        binding.pbExpensePerMonth.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clInfo.visibility = View.GONE
                        binding.clHomeExpensesEmptyState.visibility = View.VISIBLE
                        binding.pbExpensePerMonth.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {
                        binding.clInfo.visibility = View.VISIBLE
                        binding.clHomeExpensesEmptyState.visibility = View.GONE
                        binding.pbExpensePerMonth.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.expenseMonthsLiveData.observe(viewLifecycleOwner) { expenseMonths ->
            adapter.updateExpenseMonths(expenseMonths)
            focusOnCurrentMonth()
        }

        adapter.setOnItemClickListener(object : OnExpenseMonthSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onExpenseMonthSelected(date: String) {
                val formattedDate = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(date)
                getAvailableNow(formattedDate)
                getMonthExpense(formattedDate)
                initMonthExpenseChart(formattedDate)
                initAvailableNowChart(formattedDate)
                viewModel.getCategoriesWithMoreExpense(formattedDate)
            }
        })

        viewModel.expensePerCategory.observe(viewLifecycleOwner) { expensePerCategoryList ->
            initExpensePerCategoryChart(expensePerCategoryList)

            val imageViews = listOf(
                binding.ivIconCategoriesLegend1,
                binding.ivIconCategoriesLegend2,
                binding.ivIconCategoriesLegend3,
                binding.ivIconCategoriesLegend4,
                binding.ivIconCategoriesLegend5
            )

            val textViews = listOf(
                binding.tvTextCategoriesList1,
                binding.tvTextCategoriesList2,
                binding.tvTextCategoriesList3,
                binding.tvTextCategoriesList4,
                binding.tvTextCategoriesList5
            )

            val colors = viewModel.getPieChartCategoriesColors()

            for (i in imageViews.indices) {
                if (i < expensePerCategoryList.size) {
                    val (category, amount) = expensePerCategoryList[i]
                    imageViews[i].visibility = View.VISIBLE
                    textViews[i].visibility = View.VISIBLE
                    imageViews[i].setColorFilter(colors[i])
                    val text = "$category\n${NumberFormat.getCurrencyInstance().format(amount.toFloat())}"
                    textViews[i].text = text
                } else {
                    imageViews[i].visibility = View.GONE
                    textViews[i].visibility = View.GONE
                }
            }
        }

        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.changeBlurState()
        }

        viewModel.isBlurred.observe(viewLifecycleOwner){ state ->
            if (state) {
                val blurMaskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL) // Intensidade do desfoque
                binding.tvTotalExpensesValue.paint.maskFilter = blurMaskFilter
                binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
            } else {
                binding.tvTotalExpensesValue.paint.maskFilter = null
                binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_24, 0)
            }
            binding.tvTotalExpensesValue.invalidate()
        }

        viewModel.totalExpenseLiveData.observe(viewLifecycleOwner){totalExpense ->
            binding.tvTotalExpensesValue.text = totalExpense
        }

        viewModel.expenseBarChartParams.observe(viewLifecycleOwner){ barChartParams ->
            if(barChartParams.entries.isNotEmpty()){
                initExpenseEachMonthChart(barChartParams)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAvailableNow(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val availableNow = viewModel.getAvailableNow(date).await()
                val availableNowJustNumber = viewModel.getAvailableNow(date, formatted = false).await()
                var myColor = ContextCompat.getColor(requireContext(), R.color.red)
                if(availableNow == "---"){
                    binding.tvAvailableThisMonthValue.text = availableNow
                } else if(availableNowJustNumber.toFloat() < 0){
                    binding.tvAvailableThisMonthValue.setTextColor(myColor)
                    binding.tvAvailableThisMonthValue.text = availableNow
                } else {
                    when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            myColor = ContextCompat.getColor(requireContext(), R.color.white)
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                            myColor = ContextCompat.getColor(requireContext(), R.color.black)
                        }
                        Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
                    }
                    binding.tvAvailableThisMonthValue.setTextColor(myColor)
                    binding.tvAvailableThisMonthValue.text = availableNow
                }
            }catch (exception:Exception){}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMonthExpense(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val monthExpense = viewModel.getMonthExpense(date).await()
                binding.tvTotalExpensesThisMonthValue.text = monthExpense
            } catch (exception: Exception) {
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initMonthExpenseChart(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()) {

        lifecycleScope.launch{

            val pieChart = binding.pcMonthExpense
            var holeColor = 1

            val monthExpenseValue = viewModel.getMonthExpense(date, formatted = false).await()
            var monthExpenseValueFormatted = 0f
            if(monthExpenseValue != "---"){
                monthExpenseValueFormatted = monthExpenseValue.toFloat()
            }
            var monthExpenseColor = ""

            val availableNowValue = viewModel.getAvailableNow(date, formatted = false).await()
            var availableNowValueFormatted = 1f
            if(availableNowValue != "---"){
                availableNowValueFormatted = availableNowValue.toFloat()
            }

            val budget = monthExpenseValueFormatted + availableNowValueFormatted

            // Defining color of availableNow part
            if(availableNowValueFormatted < 0){
                monthExpenseColor = "#ed2b15" // Red
            }else if(monthExpenseValueFormatted <= (budget/2)){
                monthExpenseColor = "#19d14e" // Green
            }else if(monthExpenseValueFormatted <= (budget*0.85)){
                monthExpenseColor = "#ebe23b" // Yellow
            }else if(monthExpenseValueFormatted > (budget*0.85)){
                monthExpenseColor = "#ed2b15" // Red
            }

            // Defining chart inside hole color
            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    holeColor = Color.rgb(104, 110, 106)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    holeColor = Color.WHITE
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }

            // Create a entries list for Pie Chart
            val entries = mutableListOf<PieEntry>()
            if(availableNowValueFormatted < 0){
                entries.add(PieEntry(monthExpenseValueFormatted))
                entries.add(PieEntry(0f))
            }else{
                entries.add(PieEntry(monthExpenseValueFormatted))
                entries.add(PieEntry(availableNowValueFormatted))
            }

            // Colors for parts of chart
            val colors = listOf(
                Color.parseColor(monthExpenseColor), // FirstColor
                Color.parseColor("#9aa19c")  // SecondColor
            )

            // Create a data set from entries
            val dataSet = PieDataSet(entries, "Uso de Recursos")
            dataSet.colors = colors

            // Data set customizing
            dataSet.sliceSpace = 2f

            // Create an PieData object from data set
            val pieData = PieData(dataSet)
            pieData.setValueFormatter(PercentFormatter(pieChart)) // Format value as percentage

            // Configure the PieChart
            pieChart.data = pieData
            pieChart.setUsePercentValues(false)
            pieChart.description.isEnabled = false
            pieChart.setHoleRadius(80f) // middle chart hole size
            pieChart.setTransparentCircleRadius(85f) // Transparent area size
            pieChart.setHoleColor(holeColor)
            pieChart.legend.isEnabled = false

            // Ocult label values
            pieData.setDrawValues(false)

            // Circular animation on create chart
            pieChart.animateY(1400, Easing.EaseInOutQuad)

            // Update the chart
            pieChart.invalidate()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAvailableNowChart(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()) {

        lifecycleScope.launch{

            val pieChart = binding.pcAvailableNow
            var holeColor = 1

            val monthExpenseValue = viewModel.getMonthExpense(date, formatted = false).await()
            var monthExpenseValueFormatted = 0f
            if(monthExpenseValue != "---"){
                monthExpenseValueFormatted = monthExpenseValue.toFloat()
            }

            val availableNowValue = viewModel.getAvailableNow(date, formatted = false).await()
            var availableNowValueFormatted = 1f
            if(availableNowValue != "---"){
                availableNowValueFormatted = availableNowValue.toFloat()
            }
            var availableNowColor = ""
            val budget = monthExpenseValueFormatted + availableNowValueFormatted

            // Defining color of availableNow part
            if(availableNowValueFormatted >= (budget/2)){
                availableNowColor = "#19d14e" // Green
            }else if(availableNowValueFormatted >= (budget*0.15)){
                availableNowColor = "#ebe23b" // Yellow
            }else if(availableNowValueFormatted < (budget*0.15)){
                availableNowColor = "#ed2b15" // Red
            }

            // Defining chart insede hole color
            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    holeColor = Color.rgb(104, 110, 106)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    holeColor = Color.WHITE
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }

            // Create a entries list for Pie Chart
            val entries = mutableListOf<PieEntry>()
            if(availableNowValueFormatted < 0){
                entries.add(PieEntry(0f))
                entries.add(PieEntry(monthExpenseValueFormatted))
            }else{
                entries.add(PieEntry(availableNowValueFormatted))
                entries.add(PieEntry(monthExpenseValueFormatted))
            }

            // Colors for parts of chart
            val colors = listOf(
                Color.parseColor(availableNowColor), // FirstColor
                Color.parseColor("#9aa19c")  // SecondColor
            )

            // Create a data set from entries
            val dataSet = PieDataSet(entries, "Uso de Recursos")
            dataSet.colors = colors

            // Data set customizing
            dataSet.sliceSpace = 2f

            // Create an PieData object from data set
            val pieData = PieData(dataSet)
            pieData.setValueFormatter(PercentFormatter(pieChart)) // Format value as percentage

            // Configure the PieChart
            pieChart.data = pieData
            pieChart.setUsePercentValues(false)
            pieChart.description.isEnabled = false
            pieChart.setHoleRadius(75f) // middle chart hole size
            pieChart.setTransparentCircleRadius(80f) // Transparent area size
            pieChart.setHoleColor(holeColor)
            pieChart.legend.isEnabled = false

            // Ocult label values
            pieData.setDrawValues(false)

            // Circular animation on create chart
            pieChart.animateY(1400, Easing.EaseInOutQuad)

            // Update the chart
            pieChart.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initExpensePerCategoryChart(categoriesList : List<Pair<String, Double>>) {

        val pieChart = binding.pcExpensePerCategory
        var holeColor = 1

        // Defining chart insede hole color
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                holeColor = Color.rgb(104, 110, 106)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holeColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Create a entries list for Pie Chart and set a color for each entry
        val entries = mutableListOf<PieEntry>()
        val paletteColors = viewModel.getPieChartCategoriesColors()
        val colors = mutableListOf<Int>()
        categoriesList.forEachIndexed { index, category ->
            entries.add(PieEntry(category.second.toFloat(), category.first))
            colors.add(paletteColors[index])
        }

        // Create a data set from entries
        val dataSet = PieDataSet(entries, getString(R.string.categories))
        dataSet.colors = colors

        // Data set customizing
        dataSet.sliceSpace = 2f

        // Create an PieData object from data set
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart)) // Format value as percentage

        // Configure the PieChart
        pieChart.data = pieData
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.setHoleRadius(55f) // middle chart hole size
        pieChart.setTransparentCircleRadius(60f) // Transparent area size
        pieChart.setHoleColor(holeColor)
        pieChart.legend.isEnabled = false

        // Ocult label values
        pieData.setDrawValues(false)
        pieChart.setDrawEntryLabels(false)

        // Circular animation on create chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // Update the chart
        pieChart.invalidate()

    }

    fun initEmptyChart(vararg charts : PieChart){
        for (chart in charts){
            var holeColor = 1

            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    holeColor = Color.rgb(104, 110, 106)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    holeColor = Color.WHITE
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }

            val emptyEntries: ArrayList<PieEntry> = ArrayList()
            emptyEntries.add(PieEntry(0f, ""))

            val emptyChartColors = listOf(
                Color.parseColor("#19d14e"), // FirstColor
                Color.parseColor("#9aa19c")  // SecondColor
            )

            val emptyDataSet = PieDataSet(emptyEntries, "Label")
            emptyDataSet.colors = emptyChartColors

            val emptyData = PieData(emptyDataSet)
            chart.data = emptyData

            chart.setUsePercentValues(false)
            chart.description.isEnabled = false
            chart.setHoleRadius(80f) // middle chart hole size
            chart.setTransparentCircleRadius(85f) // Transparent area size
            chart.setHoleColor(holeColor)
            chart.legend.isEnabled = false
            chart.data.setDrawValues(false)// Ocult label values
            chart.animateY(1400, Easing.EaseInOutQuad)// Circular animation on create chart

            chart.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun focusOnCurrentMonth(){
        val currentDate = DateFunctions().getCurrentlyDateYearMonthToDatabase()
        val currentDateFormatted = FormatValuesFromDatabase().formatDateForFilterOnExpenseList(currentDate)
        val monthFocusPosition = viewModel.getCurrentMonthPositionOnList(currentDateFormatted)
        if(monthFocusPosition != RecyclerView.NO_POSITION){
            binding.rvExpenseMonths.scrollToPosition(monthFocusPosition)
        }
        adapter.selectItem(monthFocusPosition)
    }

    private fun initExpenseEachMonthChartEmpty(){
        val barChart = binding.bcExpenseEachMonth

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))

        // Crie um conjunto de dados com a lista de entradas
        val dataSet = BarDataSet(entries, "Label") // "Label" é o nome da legenda

        //dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        dataSet.color = Color.BLUE
        dataSet.setDrawValues(false)

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
        xAxis.setDrawLabels(false)

        // Atualize o gráfico
        barChart.invalidate()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initExpenseEachMonthChart(barChartParams : BarChartParams){
        val barChart = binding.bcExpenseEachMonth

        // Create a data set with entry list
        val dataSet = BarDataSet(barChartParams.entries, "Label")
        dataSet.valueTextSize = 12f
        dataSet.color = Color.BLUE

        //Customize values that appear on top of the bars
        val valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return NumberFormat.getCurrencyInstance().format(value)
            }
        }
        dataSet.valueFormatter = valueFormatter

        // Create an object BarData and define data set
        val barData = BarData(dataSet)

        // Define data to bar chart
        barChart.data = barData
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(barChartParams.xBarLabels)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)

        // Format text color based on theme
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                xAxis.textColor = Color.WHITE
                dataSet.valueTextColor = Color.WHITE
                barChart.axisLeft.textColor = Color.WHITE
                barChart.axisRight.textColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                xAxis.textColor = Color.BLACK
                dataSet.valueTextColor = Color.BLACK
                barChart.axisLeft.textColor = Color.BLACK
                barChart.axisRight.textColor = Color.BLACK
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Configure bar width
        barData.barWidth = 0.35f

        // Define number of visible bar
        barChart.setVisibleXRangeMaximum(3f)

        // Get current month position on chart and move chart to it
        val currentDateIndex = viewModel.getCurrentDatePositionBarChart().toFloat()

        barChart.moveViewToX(currentDateIndex)

        // Add animation of increasing bars
        barChart.animateY(1500, Easing.EaseInOutQuad)

        barChart.renderer = RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        barChart.renderer.initBuffers()

        // Update chart
        barChart.invalidate()

    }

}