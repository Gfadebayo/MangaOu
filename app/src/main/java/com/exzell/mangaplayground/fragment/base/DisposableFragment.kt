package com.exzell.mangaplayground.fragment.base

import android.util.Log
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.disposables.Disposable

abstract class DisposableFragment: Fragment(){

    private val mToBeDisposed = arrayListOf<Disposable>()

    fun addDisposable(dispose: Disposable?){
        dispose?.let{ mToBeDisposed.add(it)}
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mToBeDisposed.forEach {
            if (!it.isDisposed) it.dispose()
        }
        mToBeDisposed.clear()
    }
}