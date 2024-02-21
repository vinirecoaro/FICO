package com.example.fico.ui.adapters

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.domain.model.ExpenseCategory
import com.example.fico.ui.interfaces.OnCategorySelectedListener


class CategoryListAdapter(
    private val categories : List<ExpenseCategory>,
    private val listener: OnCategorySelectedListener,
    ) : RecyclerView.Adapter<CategoryListAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val cardView : CardView = itemView.findViewById(R.id.cv_category)
        val border : ImageView = itemView.findViewById(R.id.iv_catergory_border)
        val description : TextView = itemView.findViewById(R.id.tv_category)
        val icon : ImageView = itemView.findViewById(R.id.iv_category)
    }

    private var selectedItemIndex: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_icon_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.description.text = category.description

        val iconPath = holder.itemView.context.resources.getIdentifier(category.iconName, "drawable", holder.itemView.context.packageName)
        holder.icon.setImageResource(iconPath)

        holder.border.visibility = if (position == selectedItemIndex) View.VISIBLE else View.GONE

        val theme = holder.itemView.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val cardBackgroundColor = if (theme == Configuration.UI_MODE_NIGHT_YES) {
            holder.border.setImageResource(R.drawable.category_item_background_selected_light)
        } else {
            holder.border.setImageResource(R.drawable.category_item_background_selected_black)
        }

        holder.cardView.setOnClickListener {
            selectedItemIndex = holder.adapterPosition
            notifyDataSetChanged()
            listener.onCategorySelected(category.description)
        }
    }

    fun clearCategorySelection(){
        selectedItemIndex = -1
        notifyDataSetChanged()
    }

    fun selectCategory(category : String){
        selectedItemIndex = categories.indexOfFirst { it.description == category}
        notifyDataSetChanged()
    }


}