package com.example.fico.model

data class UserData(
    val totalExpense: String,
    val expenseList: List<HashMap<String,List<HashMap<String,List<HashMap<String, String>>>>>>,
    val informationPerMonth : List<HashMap<String, List<HashMap<String, String>>>>
) {
}