package com.example.fico.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.model.Budget
import com.example.fico.interfaces.OnListItemClick

class BudgetPerMonthAdapter(private var data : List<Budget>) : RecyclerView.Adapter<BudgetPerMonthAdapter.ViewHolder>(){

    private var listener: OnListItemClick? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val budget : TextView = itemView.findViewById(R.id.tv_budget)
        val date : TextView = itemView.findViewById(R.id.tv_budget_per_month_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.budget_per_month_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.budget.text = item.budget
        holder.date.text = item.date

        holder.itemView.setOnClickListener {
            listener?.onListItemClick(position)
        }
    }

    fun updateList(newInputs: List<Budget>){
        data = newInputs
    }

    fun setOnItemClickListener(listener: OnListItemClick) {
        this.listener = listener
    }

}