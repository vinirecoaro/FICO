package com.example.fico.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.model.Expense

class ExpenseListAdapter(private val data: List<Expense>) : RecyclerView.Adapter<ExpenseListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val description: TextView = itemView.findViewById(R.id.tv_description)
        val price: TextView = itemView.findViewById(R.id.tv_price)
        val date: TextView = itemView.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.description.text = item.description
        holder.price.text = item.price
        holder.date.text = item.date
    }

    override fun getItemCount(): Int {
        return data.size
    }

}