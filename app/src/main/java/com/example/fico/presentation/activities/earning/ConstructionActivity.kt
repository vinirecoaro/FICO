package com.example.fico.presentation.activities.earning

import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fico.R
import com.example.fico.databinding.ActivityConstructionBinding

class ConstructionActivity : AppCompatActivity() {

    private val binding by lazy { ActivityConstructionBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.earningsHomeToolbar.setTitle("Home")
        binding.earningsHomeToolbar.setTitleTextColor(Color.WHITE)

        setSupportActionBar(binding.earningsHomeToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.earningsHomeToolbar.setNavigationOnClickListener {
            finish()
        }

        setColorBasedOnTheme()

    }

    private fun setColorBasedOnTheme(){
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivConstruction.setImageResource(R.drawable.baseline_construction_24_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivConstruction.setImageResource(R.drawable.baseline_construction_24_black)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

}