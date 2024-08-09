package com.example.fico.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.presentation.viewmodel.AddExpenseViewModel
import com.example.fico.presentation.viewmodel.EditExpenseViewModel
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.example.fico.presentation.viewmodel.ExpenseListViewModel
import com.example.fico.presentation.viewmodel.GeneralConfigurationViewModel
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.example.fico.presentation.viewmodel.LogoViewModel
import com.example.fico.presentation.viewmodel.RegisterViewModel
import com.example.fico.presentation.viewmodel.ResetPasswordViewModel
import com.example.fico.presentation.viewmodel.SetDefaultBudgetViewModel
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.example.fico.presentation.viewmodel.VerifyEmailViewModel
import com.example.fico.presentation.viewmodel.shared.ExpensesViewModel
import com.example.fico.util.constants.AppConstants
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.N)
val appModule = module {

    single {
        androidContext().getSharedPreferences(AppConstants.SHARED_PREFERENCES.NAME, Context.MODE_PRIVATE)
    }

    single<DataStoreManager>(){
        DataStoreManager(androidContext())
    }

    factory<ExpensesViewModel> {
        ExpensesViewModel(
            dataStore = get(),
            firebaseAPI = get()
        )
    }

    factory<VerifyEmailViewModel> {
        VerifyEmailViewModel(
            firebaseAPI = get()
        )
    }

    factory<UserDataViewModel> {
        UserDataViewModel(
            firebaseAPI = get()
        )
    }

    factory<EditExpenseViewModel> {
        EditExpenseViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<SetDefaultBudgetViewModel> {
        SetDefaultBudgetViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<ResetPasswordViewModel> {
        ResetPasswordViewModel(
            firebaseAPI = get()
        )
    }

    factory<RegisterViewModel> {
        RegisterViewModel(
            firebaseAPI = get()
        )
    }

    factory<LogoViewModel> {
        LogoViewModel(
            firebaseAPI = get()
        )
    }

    factory<LoginViewModel> {
        LoginViewModel(
            firebaseAPI = get()
        )
    }

    factory<GeneralConfigurationViewModel> {
        GeneralConfigurationViewModel(
            firebaseAPI = get()
        )
    }

    factory<ExpenseListViewModel> {
        ExpenseListViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<HomeViewModel> {
            HomeViewModel(
                firebaseAPI = get(),
                dataStore = get()
            )
        }

    factory<ExpenseConfigurationViewModel> {
        ExpenseConfigurationViewModel(
                firebaseAPI = get()
            )
        }

    factory<AddExpenseViewModel> {
        AddExpenseViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<FirebaseAPI>{
        FirebaseAPI.instance
    }

}

