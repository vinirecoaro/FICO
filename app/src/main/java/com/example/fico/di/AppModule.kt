package com.example.fico.di

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.fico.api.FirebaseAPI
import com.example.fico.presentation.viewmodel.AddExpenseViewModel
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.example.fico.presentation.viewmodel.ExpenseListViewModel
import com.example.fico.presentation.viewmodel.GeneralConfigurationViewModel
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.example.fico.presentation.viewmodel.LogoViewModel
import com.example.fico.presentation.viewmodel.RegisterViewModel
import com.example.fico.presentation.viewmodel.ResetPasswordViewModel
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.example.fico.presentation.viewmodel.VerifyEmailViewModel
import org.koin.dsl.module

@RequiresApi(Build.VERSION_CODES.N)
val appModule = module {

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
            firebaseAPI = get()
        )
    }

    factory<HomeViewModel> {
            HomeViewModel(
                firebaseAPI = get()
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
        )
    }

    factory<FirebaseAPI>{
        FirebaseAPI.instance
    }

}

