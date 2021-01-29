package com.exzell.mangaplayground.reader

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat

class ReaderPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {


    var onSingleTap: ((MotionEvent) -> Boolean)? = null
    var onPageChanged: ((Int) -> Unit)? = null
    private val mGestureDetector: GestureDetectorCompat

    private val mPageListener: SimpleOnPageChangeListener = object : SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            onPageChanged?.invoke(position)
        }
    }

    constructor(context: Context) : this(context, null)

    init {
        addOnPageChangeListener(mPageListener)
        mGestureDetector = GestureDetectorCompat(context, createGestureListener())
    }

    private fun createGestureListener() = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?) = true

        override fun onSingleTapConfirmed(e: MotionEvent?) = onSingleTap?.invoke(e!!) ?: false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val parentTouch = mGestureDetector.onTouchEvent(ev)
        val childTouch = super.dispatchTouchEvent(ev)
        return parentTouch || childTouch
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearOnPageChangeListeners()
    }
}