package com.exzell.mangaplayground

import android.app.Application
import com.exzell.mangaplayground.di.AppComponent
import com.exzell.mangaplayground.di.DaggerAppComponent
import com.exzell.mangaplayground.notification.Notifications.createChannels
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import timber.log.Timber

class MangaApplication: Application() {

    lateinit var mAppComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        mAppComponent = DaggerAppComponent.builder()
                .bindActivity(this)
                .bindContext(applicationContext)
                .bindExecutors(AppExecutors())
                .build()

        createChannels(this)

//        AppWatcher.config = AppWatcher.config.copy(enabled = false)
//        LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)
//        LeakCanary.showLeakDisplayActivityLauncherIcon(false)
    }
}