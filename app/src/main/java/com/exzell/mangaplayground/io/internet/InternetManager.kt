package com.exzell.mangaplayground.io.internet

import android.content.Context
import com.exzell.mangaplayground.BuildConfig
import dagger.Module
import dagger.Provides
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object InternetManager {

    const val mBaseUrl = "https://mangapark.net/"

    @JvmField
    val mClient = createClient(false, null)

    @JvmStatic
    @Singleton
    @Provides
    fun getApi(context: Context): Retrofit{

        val client = createClient(true, context)

        return Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
    }

    private fun createClient(withCache: Boolean, context: Context?): OkHttpClient{
        return OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(if(withCache) createCache(context!!) else null)
                .retryOnConnectionFailure(true).build()

    }

    private fun createCache(context: Context): Cache {
        val dir = if (BuildConfig.DEBUG)
            context.getExternalFilesDir("response_cache")
        else
            context.getDir("response_cache", Context.MODE_PRIVATE)
        return Cache(dir!!, 3L * 1000 * 1000)
    }
}