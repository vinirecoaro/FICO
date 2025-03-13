package com.example.fico.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.interfaces.AuthInterface
import com.example.fico.interfaces.UserDataInterface
import com.example.fico.presentation.viewmodel.AddTransactionViewModel
import com.example.fico.presentation.viewmodel.BudgetConfigurationListViewModel
import com.example.fico.presentation.viewmodel.BudgetPerMonthViewModel
import com.example.fico.presentation.viewmodel.DefaultPaymentDateConfigurationViewModel
import com.example.fico.presentation.viewmodel.EditTransactionViewModel
import com.example.fico.presentation.viewmodel.TransactionConfigurationViewModel
import com.example.fico.presentation.viewmodel.TransactionListViewModel
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
import com.example.fico.repositories.AuthRepository
import com.example.fico.repositories.UserDataRepository
import com.example.fico.utils.constants.CategoriesList
import com.example.fico.utils.constants.StringConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidApplication
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
    single { FirebaseAPI(auth = get(), database = get()) }

    single<AuthInterface> { get<FirebaseAPI>() }
    single { AuthRepository(get()) }

    single<UserDataInterface> { get<FirebaseAPI>() }
    single { UserDataRepository(get()) }

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

    factory<MainViewModel> {
        MainViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<UserDataViewModel> {
        UserDataViewModel(
            firebaseAPI = get(),
            dataStore = get()
        )
    }

    factory<EditTransactionViewModel> {
        EditTransactionViewModel(
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

    factory<TransactionConfigurationViewModel> {
        TransactionConfigurationViewModel(
            androidContext(),
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

    factory<BudgetConfigurationListViewModel> {
        BudgetConfigurationListViewModel(
            androidContext()
        )
    }

    factory<RegisterViewModel> {
        RegisterViewModel(
            firebaseAPI = get(),
            authRepository = get()
        )
    }

    factory<LogoViewModel> {
        LogoViewModel(
            authRepository = get()
        )
    }

    factory<LoginViewModel> {
        LoginViewModel(
            androidApplication(),
            dataStore = get(),
            authRepository = get(),
            userDataRepository = get()
        )
    }

    factory<GeneralConfigurationViewModel> {
        GeneralConfigurationViewModel(
            firebaseAPI = get()
        )
    }

    factory<TransactionListViewModel> {
        TransactionListViewModel(
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

    factory<AddTransactionViewModel> {
        AddTransactionViewModel(
            firebaseAPI = get(),
            dataStore = get(),
        )
    }

    factory<CategoriesList>{
        CategoriesList(androidContext())
    }

}

