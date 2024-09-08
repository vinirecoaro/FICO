package com.example.fico.presentation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.model.Expense
import com.example.fico.model.TransactionCategory
import com.example.fico.presentation.interfaces.OnListItemClick
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ExpenseListAdapter(private var data: List<Expense>, private val categories : List<TransactionCategory>) : RecyclerView.Adapter<ExpenseListAdapter.ViewHolder>(){

    private var listener: OnListItemClick? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val description: TextView = itemView.findViewById(R.id.tv_description)
        val price: TextView = itemView.findViewById(R.id.tv_price)
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val categoryImg: ImageView = itemView.findViewById(R.id.iv_category_expense_item_list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.description.text = item.description

        val regex = Regex("[\\d,.]+")
        val justNumber = regex.find(item.price)
        val formatNum = DecimalFormat("#.##")
        val justNumberValue = justNumber!!.value.replace(",",".").toFloat()
        val formattedNum = formatNum.format(justNumberValue).replace(",",".").toFloat()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val formattedPrice = currencyFormat.format(formattedNum)
        holder.price.text = formattedPrice
        holder.price.setTextColor(Color.RED)

        holder.date.text = item.paymentDate

        val itemCategory = item.category
        val categoryPathName = categories.find{ it.description == itemCategory }
        val iconName = categoryPathName!!.iconName
        val iconPath = holder.itemView.context.resources.getIdentifier(iconName, "drawable", holder.itemView.context.packageName)
        holder.categoryImg.setImageResource(iconPath)

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

    fun getDataAtPosition(position: Int): Expense {
        return data[position]
    }

}