package com.exzell.mangaplayground.fragment.base


import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * A Fragment class that helps in disposing of [io.reactivex.rxjava3.disposables.Disposable]
 * in order not to make things look more complex, the class also handles the SwipeRefreshLayout issues
 */
abstract class SwipeRefreshFragment : Fragment() {

    private var mSwipeRefresh: SwipeRefreshLayout? = null

    private var mChild: View? = null

    private var mIsFingerDown = false

    private var mRecyclerScrollListener: RecyclerView.OnScrollListener? = object : RecyclerView.OnScrollListener() {
        var currentScrollChange = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            currentScrollChange += dy

            mSwipeRefresh!!.isEnabled = currentScrollChange == 0 //&& !mIsFingerDown
        }
    }

    private var mScrollChangedListener: ViewTreeObserver.OnScrollChangedListener? = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            if (!isVisible) return

//            val scrollY = mSwipeRefresh!!.getChildAt(0).scrollY
            val scrollY = mChild!!.scrollY
            mSwipeRefresh!!.isEnabled = scrollY == 0 //&& !mIsFingerDown
        }
    }

    fun setSwipeRefreshView(swipe: SwipeRefreshLayout, scrollChild: View) {
        mSwipeRefresh = swipe
        mChild = scrollChild
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSwipeRefresh?.let {

            if (mChild is RecyclerView) (mChild as RecyclerView).addOnScrollListener(mRecyclerScrollListener!!)
            else it.viewTreeObserver.addOnScrollChangedListener(mScrollChangedListener)

        }
    }

    override fun onDestroyView() {
        mSwipeRefresh?.let {
            it.viewTreeObserver!!.removeOnScrollChangedListener(mScrollChangedListener)
            it.setOnTouchListener(null)
        }

        if (mChild is RecyclerView) (mChild as RecyclerView).removeOnScrollListener(mRecyclerScrollListener!!)
        super.onDestroyView()

        mSwipeRefresh = null
        mChild = null
        mScrollChangedListener = null
//        mRecyclerScrollListener = null
    }
}