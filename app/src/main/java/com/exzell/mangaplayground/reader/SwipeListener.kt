package com.exzell.mangaplayground.reader

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeListener(private val onSwipe: ((Int) -> Unit)? = null,
                    private val onSingleTap: ((MotionEvent?) -> Boolean)? = null): GestureDetector.OnGestureListener {

    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {return onSingleTap?.invoke(e) ?: false}

    override fun onDown(e: MotionEvent?): Boolean {return true}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {return false}

    override fun onLongPress(e: MotionEvent?) {}
}