package com.example.fico

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidContext(this@MyApp)
            modules(appModule)
        }
    }

}