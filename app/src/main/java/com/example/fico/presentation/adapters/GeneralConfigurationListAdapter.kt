package com.example.fico.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.interfaces.OnListItemClick

class GeneralConfigurationListAdapter(private val data : MutableList<String>) : RecyclerView.Adapter<GeneralConfigurationListAdapter.ViewHolder>() {

    private var listener: OnListItemClick? = null

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val configuration : TextView = itemView.findViewById(R.id.tv_configuration_list_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.configuration_list_item, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.configuration.text = item

        holder.itemView.setOnClickListener {
            listener?.onListItemClick(position)
        }
    }

    fun setOnItemClickListener(listener: OnListItemClick) {
        this.listener = listener
    }
}