package com.exzell.mangaplayground.reader

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import timber.log.Timber

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

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Timber.d("Gesture detector reached with event $ev")
        super.onInterceptTouchEvent(ev)
        return true
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        mGestureDetector.onTouchEvent(ev)
        return super.onTouchEvent(ev)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearOnPageChangeListeners()
    }
}