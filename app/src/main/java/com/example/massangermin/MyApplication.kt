// MyApplication.kt
package com.example.massangermin

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        private const val TAG = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Приложение запущено")

    }

    override fun onTerminate() {
        Log.d(TAG, "Приложение завершено")
        super.onTerminate()
    }
}