package com.example.fico.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fico.R
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnButtonClickListener{

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val bindingAdd by lazy { FragmentAddExpenseBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.bottomNavigation
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home,
            R.id.navigation_add_expense,
            R.id.navigation_expense_list,
            R.id.navigation_config
        ),binding.drawerLayout)
        setupActionBarWithNavController(navController,appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        getUserEmail()
        getUserName()
        setupListeners()
    }

    override fun onSaveButtonFragmentClick() {
        bindingAdd.btSave.performClick()
    }

    private fun getUserEmail(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val headerView = navigationView.getHeaderView(0)
        val headerUserEmail = headerView.findViewById<TextView>(R.id.nv_header_user_email)
        lifecycleScope.launch {
            val email = viewModel.getUserEmail().await()
            headerUserEmail.text = email
        }
    }

    private fun getUserName(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val headerView = navigationView.getHeaderView(0)
        val headerUserName = headerView.findViewById<TextView>(R.id.nv_header_user_name)
        lifecycleScope.launch {
            val name = viewModel.getUserName().await()
            headerUserName.text = name
        }
    }

    private fun setupListeners(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val headerView = navigationView.getHeaderView(0)
        headerView.setOnClickListener {

        }
    }
}