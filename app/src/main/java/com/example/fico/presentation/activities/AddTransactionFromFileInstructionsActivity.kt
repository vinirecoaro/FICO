package com.example.fico.presentation.activities

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.fico.R
import com.example.fico.databinding.ActivityInstallmentExpenseImportFileInstructionsBinding
import com.example.fico.model.ImportFileInstructionsComponents
import com.example.fico.presentation.adapters.ImportFileInstructionsAdapter

class AddTransactionFromFileInstructionsActivity : AppCompatActivity() {

    private val binding by lazy{ActivityInstallmentExpenseImportFileInstructionsBinding.inflate(layoutInflater)}
    private lateinit var importFileInstructionsAdapter: ImportFileInstructionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setDotsColorBasedOnTheme()
        setUpListeners()

        importFileInstructionsAdapter = ImportFileInstructionsAdapter(this, getImportFileInstructionsComponents())
        binding.vpInstructions.adapter = importFileInstructionsAdapter

    }

    private fun getImportFileInstructionsComponents() : List<ImportFileInstructionsComponents>{
        val contents = listOf(
            ImportFileInstructionsComponents(
                getString(R.string.file_extension),
                setImageBasedOnTheme(
                    R.drawable.import_file_instructions_table_xls_dark,
                    R.drawable.import_file_instructions_table_xls_light
                ),
                getString(R.string.import_transaction_file_extension_message),
                true
            ),
            ImportFileInstructionsComponents(
                getString(R.string.sheet_names),
                R.drawable.sheet_names,
                getString(R.string.sheet_names_instructions),
                false
            ),
            ImportFileInstructionsComponents(
                getString(R.string.expense_header),
                R.drawable.expense_header,
                getString(R.string.expense_header_instructions),
                false
            ),
            ImportFileInstructionsComponents(
                getString(R.string.earning_header),
                R.drawable.earning_header,
                getString(R.string.earning_header_instructions),
                false
            ),
            ImportFileInstructionsComponents(
                getString(R.string.installment_expense_header),
                R.drawable.installment_expense_header,
                getString(R.string.installment_expense_header_instructions),
                false
            ),
            ImportFileInstructionsComponents(
                getString(R.string.price_column),
                R.drawable.import_file_instructions_price_column,
                getString(R.string.price_column_instructions),
                false
            ),
            ImportFileInstructionsComponents(
                getString(R.string.date_column),
                R.drawable.import_file_instructions_date_column,
                getString(R.string.date_column_instructions),
                false
            ),
            ImportFileInstructionsComponents(
                getString(R.string.final_line_identificator),
                R.drawable.import_file_instructions_final_line_identificator,
                getString(R.string.final_line_identificator_instructions),
                true
            ),
        )
        return contents
    }

    private fun setDotsColorBasedOnTheme(){
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.dot1.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot2.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot3.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot4.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot5.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot6.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot7.setImageResource(R.drawable.ic_dot_unselected_light)
                binding.dot8.setImageResource(R.drawable.ic_dot_unselected_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot7.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot8.setImageResource(R.drawable.ic_dot_unselected_black)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun setImageBasedOnTheme(lightImage : Int, darkImage : Int) : Int{
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                return lightImage
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                return darkImage
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
        return darkImage
    }

    private fun setUpListeners(){
        binding.vpInstructions.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position)
            }
        })
    }

    private fun updateIndicator(currentPosition: Int) {
        val dots = listOf(
            binding.dot1,
            binding.dot2,
            binding.dot3,
            binding.dot4,
            binding.dot5,
            binding.dot6,
            binding.dot7,
            binding.dot8
        )

        dots.forEachIndexed { index, imageView ->
            val resId = if (index == currentPosition) {
                R.drawable.ic_dot_selected
            } else {
                R.drawable.ic_dot_unselected_black
            }
            imageView.setImageResource(resId)
        }
    }


}