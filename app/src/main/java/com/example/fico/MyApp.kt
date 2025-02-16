package com.example.fico

import NetworkConnectionLiveData
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.api.FirebaseAPI
import com.example.fico.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application() {

    private lateinit var internetConnection: NetworkConnectionLiveData
    private val firebaseAPI : FirebaseAPI by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidContext(this@MyApp)
            modules(appModule)
        }

        internetConnection = NetworkConnectionLiveData(this)

        setUpListeners()
    }

    private fun setUpListeners(){
        CoroutineScope(Dispatchers.Main).launch {
            internetConnection.isConnected.collectLatest { isConnected ->
                if (isConnected) {
                    firebaseAPI.updateReferences()
                }
            }
        }
    }

}