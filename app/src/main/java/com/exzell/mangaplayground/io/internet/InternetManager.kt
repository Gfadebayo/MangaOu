package com.exzell.mangaplayground.io.internet

import android.content.Context
import com.exzell.mangaplayground.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object InternetManager {

    const val mBaseUrl = "https://mangapark.net/"

    @JvmField
    val mClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true).build()

    @JvmStatic
    @Singleton
    @Provides
    fun getApi(context: Context): Retrofit{

        val client = mClient.newBuilder().cache(createCache(context)).build()

        return Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
    }

    private fun createCache(context: Context): Cache {
        val dir = if (BuildConfig.DEBUG)
            context.getExternalFilesDir("response_cache")
        else
            context.getDir("response_cache", Context.MODE_PRIVATE)
        return Cache(dir!!, 3L * 1000 * 1000)
    }
}