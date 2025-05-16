package com.example.fico.presentation.adapters

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.interfaces.OnExpenseMonthSelectedListener
import com.example.fico.utils.DateFunctions

class MonthsForHorizontalRecyclerViewAdapter(private val context : Context, private var monthsList : List<String>) : RecyclerView.Adapter<MonthsForHorizontalRecyclerViewAdapter.ViewHolder>() {

    private var listener: OnExpenseMonthSelectedListener? = null
    private var selectedItemIndex: Int = -1

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val cardView : CardView = itemView.findViewById(R.id.cv_month_for_horizontal_recycler_view)
        val date : TextView = itemView.findViewById(R.id.tv_month_for_horizontal_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month_for_horizontal_recycler_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return monthsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = monthsList[position]
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
        monthsList = newExpenseMonths
        notifyDataSetChanged()
    }

    fun selectItem(position : Int){
        selectedItemIndex = position
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnExpenseMonthSelectedListener) {
        this.listener = listener
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun focusOnCurrentMonth(recyclerView : RecyclerView){
        val currentDate = DateFunctions().getCurrentlyDateYearMonthToDatabase()
        val currentDateFormatted = FormatValuesFromDatabase().formatDateForFilterOnExpenseList(currentDate)
        val monthFocusPosition = getCurrentMonthPositionOnList(currentDateFormatted)
        if(monthFocusPosition != RecyclerView.NO_POSITION){
            recyclerView.scrollToPosition(monthFocusPosition)
        }
        selectItem(monthFocusPosition)
    }

    private fun getCurrentMonthPositionOnList(date : String) : Int{
        monthsList.let{
            val position = it.indexOf(date)
            return if (position != -1){
                position
            }else{
                RecyclerView.NO_POSITION
            }
        }
        return RecyclerView.NO_POSITION
    }

}