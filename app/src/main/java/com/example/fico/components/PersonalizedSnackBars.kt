package com.example.fico.components

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.example.fico.R
import com.google.android.material.snackbar.Snackbar

class PersonalizedSnackBars {

    companion object {

        @RequiresApi(Build.VERSION_CODES.M)
        fun noInternetConnection(view: View, activity: Activity): Snackbar {
            return Snackbar.make(
                view,
                activity.getString(R.string.without_network_connection),
                Snackbar.LENGTH_LONG
            )
                .setBackgroundTint(activity.resources.getColor(android.R.color.holo_red_dark, activity.theme))
                .setTextColor(activity.resources.getColor(android.R.color.white, activity.theme))
        }

        fun fillField(activity : Activity, viewFromActivity: View, fieldName : String){
            Snackbar.make(
                viewFromActivity,
                "${activity.getString(R.string.fill_field)} $fieldName",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
