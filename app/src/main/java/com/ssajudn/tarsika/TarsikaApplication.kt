package com.ssajudn.tarsika

import android.app.Application
import com.ssajudn.tarsika.app.di.AppContainer

class TarsikaApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
