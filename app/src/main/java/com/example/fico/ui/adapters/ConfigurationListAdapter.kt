package com.example.fico.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.ui.interfaces.OnConfigurationItemClick

class ConfigurationListAdapter(private var data: MutableList<String>) : RecyclerView.Adapter<ConfigurationListAdapter.ViewHolder>() {

    private var listener: OnConfigurationItemClick? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val configuration: TextView = itemView.findViewById(R.id.tv_configuration_list_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.configuration_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.configuration.text = item

        holder.itemView.setOnClickListener {
            listener?.onConfigurationItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setOnItemClickListener(listener: OnConfigurationItemClick) {
        this.listener = listener
    }

}