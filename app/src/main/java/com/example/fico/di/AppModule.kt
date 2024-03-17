package com.example.fico.di

import androidx.room.Room
import com.example.fico.api.FirebaseAPI
import com.example.fico.data.AppDatabase
import com.example.fico.data.dao.ExpenseDao
import com.example.fico.data.repository.ExpenseRepositoryImpl
import com.example.fico.domain.usecase.GetAllExpensesUseCase
import com.example.fico.domain.usecase.InsertExpenseUseCase
import com.example.fico.presentation.viewmodel.AddExpenseViewModel
import com.example.fico.presentation.viewmodel.ExpenseListViewModel
import com.example.fico.presentation.viewmodel.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

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

    factory<AddExpenseViewModel> {
        AddExpenseViewModel(
            firebaseAPI = get(),
            getAllExpensesUseCase = get(),
            insertExpenseUseCase = get()
        )
    }

    factory<FirebaseAPI>{
        FirebaseAPI.instance
    }

    factory<GetAllExpensesUseCase>{
        GetAllExpensesUseCase(
            get<ExpenseRepositoryImpl>()
        )
    }

    factory<InsertExpenseUseCase>{
        InsertExpenseUseCase(
            get<ExpenseRepositoryImpl>()
        )
    }

    factory<ExpenseRepositoryImpl>{
        ExpenseRepositoryImpl(
            dao = get<AppDatabase>().expenseDao()
        )
    }

    single<AppDatabase>{
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "database-expense"
        ).build()
    }
}

