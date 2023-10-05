package com.example.fico.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.model.Expense
import com.example.fico.ui.interfaces.OnListItemClick

class ExpenseListAdapter(private var data: List<Expense>) : RecyclerView.Adapter<ExpenseListAdapter.ViewHolder>(){

    private var listener: OnListItemClick? = null

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

        holder.itemView.setOnClickListener {
            listener?.onListItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateExpenses(newExpenses: List<Expense>){
        data = newExpenses
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnListItemClick) {
        this.listener = listener
    }

    fun removeItem(position: Int) {
        //data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getDataAtPosition(position: Int): Expense {
        return data[position]
    }

}