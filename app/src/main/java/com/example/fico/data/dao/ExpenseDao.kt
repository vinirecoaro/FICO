package com.example.fico.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fico.data.model.ExpenseEntity


@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense")
    fun getAll() : List<ExpenseEntity>

    @Insert
    fun insert(expense : ExpenseEntity)

}