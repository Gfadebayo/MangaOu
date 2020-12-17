package com.exzell.mangaplayground

import android.app.Application
import com.exzell.mangaplayground.di.AppComponent
import com.exzell.mangaplayground.di.DaggerAppComponent
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
                .build();
    }
}