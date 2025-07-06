package com.example.fico.presentation.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.fico.R
import com.example.fico.presentation.activities.ui.theme.FICOTheme

class ImportTransactionsFromFileHistoryComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            FICOTheme {
                ImportTransactionsFromFileHistoryScreen(onBackClick = {finish()})
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTransactionsFromFileHistoryScreen(onBackClick: () -> Unit) {

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
        ) {
            Text(
                text = "Conteúdo da tela",
                modifier = Modifier.padding(innerPadding)
            )
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FICOTheme {
        ImportTransactionsFromFileHistoryScreen({})
    }
}