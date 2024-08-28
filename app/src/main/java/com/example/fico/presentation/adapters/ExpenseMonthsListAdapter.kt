package com.example.fico.presentation.adapters

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.presentation.interfaces.OnExpenseMonthSelectedListener

class ExpenseMonthsListAdapter(private val context : Context, private var expenseMonthList : List<String>) : RecyclerView.Adapter<ExpenseMonthsListAdapter.ViewHolder>() {

    private var listener: OnExpenseMonthSelectedListener? = null
    private var selectedItemIndex: Int = -1

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


        val theme = holder.itemView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (theme == Configuration.UI_MODE_NIGHT_YES) {
            if (position == selectedItemIndex){
                holder.cardView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rounded_month_expense_corner_dark_mode_selected, null)
            }else{
                holder.cardView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rounded_month_expense_corner_dark_mode, null)
            }
        }else{
            if (position == selectedItemIndex){
                holder.cardView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rounded_month_expense_corner_light_mode_selected, null)
            }else{
                holder.cardView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rounded_month_expense_corner_light_mode, null)
            }
        }

        holder.cardView.setOnClickListener {
            selectedItemIndex = holder.adapterPosition
            notifyDataSetChanged()
            listener?.onExpenseMonthSelected(item)
        }
    }

    fun updateExpenseMonths(newExpenseMonths: List<String>){
        expenseMonthList = newExpenseMonths
        notifyDataSetChanged()
    }

    fun selectItem(position : Int){
        selectedItemIndex = position
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnExpenseMonthSelectedListener) {
        this.listener = listener
    }

}