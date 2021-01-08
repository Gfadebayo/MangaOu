package com.exzell.mangaplayground.customview

import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import com.exzell.mangaplayground.R

class VisibilityGroup(vararg view: View, val decorView: View? = null) {

    private val viewList = ArrayList(view.asList())

//TODO: Fix Visibility Group
    private fun show(){
        viewList.forEach {
            it.visibility = View.VISIBLE
        }
        decorView?.let{it.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)}
    }

    private fun hide(){
        viewList.forEach {
            it.visibility = View.GONE
            animate(it, false)
        }
        decorView?.let {
            it.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun apply(){
        //hidden
        if(decorView != null && decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION > 0) show()
        else hide()
    }

    fun clear(){
        viewList.clear()
    }

    private fun animate(view: View, show: Boolean){
        AnimationUtils.loadAnimation(view.context, R.anim.enter_from_bottom)
    }
}