package com.example.fico.presentation.activities

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fico.R
import com.example.fico.databinding.ActivityDefaultPaymentDateConfigurationBinding
import com.example.fico.databinding.ActivitySecurityConfigurationBinding
import com.example.fico.presentation.viewmodel.DefaultPaymentDateConfigurationViewModel
import org.koin.android.ext.android.inject

class SecurityConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ ActivitySecurityConfigurationBinding.inflate(layoutInflater)}
   // private val viewModel : DefaultPaymentDateConfigurationViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.securityConfigurationToolbar.setTitle(getString(R.string.security))
        binding.securityConfigurationToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.securityConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

    }

    private fun setUpListeners(){
        binding.securityConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.swtBlockApp.setOnCheckedChangeListener{ _ , state ->

        }
    }
}