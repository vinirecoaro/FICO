package com.example.fico.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fico.R
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.presentation.viewmodel.MainViewModel
import com.example.fico.presentation.viewmodel.shared.ExpensesViewModel
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainExpenseActivity : AppCompatActivity(){

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel : MainViewModel by inject()
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

        setupListeners()
        removeTintOfMenuIcons()
        setImageBasedOnTheme()
        getUserName()
        getUserEmail()

        //Update DataStore with info from database
        if(ConnectionFunctions().internetConnectionVerification(this)){
            expensesViewModel.getExpenseList()
            expensesViewModel.getExpenseMonths()
            expensesViewModel.getExpenseInfoPerMonth()
            expensesViewModel.getTotalExpense()
            expensesViewModel.getDefaultBudget()
            expensesViewModel.getDefaultPaymentDay()
            expensesViewModel.getDaysForClosingBill()
            expensesViewModel.getEarningsList()
            expensesViewModel.getRecurringExpensesList()
        }
    }

    private fun getUserEmail(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val headerView = navigationView.getHeaderView(0)
        val headerUserEmail = headerView.findViewById<TextView>(R.id.nv_header_user_email)
        lifecycleScope.launch {
            var email = ""
            email = viewModel.getUserEmailDataStore().await()
            headerUserEmail.text = email
        }
    }

    private fun getUserName(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val headerView = navigationView.getHeaderView(0)
        val headerUserName = headerView.findViewById<TextView>(R.id.nv_header_user_name)
        lifecycleScope.launch {
            var name = ""
            name = viewModel.getUserNameDataStore().await()
            headerUserName.text = name
        }
    }

    private fun removeTintOfMenuIcons(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        navigationView.itemIconTintList = null
    }

    private fun setupListeners(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.nav_menu_item_personal_data -> {
                    startActivity(Intent(this, UserDataActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_menu_item_logout -> {
                    dialogLogout()
                    true
                }

                else -> false
            }
        }
    }

    private fun setImageBasedOnTheme(){
        val navigationView = findViewById<NavigationView>(R.id.nv_main)
        val menu = navigationView.menu
        val configMenuItem = menu.findItem(R.id.nav_menu_item_logout)
        val profileMenuItem = menu.findItem(R.id.nav_menu_item_personal_data)
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                configMenuItem.setIcon(R.drawable.logout_24_light)
                profileMenuItem.setIcon(R.drawable.baseline_person_24_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                configMenuItem.setIcon(R.drawable.logout_24_dark)
                profileMenuItem.setIcon(R.drawable.baseline_person_24_black)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

    }

    private fun dialogLogout(){
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(R.string.confirm) { dialog, which ->
                viewModel.logoff()
                finish()
                val intent = Intent(this, LoginActivity::class.java)

                // Create a new task to restart the app and clear the old activities stack
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
            .show()
    }

}