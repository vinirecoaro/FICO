package com.example.fico.ui.activities.expense

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.fico.R
import com.example.fico.databinding.ActivityComonExpenseImportFileInstructionsBinding
import com.example.fico.domain.model.ImportFileInstructionsComponents
import com.example.fico.ui.adapters.ImportFileInstructionsAdapter

class ComonExpenseImportFileInstructionsActivity : AppCompatActivity() {

    private val binding by lazy{ActivityComonExpenseImportFileInstructionsBinding.inflate(layoutInflater)}
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
                "Extensão do arquivo",
                setImageBasedOnTheme(
                    R.drawable.import_file_instructions_table_xls_light,
                    R.drawable.import_file_instructions_table_xls_black
                    ),
                "O arquivo deve ser com extensão xls.",
                false
            ),
            ImportFileInstructionsComponents(
                "Cebeçalho",
                R.drawable.import_file_instructions_complete_table,
                "O cabeçalho deve ser conforme ilustrado na imagem acima. " +
                        "Deve ter as colunas Preço, Descrição, Categoria e Data nessa ordem.",
                false
            ),
            ImportFileInstructionsComponents(
                "Coluna Preço",
                R.drawable.import_file_instructions_price_column,
                "Na coluna preço os valores podem \nestar nos formatos acima.",
                false
            ),
            ImportFileInstructionsComponents(
                "Coluna Data",
                R.drawable.import_file_instructions_date_column,
                "Na coluna data os valores devem ser no formato:\n\n" +
                        "dd/mm/aaaa",
                false
            ),
            ImportFileInstructionsComponents(
                "Identificador de linha final",
                R.drawable.import_file_instructions_final_line_identificator,
                "Para identificar a última linha a ser lida use\no identificador abaixo na linha posterior:\n\n" +
                        "xxx",
                false
            ),
            ImportFileInstructionsComponents(
                "Gastos Parcelados",
                setImageBasedOnTheme(
                    R.drawable.import_file_instructions_warning_light,
                    R.drawable.import_file_instructions_warning_black
                ),
                "Não inserir gastos parcelados através\ndesta opção.",
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
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
                binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
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
        if(currentPosition == 0){
            binding.dot1.setImageResource(R.drawable.ic_dot_selected)
            binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
        }
        else if(currentPosition == 1){
            binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot2.setImageResource(R.drawable.ic_dot_selected)
            binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
        }
        else if(currentPosition == 2){
            binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot3.setImageResource(R.drawable.ic_dot_selected)
            binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
        }
        else if(currentPosition == 3){
            binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot4.setImageResource(R.drawable.ic_dot_selected)
            binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
        }
        else if(currentPosition == 4){
            binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot5.setImageResource(R.drawable.ic_dot_selected)
            binding.dot6.setImageResource(R.drawable.ic_dot_unselected_black)
        }
        else if(currentPosition == 5){
            binding.dot1.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot2.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot3.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot4.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot5.setImageResource(R.drawable.ic_dot_unselected_black)
            binding.dot6.setImageResource(R.drawable.ic_dot_selected)
        }
    }

}