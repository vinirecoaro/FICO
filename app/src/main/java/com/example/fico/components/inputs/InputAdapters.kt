package com.example.fico.components.inputs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.example.fico.R
import com.example.fico.model.CreditCardColors

class InputAdapters {
    companion object{

        fun colorAutoCompleteTextInputLayout(context : Context, colorOptions : List<CreditCardColors>): ArrayAdapter<CreditCardColors> {
            val adapter = object : ArrayAdapter<CreditCardColors>(
                context,
                R.layout.item_color_option,
                colorOptions
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context)
                        .inflate(R.layout.item_color_option, parent, false)

                    val colorCircle = view.findViewById<View>(R.id.color_circle)
                    val colorName = view.findViewById<TextView>(R.id.color_name)

                    val item = getItem(position)
                    item?.let {
                        // Aplicar a cor ao background do círculo
                        val drawable = AppCompatResources.getDrawable(context, R.drawable.circle_shape)?.mutate()
                        drawable?.setTint(it.backgroundColor)
                        colorCircle.background = drawable

                        colorName.text = context.getString(it.backgroundColorNameRes)
                    }

                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    return getView(position, convertView, parent)
                }
            }
            return adapter
        }
    }
}