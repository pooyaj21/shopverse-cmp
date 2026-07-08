package com.shopverse.cmp

import android.app.Application
import android.content.Context

class ShopVerseApplication : Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
