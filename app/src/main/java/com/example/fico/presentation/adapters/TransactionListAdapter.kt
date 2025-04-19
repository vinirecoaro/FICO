package com.example.fico.presentation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.Transaction
import com.example.fico.model.TransactionCategory
import com.example.fico.interfaces.OnListItemClick
import com.example.fico.utils.constants.StringConstants
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class TransactionListAdapter(private val expenseCategory : List<TransactionCategory>, private val earningCategory : List<TransactionCategory>) : RecyclerView.Adapter<TransactionListAdapter.ViewHolder>(){

    private var listener: OnListItemClick? = null
    private var transactionList = listOf<Transaction>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val description: TextView = itemView.findViewById(R.id.tv_description)
        val price: TextView = itemView.findViewById(R.id.tv_price)
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val categoryImg: ImageView = itemView.findViewById(R.id.iv_category_expense_item_list)
        val installmentField: TextView = itemView.findViewById(R.id.tv_installment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = transactionList[position]

        holder.description.text = item.description
        holder.installmentField.visibility = View.GONE

        val regex = Regex("[\\d,.]+")
        val justNumber = regex.find(item.price)
        val formatNum = DecimalFormat("#.##")
        val justNumberValue = justNumber!!.value.replace(",",".").toFloat()
        val formattedNum = formatNum.format(justNumberValue).replace(",",".").toFloat()
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        val formattedPrice = currencyFormat.format(formattedNum)
        holder.price.text = formattedPrice

        val itemCategory = item.category

        if(item.type == StringConstants.DATABASE.EXPENSE || item.type == StringConstants.DATABASE.RECURRING_EXPENSE){

            if(item.type == StringConstants.DATABASE.EXPENSE){
                holder.date.text = item.purchaseDate
            }else{
                if(item.purchaseDate != ""){
                    val text = "Dia - ${item.purchaseDate}"
                    holder.date.text = text
                }
            }

            if(item.description.contains("Parcela") || item.description.contains("parcela")){

                val numOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(item.id).replace("0","")
                val currentInstallment = FormatValuesFromDatabase().installmentExpenseCurrentInstallment(item.id).replace("0","")
                holder.installmentField.visibility = View.VISIBLE
                val installmentText = "Parcela $currentInstallment de $numOfInstallment"
                holder.installmentField.text = installmentText

                val descriptionFormatted = FormatValuesFromDatabase().installmentExpenseDescription(item.description)
                holder.description.text = descriptionFormatted

            }

            holder.price.setTextColor(Color.RED)

            val categoryPathName = expenseCategory.find{ it.description == itemCategory }
            if(categoryPathName != null){
                val iconName = categoryPathName.iconName
                val iconPath = holder.itemView.context.resources.getIdentifier(iconName, "drawable", holder.itemView.context.packageName)
                holder.categoryImg.setImageResource(iconPath)
            }else{
                val iconPath = holder.itemView.context.resources.getIdentifier("baseline_cancel_dark", "drawable", holder.itemView.context.packageName)
                holder.categoryImg.setImageResource(iconPath)
            }

            holder.itemView.setOnClickListener {
                listener?.onListItemClick(position)
            }
        }

        else if(item.type == StringConstants.DATABASE.EARNING || item.type == StringConstants.DATABASE.RECURRING_EARNING){

            holder.price.setTextColor(Color.GREEN)

            holder.date.text = item.purchaseDate

            val categoryPathName = earningCategory.find{ it.description == itemCategory }
            if(categoryPathName != null){
                val iconName = categoryPathName.iconName
                val iconPath = holder.itemView.context.resources.getIdentifier(iconName, "drawable", holder.itemView.context.packageName)
                holder.categoryImg.setImageResource(iconPath)
            }else{
                val iconPath = holder.itemView.context.resources.getIdentifier("baseline_cancel_dark", "drawable", holder.itemView.context.packageName)
                holder.categoryImg.setImageResource(iconPath)
            }

            holder.itemView.setOnClickListener {
                listener?.onListItemClick(position)
            }
        }

    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun setOnItemClickListener(listener: OnListItemClick) {
        this.listener = listener
    }

    fun getDataAtPosition(position: Int): Transaction {
        return transactionList[position]
    }

    fun updateTransactions(transactionsList : List<Transaction>){
        transactionList = transactionsList
        notifyDataSetChanged()
    }

}