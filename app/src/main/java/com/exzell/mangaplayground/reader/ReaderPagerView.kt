package com.exzell.mangaplayground.reader

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.ViewPager

class ReaderPagerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewPager(context) {

    var onSingleTap: ((MotionEvent) -> Unit)? = null

    val gestureDetector = GestureDetectorCompat(context, SwipeListener(onSingleTap = {
        it?.let{onSingleTap?.invoke(it)}
        true
    }))

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("ReaderPagerView", "Touch event dispatched")
        val hand = super.dispatchTouchEvent(ev)
        gestureDetector.onTouchEvent(ev)
        return hand
    }
}