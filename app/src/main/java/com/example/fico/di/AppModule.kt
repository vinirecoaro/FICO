package com.example.fico.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.interfaces.AuthInterface
import com.example.fico.interfaces.CreditCardInterface
import com.example.fico.interfaces.TransactionsInterface
import com.example.fico.interfaces.UserDataInterface
import com.example.fico.presentation.viewmodel.CreditCardViewModel
import com.example.fico.presentation.viewmodel.AddTransactionViewModel
import com.example.fico.presentation.viewmodel.BudgetConfigurationListViewModel
import com.example.fico.presentation.viewmodel.BudgetPerMonthViewModel
import com.example.fico.presentation.viewmodel.CreditCardConfigurationViewModel
import com.example.fico.presentation.viewmodel.EditTransactionViewModel
import com.example.fico.presentation.viewmodel.TransactionConfigurationViewModel
import com.example.fico.presentation.viewmodel.TransactionListViewModel
import com.example.fico.presentation.viewmodel.GeneralConfigurationViewModel
import com.example.fico.presentation.viewmodel.HomeAllBalanceViewModel
import com.example.fico.presentation.viewmodel.HomeAllExpensesViewModel
import com.example.fico.presentation.viewmodel.HomeMonthBalanceViewModel
import com.example.fico.presentation.viewmodel.HomeEarningsViewModel
import com.example.fico.presentation.viewmodel.HomeMonthExpensesViewModel
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.example.fico.presentation.viewmodel.LogoViewModel
import com.example.fico.presentation.viewmodel.MainViewModel
import com.example.fico.presentation.viewmodel.RegisterViewModel
import com.example.fico.presentation.viewmodel.ResetPasswordViewModel
import com.example.fico.presentation.viewmodel.SecurityConfigurationViewModel
import com.example.fico.presentation.viewmodel.SetDefaultBudgetViewModel
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.example.fico.presentation.viewmodel.VerifyEmailViewModel
import com.example.fico.presentation.viewmodel.shared.RemoteDatabaseViewModel
import com.example.fico.repositories.AuthRepository
import com.example.fico.repositories.CreditCardRepository
import com.example.fico.repositories.TransactionsRepository
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

    single<TransactionsInterface> { get<FirebaseAPI>() }
    single { TransactionsRepository(get()) }

    single<CreditCardInterface> { get<FirebaseAPI>() }
    single { CreditCardRepository(get()) }

    single<DataStoreManager>(){
        DataStoreManager(androidContext())
    }

    factory<HomeViewModel> {
        HomeViewModel()
    }

    factory<HomeMonthExpensesViewModel> {
        HomeMonthExpensesViewModel(
            dataStore = get(),
        )
    }

    factory<HomeMonthBalanceViewModel> {
        HomeMonthBalanceViewModel(
            dataStore = get(),
        )
    }

    factory<HomeAllBalanceViewModel> {
        HomeAllBalanceViewModel(
            dataStore = get(),
        )
    }

    factory<HomeAllExpensesViewModel> {
        HomeAllExpensesViewModel(
            dataStore = get(),
        )
    }

    factory<HomeEarningsViewModel> {
        HomeEarningsViewModel(
            dataStore = get(),
        )
    }

    factory<RemoteDatabaseViewModel> {
        RemoteDatabaseViewModel(
            firebaseAPI = get(),
            dataStore = get(),
            transactionsRepository = get(),
            creditCardRepository = get()
        )
    }

    factory<VerifyEmailViewModel> {
        VerifyEmailViewModel(
            firebaseAPI = get(),
            authRepository = get()
        )
    }

    factory<CreditCardViewModel> {
        CreditCardViewModel(
            dataStore = get(),
            creditCardRepository = get()
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

    factory<SecurityConfigurationViewModel> {
        SecurityConfigurationViewModel(
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
            authRepository = get()
        )
    }

    factory<LogoViewModel> {
        LogoViewModel(
            authRepository = get(),
            dataStore = get()
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

    factory<CreditCardConfigurationViewModel> {
        CreditCardConfigurationViewModel(
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

