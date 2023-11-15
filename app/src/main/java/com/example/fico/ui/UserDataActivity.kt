package com.example.fico.ui

import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityUserDataBinding
import com.example.fico.ui.viewmodel.UserDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDataActivity : AppCompatActivity() {

    private val binding by lazy {ActivityUserDataBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<UserDataViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbar.setTitle("Dados Pessoais")
        binding.toolbar.setTitleTextColor(Color.WHITE)
        setColorBasedOnTheme()
        getUserEmail()
        getUserName()
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

    private fun getUserName(){
        lifecycleScope.launch(Dispatchers.Main){
            binding.tvNameValue.text = viewModel.getUserName().await()
        }
    }

    private fun getUserEmail(){
        lifecycleScope.launch(Dispatchers.Main){
            binding.tvEmailValue.text = viewModel.getUserEmail().await()
        }
    }
}