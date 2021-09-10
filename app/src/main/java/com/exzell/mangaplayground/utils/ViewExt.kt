package com.exzell.mangaplayground.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.drawable.LevelListDrawable
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.toggleVisibility(toVisible: Boolean) {

    if (translationY == 0f && toVisible) return
    var translateDistance = height.toFloat()
    if (toVisible) translateDistance = -translateDistance

    animate().apply {
        translationYBy(translateDistance)

        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!toVisible) visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animator) {
                if (toVisible) visibility = View.VISIBLE
            }
        })

        start()
    }
}

fun TextView.rotateDrawable(fromLevel: Int, toLevel: Int) {
    for (drawable in compoundDrawablesRelative) {
        if (drawable != null && drawable is LevelListDrawable) {
            with(drawable) {
                ObjectAnimator.ofInt(this, "level", fromLevel, toLevel)
                        .setDuration(300L).start()
            }
        }
    }
}