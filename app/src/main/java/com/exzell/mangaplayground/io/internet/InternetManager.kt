package com.exzell.mangaplayground.io.internet

import android.content.Context
import com.exzell.mangaplayground.BuildConfig
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object InternetManager {

    const val mBaseUrl = "https://mangapark.net/"

    @JvmField
    val mClient = createClient(false, null)

    @JvmStatic
    fun getApi(context: Context): MangaParkApi{

        val client = createClient(true, context)

        val fit = Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()

        return fit.create(MangaParkApi::class.java)
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