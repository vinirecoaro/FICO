package com.example.fico.presentation.activities.import_transactions

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.UpdateFromFileItem
import com.example.fico.presentation.compose.theme.FICOTheme
import com.example.fico.presentation.viewmodel.ImportTransactionsHistoryViewModel
import com.example.fico.utils.DateFunctions
import org.koin.android.ext.android.inject
import kotlin.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

class ImportTransactionsFromFileHistoryComposeActivity : ComponentActivity() {

    private val viewModel : ImportTransactionsHistoryViewModel by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FICOTheme {
                ImportTransactionsFromFileHistoryScreen(viewModel, onBackClick = {finish()})
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUploadsFromFileList()
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTransactionsFromFileHistoryScreen(
    viewModel : ImportTransactionsHistoryViewModel,
    onBackClick: () -> Unit
) {

    val uiState = viewModel.uiState.collectAsState().value

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
            when(uiState){
                is ImportTransactionsHistoryUiState.Loading -> {
                    Column (
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ){
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(24.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                is ImportTransactionsHistoryUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center // Centraliza tudo verticalmente
                    ) {
                        Text(
                            text = stringResource(R.string.there_is_no_import_history_message),
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 30.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.empty_info),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
                is ImportTransactionsHistoryUiState.Success -> {
                    LazyColumn {
                        items(uiState.data) { item ->
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
                is ImportTransactionsHistoryUiState.Error -> {
                    Log.e("Error: ", uiState.message)
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


