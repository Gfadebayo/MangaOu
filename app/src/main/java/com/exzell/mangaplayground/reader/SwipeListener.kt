package com.exzell.mangaplayground.reader

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeListener(private val onSwipe: ((Int) -> Unit)? = null,
                    private val onSingleTap: ((MotionEvent?) -> Boolean)? = null): GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapUp(e: MotionEvent?): Boolean {return onSingleTap?.invoke(e) ?: false}

    override fun onDown(e: MotionEvent?): Boolean {return true}

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return super.onDoubleTap(e)
    }
}