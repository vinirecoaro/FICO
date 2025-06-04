package com.example.fico.components.inputs

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.View

class InputValueHandle {
    companion object{

        fun circleColorfulWithText(autoCompleteTextView : View, selectedColor : Int, colorName : String): SpannableString {

            // Create a colored circle as a drawable
            val circleDrawable = createColorCircleDrawable(autoCompleteTextView.context, selectedColor)

            // Create a SpannableString with placeholder for the circle
            val spannable = SpannableString("  $colorName") // espaço inicial para o ícone

            // Set drawable size and position
            circleDrawable.setBounds(0, 0, circleDrawable.intrinsicWidth, circleDrawable.intrinsicHeight)

            // Apply ImageSpan to the first character
            val imageSpan = ImageSpan(circleDrawable, ImageSpan.ALIGN_BOTTOM)
            spannable.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannable
        }

        private fun createColorCircleDrawable(context: Context, color: Int, sizeDp: Int = 16): Drawable {
            val sizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeDp.toFloat(), context.resources.displayMetrics
            ).toInt()

            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setSize(sizePx, sizePx)
            }
            return shape
        }
    }
}