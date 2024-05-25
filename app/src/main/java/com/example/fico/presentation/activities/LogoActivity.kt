package com.example.fico.presentation.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.ActivityLogoBinding
import com.example.fico.presentation.activities.expense.MainActivity
import com.example.fico.presentation.viewmodel.LogoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LogoActivity : AppCompatActivity() {

    private val binding by lazy {ActivityLogoBinding.inflate(layoutInflater)}
    private val viewModel : LogoViewModel by inject()

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

        val marginPercentage = 0.32
        val horizontalMargin = (screenWidth * marginPercentage).toInt()

        layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0)
        binding.ivLogo.layoutParams = layoutParams

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpListeners(){
        viewModel.onUserLogged = {
            lifecycleScope.launch {
                if(viewModel.verifyExistsExpensesPath().await()){
                    startActivity(Intent(this@LogoActivity, MainActivity::class.java))
                }else{
                    Toast.makeText(this@LogoActivity, "Atualizando informações", Toast.LENGTH_LONG).show()
                    delay(2000);
                    viewModel.updateExpensesDatabasePath().await()
                    startActivity(Intent(this@LogoActivity, MainActivity::class.java))
                }
            }
        }

        viewModel.onError = { message ->
            Snackbar.make(binding.ivLogo, message, Snackbar.LENGTH_LONG).show()
        }
    }

}