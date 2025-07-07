package com.example.fico.presentation.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.UpdateTransactionFromFileInfo
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.CreditCardItem
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.UpdateFromFileItem
import com.example.fico.presentation.compose.theme.FICOTheme
import com.example.fico.presentation.viewmodel.EditTransactionViewModel
import com.example.fico.presentation.viewmodel.ImportTransactionsFromFileHistoryViewModel
import com.example.fico.utils.DateFunctions
import org.koin.android.ext.android.inject
import kotlin.getValue

class ImportTransactionsFromFileHistoryComposeActivity : ComponentActivity() {

    private val viewModel : ImportTransactionsFromFileHistoryViewModel by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            FICOTheme {
                ImportTransactionsFromFileHistoryScreen(emptyList(), onBackClick = {finish()})
            }
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUploadsFromFileList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners(){
        viewModel.uploadsFromFileList.observe(this){ uploadsList ->
            setContent {
                FICOTheme {
                    ImportTransactionsFromFileHistoryScreen(uploadsList, onBackClick = {finish()})
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTransactionsFromFileHistoryScreen(
    updatesFromFileList : List<UpdateTransactionFromFileInfo>,
    onBackClick: () -> Unit
) {

    SetStatusBarColor(color = colorResource(id = R.color.blue_500), darkIcons = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.imports_from_file_history_activity_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.blue_400),
                    titleContentColor = colorResource(id = R.color.white),
                    navigationIconContentColor = colorResource(id = R.color.white)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            LazyColumn {
                items(updatesFromFileList) { item ->
                    val nOfExpenses = item.expenseIdList.filter { !it.contains("Parcela") }.size.toString()
                    val nOfEarnings = item.earningIdList.size.toString()
                    val installmentIdList = mutableListOf<String>()
                    for (id in item.expenseIdList){
                        val commonId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(id)
                        if (
                            id.contains("Parcela") &&
                            !installmentIdList.any{FormatValuesFromDatabase().commonIdOnInstallmentExpense(it) == commonId}
                        ){
                            installmentIdList.add(id)
                        }
                    }
                    val nOfInstallmentExpenses = installmentIdList.size.toString()
                    val date = DateFunctions().formatDateTimeToShow(item.inputDateTime)
                    UpdateFromFileItem(
                        date,
                        nOfExpenses, nOfEarnings, nOfInstallmentExpenses,
                    )
                }
            }
        }
    }
}

@Composable
fun SetStatusBarColor(
    color: Color,
    darkIcons: Boolean = true
) {
    val view = LocalView.current
    val activity = LocalActivity.current

    SideEffect {
        val window = activity!!.window
        window.statusBarColor = color.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
    }
}


