package com.example.fico.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.presentation.viewmodel.AddExpenseViewModel
import com.example.fico.presentation.viewmodel.BudgetPerMonthViewModel
import com.example.fico.presentation.viewmodel.DefaultPaymentDateConfigurationViewModel
import com.example.fico.presentation.viewmodel.EditExpenseViewModel
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.example.fico.presentation.viewmodel.ExpenseListViewModel
import com.example.fico.presentation.viewmodel.GeneralConfigurationViewModel
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.example.fico.presentation.viewmodel.LogoViewModel
import com.example.fico.presentation.viewmodel.MainViewModel
import com.example.fico.presentation.viewmodel.RegisterViewModel
import com.example.fico.presentation.viewmodel.ResetPasswordViewModel
import com.example.fico.presentation.viewmodel.SetDefaultBudgetViewModel
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.example.fico.presentation.viewmodel.VerifyEmailViewModel
import com.example.fico.presentation.viewmodel.shared.ExpensesViewModel
import com.example.fico.shared.constants.CategoriesList
import com.example.fico.shared.constants.StringConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@RequiresApi(Build.VERSION_CODES.N)
val appModule = module {

    single {
        androidContext().getSharedPreferences(StringConstants.SHARED_PREFERENCES.NAME, Context.MODE_PRIVATE)
    }

    //Create a singleton for FirebaseDatabase and FirebaseAuth
    single { FirebaseDatabase.getInstance() }
    single { FirebaseAuth.getInstance() }

    single<DataStoreManager>(){
        DataStoreManager(androidContext())
    }

    single {
        FirebaseAPI(
            auth = get(),
            database = get()
        )
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

    factory<MainViewModel> {
        MainViewModel(
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

    factory<BudgetPerMonthViewModel> {
        BudgetPerMonthViewModel(
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

    factory<DefaultPaymentDateConfigurationViewModel> {
        DefaultPaymentDateConfigurationViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<AddExpenseViewModel> {
        AddExpenseViewModel(
            firebaseAPI = get(),
            dataStore = get(),
        )
    }

    factory<CategoriesList>{
        CategoriesList(androidContext())
    }

}

