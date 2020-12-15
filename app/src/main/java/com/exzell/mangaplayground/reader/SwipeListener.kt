package com.exzell.mangaplayground.reader

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeListener(private val onSwipe: ((Int) -> Unit)? = null,
                    private val onSingleTap: ((MotionEvent?) -> Boolean)? = null): GestureDetector.OnGestureListener {

    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {return onSingleTap?.invoke(e) ?: false}

    override fun onDown(e: MotionEvent?): Boolean {return false}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

        if(abs(velocityX) < VELOCITY_THRESHOLD && abs(velocityY) < VELOCITY_THRESHOLD) return false

        if(abs(velocityX) > abs(velocityY)){

            if(velocityX >= 0) onSwipe?.invoke(SWIPE_RIGHT)
            else onSwipe?.invoke(SWIPE_LEFT)

        }else{

            if(velocityY >= 0) onSwipe?.invoke(SWIPE_DOWN)
            else onSwipe?.invoke(SWIPE_UP)
        }

        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {return false}

    override fun onLongPress(e: MotionEvent?) {}

    companion object{
        val VELOCITY_THRESHOLD = 300

        const val SWIPE_LEFT = 0
        const val SWIPE_RIGHT = 1

        const val SWIPE_UP = 3
        const val SWIPE_DOWN = 4
    }


}