package com.example.fico.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.ui.interfaces.OnExpenseMonthSelectedListener

class ExpenseMonthsListAdapter(private val expenseMonthList : List<String>) : RecyclerView.Adapter<ExpenseMonthsListAdapter.ViewHolder>() {

    private var listener: OnExpenseMonthSelectedListener? = null

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val cardView : CardView = itemView.findViewById(R.id.cv_expense_month)
        val date : TextView = itemView.findViewById(R.id.tv_expense_month)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_month_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return expenseMonthList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = expenseMonthList[position]
        holder.date.text = item
        holder.cardView.setOnClickListener {
            notifyDataSetChanged()
            listener?.onExpenseMonthSelected(item)
        }
    }

}