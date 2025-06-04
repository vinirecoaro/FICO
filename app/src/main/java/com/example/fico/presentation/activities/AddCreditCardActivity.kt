package com.example.fico.presentation.activities

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fico.R
import com.example.fico.components.inputs.InputAdapters
import com.example.fico.components.inputs.InputValueHandle
import com.example.fico.databinding.ActivityAddCreditCardBinding
import com.example.fico.databinding.ActivityCreditCardConfigurationBinding
import com.example.fico.model.ColorOption
import com.example.fico.presentation.viewmodel.AddCreditCardViewModel
import com.example.fico.presentation.viewmodel.CreditCardConfigurationViewModel
import org.koin.android.ext.android.inject

class AddCreditCardActivity : AppCompatActivity() {

    private val binding by lazy{ ActivityAddCreditCardBinding.inflate(layoutInflater)}
    private val viewModel : AddCreditCardViewModel by inject()
    lateinit var adapter : ArrayAdapter<ColorOption>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.addCreditCardConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = InputAdapters.colorAutoCompleteTextInputLayout(this, viewModel.colorOptions)

        initComponents()

        setUpListeners()

    }

    private fun initComponents(){
        binding.actvColors.setAdapter(
            adapter
        )
    }

    private fun setUpListeners(){
        binding.addCreditCardConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.actvColors.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener

            //Create string with circle and color name
            val spannable = InputValueHandle.circleColorfulWithText(binding.actvColors, selected.colorValue, selected.name)

            binding.actvColors.setText(spannable, false)
        }

    }



}
