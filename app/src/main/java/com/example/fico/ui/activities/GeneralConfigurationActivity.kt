package com.example.fico.ui.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.databinding.ActivityGeneralConfigurationBinding
import com.example.fico.util.constants.AppConstants
import com.example.fico.ui.adapters.GeneralConfigurationListAdapter
import com.example.fico.ui.interfaces.OnListItemClick
import com.example.fico.ui.viewmodel.GeneralConfigurationViewModel

class GeneralConfigurationActivity : AppCompatActivity(),OnListItemClick {

    private val binding by lazy { ActivityGeneralConfigurationBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<GeneralConfigurationViewModel>()
    private lateinit var generalConfigurationListAdapter : GeneralConfigurationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.generalConfigToolbar.setTitle("Configurações")
        binding.generalConfigToolbar.setTitleTextColor(Color.WHITE)

        binding.rvGeneralConfiguration.layoutManager = LinearLayoutManager(this)
        generalConfigurationListAdapter = GeneralConfigurationListAdapter(
            viewModel.configurationList
        )
        generalConfigurationListAdapter.setOnItemClickListener(this)
        binding.rvGeneralConfiguration.adapter = generalConfigurationListAdapter

        setSupportActionBar(binding.generalConfigToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

    }

    override fun onListItemClick(position: Int) {
        val item = viewModel.configurationList[position]
        if(item == AppConstants.GENERAL_CONFIGURATION_LIST.PERSONAL_DATA){
            startActivity(Intent(this, UserDataActivity::class.java))
        }
        else if(item == AppConstants.GENERAL_CONFIGURATION_LIST.LOGOUT){
            viewModel.logoff()
            finish()
            val intent = Intent(this, LoginActivity::class.java)

            // Create a new task to restart de app and clear the old activities stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    private fun setUpListeners(){
        binding.generalConfigToolbar.setNavigationOnClickListener {
            finish()
        }
    }
}