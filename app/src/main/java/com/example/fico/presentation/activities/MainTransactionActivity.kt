package com.example.fico.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fico.R
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.presentation.viewmodel.MainViewModel
import com.example.fico.presentation.viewmodel.shared.ExpensesViewModel
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class MainTransactionActivity : AppCompatActivity(){

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel : MainViewModel by inject()
    private val expensesViewModel : ExpensesViewModel by inject()
    private val imageFileName = StringConstants.USER_DATA_ACTIVITY.PROFILE_IMAGE_FILE_NAME
    private lateinit var navigationView: NavigationView
    private lateinit var headerView: View
    private lateinit var headerUserEmail: TextView
    private lateinit var headerUserName: TextView
    private lateinit var headerProfileImage: ImageView

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

        navigationView = findViewById(R.id.nv_main)
        headerView = navigationView.getHeaderView(0)
        headerUserEmail = headerView.findViewById(R.id.nv_header_user_email)
        headerUserName = headerView.findViewById(R.id.nv_header_user_name)
        headerProfileImage = headerView.findViewById(R.id.iv_header_profile_image)

        setupListeners()
        removeTintOfMenuIcons()
        setImageBasedOnTheme()
        fillDrawer()

        //Update DataStore with info from database
        if(ConnectionFunctions().internetConnectionVerification(this)){
            //expensesViewModel.getExpenseList()
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

    override fun onResume() {
        super.onResume()
        navigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until navigationView.menu.size()) {
            navigationView.menu.getItem(i).isChecked = false
        }
        navigationView.menu.setGroupCheckable(0, true, true)
    }

    private fun fillDrawer(){
        getUserEmail(headerUserEmail)
        getUserName(headerUserName)
        getProfileImage(headerProfileImage)
    }

    private fun getUserEmail(textView: TextView){
        lifecycleScope.launch {
            var email = ""
            email = viewModel.getUserEmailDataStore().await()
            textView.text = email
        }
    }

    private fun getUserName(textView: TextView){
        lifecycleScope.launch {
            var name = ""
            name = viewModel.getUserNameDataStore().await()
            textView.text = name
        }
    }

    private fun getProfileImage(imageView : ImageView) {
        val file = File(this.filesDir, imageFileName)
        if (file.exists()) {
            imageView.setImageURI(null)
            imageView.setImageURI(Uri.fromFile(file))
            imageView.invalidate()
        }
    }

    private fun removeTintOfMenuIcons(){
        navigationView.itemIconTintList = null
    }

    private fun setupListeners(){
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

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.open()
            getProfileImage(headerProfileImage)
        }

    }

    private fun setImageBasedOnTheme(){
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