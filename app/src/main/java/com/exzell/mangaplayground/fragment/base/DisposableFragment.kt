package com.exzell.mangaplayground.fragment.base


import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.exzell.mangaplayground.R
import io.reactivex.rxjava3.disposables.Disposable

/**
 * A Fragment class that helps in disposing of [io.reactivex.rxjava3.disposables.Disposable]
 * in order not to make things look more complex, the class also handles the SwipeRefreshLayout issues
 */
abstract class DisposableFragment: Fragment(){

    private val mToBeDisposed = arrayListOf<Disposable>()

    private var mSwipeRefresh: SwipeRefreshLayout? = null

    private val mScrollChangedListener = object: ViewTreeObserver.OnScrollChangedListener{
        override fun onScrollChanged() {
            if(!isVisible) return

            val toolbarPos = activity!!.findViewById<View>(R.id.toolbar).y
            mSwipeRefresh?.let{it.isEnabled = toolbarPos == 0f}
        }
    }

    fun addDisposable(dispose: Disposable?){
        dispose?.let{ mToBeDisposed.add(it)}
    }

    override fun onDestroyView() {
        mToBeDisposed.forEach {
            if (!it.isDisposed) it.dispose()
        }
        mToBeDisposed.clear()

//
//        mSwipeRefresh?.let{
//            it.viewTreeObserver!!.removeOnScrollChangedListener(mScrollChangedListener)
//            it.setOnTouchListener(null)
//        }
//        mSwipeRefresh = null
//
        super.onDestroyView()
    }



    fun setSwipeRefreshView(swipe: SwipeRefreshLayout){
        mSwipeRefresh = swipe
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSwipeRefresh?.let{
            it.viewTreeObserver!!.addOnScrollChangedListener(mScrollChangedListener)
            it.setOnTouchListener { _, event ->
                Toast.makeText(requireContext(), "Swipe Refresh Touch called", Toast.LENGTH_SHORT).show()
                when (event.actionMasked){
                    MotionEvent.ACTION_MOVE -> it.isEnabled = activity!!.findViewById<View>(R.id.toolbar).y == 0f
                    MotionEvent.ACTION_UP -> it.isEnabled = true
                }
                true
            }
        }
    }
}