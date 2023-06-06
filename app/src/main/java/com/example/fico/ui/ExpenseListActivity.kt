package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityExpenseListBinding

class ExpenseListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityExpenseListBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        binding.rvExpenseList.layoutManager = LinearLayoutManager(this)
    }
}