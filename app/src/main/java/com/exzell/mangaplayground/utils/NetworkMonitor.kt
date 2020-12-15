package com.exzell.mangaplayground.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun Context.isConnectedToNetwork(): Boolean{
    val connectivityManager = getSystemService(ConnectivityManager::class.java)

    if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
        val net = connectivityManager.activeNetwork ?: return false
        val cap = connectivityManager.getNetworkCapabilities(net) ?: return false

        return when{
            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            cap.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }else return connectivityManager.activeNetworkInfo?.isConnected ?: false
}