package com.example.fico.presentation.activities.expense

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fico.DataStoreManager
import com.example.fico.R
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.presentation.activities.GeneralConfigurationActivity
import com.example.fico.presentation.activities.UserDataActivity
import com.example.fico.presentation.activities.earning.ConstructionActivity
import com.example.fico.presentation.viewmodel.MainViewModel
import com.example.fico.presentation.viewmodel.shared.ExpensesViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainExpenseActivity : AppCompatActivity(){

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()
    private val expensesViewModel : ExpensesViewModel by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextColor(Color.WHITE)

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
        removeTintOfMenuIcons()
        setImageBasedOnTheme()

        //Update DataStore with info from database
        expensesViewModel.getExpenseList()
        expensesViewModel.getExpenseMonths()
        expensesViewModel.getExpenseInfoPerMonth()
        expensesViewModel.getTotalExpense()
        expensesViewModel.getDefaultBudget()
        expensesViewModel.getDefaultPaymentDay()
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

    private fun removeTintOfMenuIcons(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        navigationView.itemIconTintList = null
    }

    private fun setupListeners(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val headerView = navigationView.getHeaderView(0)
        headerView.setOnClickListener {
            startActivity(Intent(this, UserDataActivity::class.java))
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.nav_menu_item_expenses -> {
                    startActivity(Intent(this, MainExpenseActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_menu_item_earnings -> {
                    startActivity(Intent(this, ConstructionActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_menu_item_config -> {
                    startActivity(Intent(this, GeneralConfigurationActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                else -> false
            }
        }
    }

    private fun setImageBasedOnTheme(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val menu = navigationView.menu
        val configMenuItem = menu.findItem(R.id.nav_menu_item_config)
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                configMenuItem.setIcon(R.drawable.config_image_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                configMenuItem.setIcon(R.drawable.config_image_dark)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

    }

}