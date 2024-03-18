package com.example.fico.presentation.activities

import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityUserDataBinding
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class UserDataActivity : AppCompatActivity() {

    private val binding by lazy {ActivityUserDataBinding.inflate(layoutInflater)}
    private val viewModel : UserDataViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.userDataToolbar.setTitle("Dados Pessoais")
        binding.userDataToolbar.setTitleTextColor(Color.WHITE)

        setSupportActionBar(binding.userDataToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setColorBasedOnTheme()
        getUserEmail()
        getUserName()
        setUpListeners()
    }

    private fun setUpListeners(){
        binding.ivEditName.setOnClickListener {
            setUserNameAlertDialog()
        }

        binding.userDataToolbar.setNavigationOnClickListener {
            finish()
        }
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

    private fun setUserNameAlertDialog() : CompletableDeferred<Boolean>{
        val result = CompletableDeferred<Boolean>()
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Editar Nome")

        val newUserName = EditText(this)
        newUserName.hint = "Nome"
        builder.setView(newUserName)

        builder.setPositiveButton("Salvar") { dialog, which ->
            val saveButton =  (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            lifecycleScope.launch {
                if(newUserName.text.toString() != ""){

                    if(viewModel.editUserName(newUserName.text.toString()).await()){
                        val rootView: View? = findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Nome redefinido com sucesso", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            getUserName()
                            result.complete(true)
                        }
                    }else{
                        val rootView: View? = findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Falha ao redefinir o nome", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            result.complete(false)
                        }
                    }
                }
            }
            saveButton.isEnabled = true
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
        return result
    }
}