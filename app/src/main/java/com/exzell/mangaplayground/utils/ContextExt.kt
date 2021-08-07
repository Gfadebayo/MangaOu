package com.exzell.mangaplayground.utils

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.IdRes
import androidx.navigation.NavDeepLinkBuilder
import com.exzell.mangaplayground.MainActivity
import com.exzell.mangaplayground.R


fun Context.createNavPendingIntent(@IdRes destination: Int, extra: Bundle?) = NavDeepLinkBuilder(this).let {
    it.setComponentName(MainActivity::class.java)
    it.setArguments(extra)
    it.setGraph(R.navigation.nav_graph)
    it.setDestination(destination)

    it.createPendingIntent()
}

fun Context.getPrimaryColor(): Int {
    val value = TypedValue()
    theme.resolveAttribute(R.attr.colorPrimary, value, true)
    return value.data
}

fun Context.getSecondaryColor(): Int {
    val value = TypedValue()
    theme.resolveAttribute(R.attr.colorSecondary, value, true)
    return value.data
}