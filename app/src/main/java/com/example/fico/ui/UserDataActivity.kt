package com.example.fico.ui

import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fico.R
import com.example.fico.databinding.ActivityUserDataBinding

class UserDataActivity : AppCompatActivity() {

    private val binding by lazy {ActivityUserDataBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbar.setTitle("Dados Pessoais")
        binding.toolbar.setTitleTextColor(Color.WHITE)
        setColorBasedOnTheme()
    }

    private fun setColorBasedOnTheme(){
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_person_24_light,0, 0, 0)
                binding.ivEditName.setImageResource(R.drawable.baseline_edit_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_person_24_black,0, 0, 0)
                binding.ivEditName.setImageResource(R.drawable.baseline_edit_black)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }
}