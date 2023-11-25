package com.example.fico.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseSetBudgetSharedViewModel
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat
import java.text.NumberFormat

class AddExpenseFragment : Fragment(), OnButtonClickListener{

    private var _binding : FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")
    private val viewModel by viewModels<AddExpenseViewModel>()
    private val sharedViewModel: AddExpenseSetBudgetSharedViewModel by activityViewModels()
    private val PICK_XLS_FILE = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater,container,false)
        var rootView = binding.root

        setUpListeners()
        actvConfig()
        setColorBasedOnTheme()

        binding.etDate.setText(viewModel.getCurrentlyDate())
        binding.etDate.inputType = InputType.TYPE_NULL

        setMaxLength(binding.etInstallments, 3)

        return rootView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_expense_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_expense_menu_common -> {
                binding.etInstallments.visibility = View.GONE
                binding.etPrice.setText("")
                binding.etDescription.setText("")
                binding.actvCategory.setText("")
                binding.etInstallments.setText("")
                return true
            }
            R.id.add_expense_menu_installments -> {
                binding.etInstallments.visibility = View.VISIBLE
                binding.etPrice.setText("")
                binding.etDescription.setText("")
                binding.actvCategory.setText("")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        binding.btSave.setOnClickListener {
            binding.btSave.isEnabled = false
            lifecycleScope.launch(Dispatchers.Main) {
                if(binding.etInstallments.visibility == View.GONE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                        val day = binding.etDate.text.toString().substring(0, 2)
                        val month = binding.etDate.text.toString().substring(3, 5)
                        val year = binding.etDate.text.toString().substring(6, 10)
                        val modifiedDate = "$year-$month-$day"
                        val checkDate = "$year-$month"

                        val regex = Regex("[\\d,.]+")
                        val justNumber = regex.find(binding.etPrice.text.toString())
                        val formatNum = DecimalFormat("#.##")
                        val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                        val formatedNum = formatNum.format(numClean/100)
                        val formattedNumString = formatedNum.toString().replace(",",".")

                        val existsDate = viewModel.checkIfExistsDateOnDatabse(checkDate).await()
                        if (existsDate) {
                            viewModel.addExpense(
                                formattedNumString,
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
                                formattedNumString,
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                modifiedDate
                            )

                            childFragmentManager.beginTransaction()
                                .replace(binding.fragSetBudget.id, setMonthBudget)
                                .commit()

                        }
                    }
                }else if(binding.etInstallments.visibility == View.VISIBLE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                        val day = binding.etDate.text.toString().substring(0, 2)
                        val month = binding.etDate.text.toString().substring(3, 5)
                        val year = binding.etDate.text.toString().substring(6, 10)
                        val modifiedDate = "$year-$month-$day"

                        val regex = Regex("[\\d,.]+")
                        val justNumber = regex.find(binding.etPrice.text.toString())
                        val formatNum = DecimalFormat("#.##")
                        val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()/binding.etInstallments.text.toString().toInt()
                        val formatedNum = formatNum.format(numClean/100)
                        val formattedNumString = formatedNum.toString().replace(",",".")

                        val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                        if (existsDefaultBudget) {
                            viewModel.addInstallmentsExpense(
                                formattedNumString,
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                modifiedDate,
                                binding.etInstallments.text.toString().toInt()
                            )
                            binding.etPrice.setText("")
                            binding.etDescription.setText("")
                            binding.actvCategory.setText("")
                            binding.etInstallments.setText("")
                        } else {
                            if(setUpDefaultBudgetAlertDialog().await()){
                                viewModel.addInstallmentsExpense(
                                    formattedNumString,
                                    binding.etDescription.text.toString(),
                                    binding.actvCategory.text.toString(),
                                    modifiedDate,
                                    binding.etInstallments.text.toString().toInt()
                                )
                                binding.etPrice.setText("")
                                binding.etDescription.setText("")
                                binding.actvCategory.setText("")
                                binding.etInstallments.setText("")
                            }
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true
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
            binding.dpDateExpense.visibility = View.GONE
        }

        binding.ivDate.setOnClickListener{
            binding.fragSetBudget.visibility = View.GONE
            binding.btSave.visibility = View.VISIBLE
            binding.dpDateExpense.visibility = View.VISIBLE
            binding.dpDateExpense.setOnDateChangedListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDate.setText(selectedDate)
            }
        }

        binding.etPrice.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }

        binding.etDescription.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }

        binding.actvCategory.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                    binding.etPrice.removeTextChangedListener(this)
                    binding.etPrice.setText(formatted)
                    binding.etPrice.setSelection(formatted.length)
                    binding.etPrice.addTextChangedListener(this)
                }
            }
        })

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

    private fun setUpDefaultBudgetAlertDialog() : CompletableDeferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Definir o budget padrão")

        val defaultBudget = EditText(requireContext())
        defaultBudget.hint = "Budget Padrão"
        defaultBudget.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(defaultBudget)

        defaultBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                    defaultBudget.removeTextChangedListener(this)
                    defaultBudget.setText(formatted)
                    defaultBudget.setSelection(formatted.length)
                    defaultBudget.addTextChangedListener(this)
                }
            }
        })

        builder.setPositiveButton("Salvar") { dialog, which ->
            val saveButton =  (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            lifecycleScope.launch {
                if(defaultBudget.text.toString() != ""){

                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(defaultBudget.text.toString())
                    val formatNum = DecimalFormat("#.##")
                    val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                    val formatedNum = formatNum.format(numClean/100)
                    val formattedNumString = formatedNum.toString().replace(",",".")

                    if(viewModel.setDefaultBudget(formattedNumString).await()){
                        val rootView: View? = activity?.findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Budget editado com sucesso", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            result.complete(true)
                        }
                    }else{
                        val rootView: View? = activity?.findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Falha ao editar o Default", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            result.complete(false)
                        }
                    }
                }
            }
            saveButton.isEnabled = true
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
     return result
    }

    fun setMaxLength(editText: EditText, maxLength: Int) {
        val inputFilter = object : InputFilter {
            override fun filter(
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val inputText = editText.text.toString() + source.toString()
                if (inputText.length <= maxLength) {
                    return null // Aceita a entrada
                }
                return "" // Rejeita a entrada se exceder o limite
            }
        }
        val filters = editText.filters
        val newFilters = if (filters != null) {
            val newFilters = filters.copyOf(filters.size + 1)
            newFilters[filters.size] = inputFilter
            newFilters
        } else {
            arrayOf(inputFilter)
        }
        editText.filters = newFilters
    }

    override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

    private fun setColorBasedOnTheme(){
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivDate.setImageResource(R.drawable.baseline_calendar_month_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivDate.setImageResource(R.drawable.baseline_calendar_month_dark)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    fun readXLSFile() {

        val nomeArquivo = "caminho/do/seu/arquivo.xls"

        try {
            val arquivo = FileInputStream(File(nomeArquivo))
            val workbook = HSSFWorkbook(arquivo)

            val sheet = workbook.getSheetAt(0) // Obter a primeira planilha

            for (linha in sheet) {
                for (celula in linha) {
                    // Processar cada célula conforme necessário
                    val valorCelula = celula.toString()
                    println("Valor da célula: $valorCelula")
                }
            }

            arquivo.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun selectArchive() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/vnd.ms-excel" // Filtrar para apenas arquivos .xls (ou .xlsx)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Selecione um arquivo .xls"), PICK_XLS_FILE)
    }

}
