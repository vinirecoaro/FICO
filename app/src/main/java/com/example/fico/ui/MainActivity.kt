package com.example.fico.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fico.R
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.ui.fragments.AddExpenseFragment
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), OnButtonClickListener{

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val bindingAdd by lazy { FragmentAddExpenseBinding.inflate(layoutInflater) }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.bottomNavigation
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home,
            R.id.navigation_add_expense,
            R.id.navigation_expense_list,
            R.id.navigation_config
        ))
        setupActionBarWithNavController(navController,appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onSaveButtonFragmentClick() {
        bindingAdd.btSave.performClick()
    }

}