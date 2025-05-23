package com.example.fico.utils.ui_personalizations

import android.view.View

class Effects {
    companion object{
        fun View.fadeIn(duration: Long = 200) {
            this.alpha = 0f
            this.visibility = View.VISIBLE
            this.animate()
                .alpha(1f)
                .setDuration(duration)
                .start()
        }

        fun View.fadeOut(duration: Long = 200) {
            this.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction { this.visibility = View.GONE }
                .start()
        }
    }
}