package com.jax.uevent

import android.app.Application
import timber.log.Timber

/**
 * Created by Jax on 2019-12-25.
 * Description :
 * Version : V1.0.0
 */
class UEventApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}