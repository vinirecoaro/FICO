package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fico.R
import com.example.fico.databinding.ActivityImportFileInstructionsBinding
import com.example.fico.model.ImportFileInstructionsComponents
import com.example.fico.ui.fragments.ImportFileInstructionsFragment

class ImportFileInstructionsActivity : AppCompatActivity() {

    private val binding by lazy{ActivityImportFileInstructionsBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val contentIndex = 0
        val fragment = ImportFileInstructionsFragment.newInstance(getImportFileInstructionsComponents()[contentIndex])

        supportFragmentManager.beginTransaction()
            .replace(binding.flContainer.id, fragment)
            .commit()

    }

    private fun getImportFileInstructionsComponents() : List<ImportFileInstructionsComponents>{
        val content = listOf(
            ImportFileInstructionsComponents(
                "Extensão do arquivo",
                R.drawable.ic_add_24,
                "O arquivo deve ser com extensão xls",
                false
            ),
            ImportFileInstructionsComponents(
                "Cebeçalho",
                R.drawable.ic_add_24,
                "O cabeçalho deve ser conforme ilustrado na imagem acima, " +
                        "portanto deve ter as colunas Preço, Descrição, Categoria e Data nessa ordem",
                false
            ),
            ImportFileInstructionsComponents(
                "Coluna Preço",
                R.drawable.ic_add_24,
                "Na coluna preço os valores podem estar no seguinte formato:\n" +
                        "R$ 20,00\n" +
                        "$ 20,00\n" +
                        "20,00",
                false
            ),
            ImportFileInstructionsComponents(
                "Coluna Data",
                R.drawable.ic_add_24,
                "Na coluna data os valores devem ser no formato:\n" +
                        "dd/mm/aaaa",
                true
            ),
        )
        return content
    }

}