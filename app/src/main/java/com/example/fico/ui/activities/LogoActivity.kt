package com.example.fico.ui.activities

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityLogoBinding
import com.example.fico.ui.activities.expense.MainActivity
import com.example.fico.ui.viewmodel.LogoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogoActivity : AppCompatActivity() {

    private val binding by lazy {ActivityLogoBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<LogoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        formatLogoImage()
        setUpListeners()

        lifecycleScope.launch(Dispatchers.Main) {
            if(!viewModel.isLogged().await()){
                startActivity(Intent(this@LogoActivity, LoginActivity::class.java))
            }else{
                finish()
            }
        }

    }

    private fun formatLogoImage(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val layoutParams = binding.ivLogo.layoutParams as ViewGroup.MarginLayoutParams

        val marginPercentage = 0.32 // 10%
        val horizontalMargin = (screenWidth * marginPercentage).toInt()

        layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0)
        binding.ivLogo.layoutParams = layoutParams

    }

    private fun setUpListeners(){
        viewModel.onUserLogged = {
            startActivity(Intent(this, MainActivity::class.java))
        }

        viewModel.onError = { message ->
            Snackbar.make(binding.ivLogo, message, Snackbar.LENGTH_LONG).show()
        }
    }

}