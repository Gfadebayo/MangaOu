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

    val baseUrl = "https://v2.mangapark.net/"

    @JvmField
    val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true).build()


    private var retrofit: Retrofit? = null

    @Singleton
    @Provides
    fun getApi(context: Context): Retrofit {
        if (retrofit != null) return retrofit!!

        val client = client.newBuilder()
                .cache(createCache(context)).build()

        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build().apply { retrofit = this }
    }

    private fun createCache(context: Context): Cache {
        val dir = if (BuildConfig.DEBUG)
            context.getExternalFilesDir("response_cache")
        else
            context.getDir("response_cache", Context.MODE_PRIVATE)
        return Cache(dir!!, 3L * 1000 * 1000)
    }
}