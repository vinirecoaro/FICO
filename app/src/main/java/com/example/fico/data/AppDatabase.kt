package com.example.fico.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fico.data.dao.ExpenseDao
import com.example.fico.data.model.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao() : ExpenseDao
}