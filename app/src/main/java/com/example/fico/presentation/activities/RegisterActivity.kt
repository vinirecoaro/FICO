package com.example.fico.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityRegisterBinding
import com.example.fico.presentation.viewmodel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {ActivityRegisterBinding.inflate(layoutInflater)}
    private val viewModel : RegisterViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.registerToolbar.setTitle(getString(R.string.register))
        binding.registerToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.registerToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()
    }

    private fun setUpListeners(){
        binding.btRegister.setOnClickListener{
            binding.btRegister.isEnabled = false
            if(checkFields(binding.etName, binding.etEmail, binding.etPassword)){
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.register(
                        binding.etName.text.toString(),
                        binding.etEmail.text.toString(),
                        binding.etPassword.text.toString())
                }
            }
            binding.btRegister.isEnabled = true
        }

        viewModel.onUserCreated = {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.sendEmailVerificarion()
            }
            startActivity(Intent(this, VerifyEmailActivity::class.java))
            finish()
        }

        viewModel.onError = { message ->
            Snackbar.make(binding.btRegister, message, Snackbar.LENGTH_LONG).show()
        }

        viewModel.onSendEmailSuccess = {
            Snackbar.make(binding.btRegister, getString(R.string.verification_email_success_message), Snackbar.LENGTH_LONG).show()
        }

        viewModel.onSendEmailFailure = {
            Snackbar.make(binding.btRegister, getString(R.string.verification_email_fail_message), Snackbar.LENGTH_LONG).show()
        }

        binding.registerToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    fun checkFields(vararg fields : EditText): Boolean {
        val nFileds = fields.size
        var counter = 0
        for (i in fields){
            if (i.text.isEmpty()){
                emptyField(i)
                return false
            }else{
                counter++
            }
        }
        return counter == nFileds
    }

    fun emptyField(text: EditText){
        val snackbar = Snackbar.make(binding.btRegister, "${getString(R.string.empty_field_part_1_message)} ${text.hint} ${getString(R.string.empty_field_part_2_message)}", Snackbar.LENGTH_LONG)
        snackbar.show()
    }

}