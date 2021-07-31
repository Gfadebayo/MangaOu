package com.exzell.mangaplayground.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.toggleVisibility(visible: Boolean) {

    if (translationY == 0f && visible) return
    var translateDistance: Float = height.toFloat()
    if (visible) translateDistance = -translateDistance

    animate().apply {
        translationYBy(translateDistance)

        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!visible) setVisibility(View.GONE)
            }

            override fun onAnimationStart(animation: Animator) {
                if (visible) setVisibility(View.VISIBLE)
            }
        })

        start()
    }
}