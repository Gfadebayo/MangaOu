package com.exzell.mangaplayground.fragment.base


import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.reactivex.rxjava3.disposables.Disposable

/**
 * A Fragment class that helps in disposing of [io.reactivex.rxjava3.disposables.Disposable]
 * in order not to make things look more complex, the class also handles the SwipeRefreshLayout issues
 */
abstract class DisposableFragment : Fragment() {

    private val mToBeDisposed = arrayListOf<Disposable>()

    private var mSwipeRefresh: SwipeRefreshLayout? = null

    private var mChild: View? = null

    private var mIsFingerDown = false

    private val mRecyclerScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        var currentScrollChange = 0
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            currentScrollChange += dy

            mSwipeRefresh!!.isEnabled = currentScrollChange == 0 //&& !mIsFingerDown
        }
    }

    private val mScrollChangedListener = object : ViewTreeObserver.OnScrollChangedListener {
        override fun onScrollChanged() {
            if (!isVisible) return

//            val scrollY = mSwipeRefresh!!.getChildAt(0).scrollY
            val scrollY = mChild!!.scrollY
            mSwipeRefresh!!.isEnabled = scrollY == 0 //&& !mIsFingerDown
        }
    }

    fun addDisposable(dispose: Disposable?) {
        dispose?.let { mToBeDisposed.add(it) }
    }

    override fun onDestroyView() {
        mToBeDisposed.forEach {
            if (!it.isDisposed) it.dispose()
        }
        mToBeDisposed.clear()


        mSwipeRefresh?.let {
            it.viewTreeObserver!!.removeOnScrollChangedListener(mScrollChangedListener)
            it.setOnTouchListener(null)
        }
        mSwipeRefresh = null


        super.onDestroyView()
    }


    fun setSwipeRefreshView(swipe: SwipeRefreshLayout, scrollChild: View) {
        mSwipeRefresh = swipe
        mChild = scrollChild
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSwipeRefresh?.let {

            if (mChild is RecyclerView) (mChild as RecyclerView).addOnScrollListener(mRecyclerScrollListener)
            else it.viewTreeObserver.addOnScrollChangedListener(mScrollChangedListener)

//            it.setOnTouchListener { _, event ->
//
//                mIsFingerDown = !(event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL)
//                true
//            }
        }
    }
}