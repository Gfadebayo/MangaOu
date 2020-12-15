package com.exzell.mangaplayground.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.ActionMenuView
import androidx.annotation.MenuRes
import com.exzell.mangaplayground.R
import com.google.android.material.card.MaterialCardView

class BottomCab @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val mActionView: ActionMenuView

    init {
        View.inflate(context, R.layout.bottom_toolbar, this)

        mActionView = findViewById(R.id.bottom_menu)
    }

    fun show(@MenuRes res: Int, inflater: MenuInflater, listener: (MenuItem) -> Boolean){
        if(mActionView.menu.size() <= 0){
            inflater.inflate(res, mActionView.menu)
            mActionView.hideOverflowMenu()
            mActionView.setOnMenuItemClickListener(listener)
        }

        animate(true)
    }

    fun hide(){
        animate(false)
    }

    fun clearMenu(){
        mActionView.menu.clear()
        mActionView.setOnMenuItemClickListener(null)
    }

    private fun animate(appearance: Boolean){

        val anim = AnimationUtils.loadAnimation(context, if(appearance) R.anim.enter_from_bottom else R.anim.exit_to_bottom).apply {
            setAnimationListener(object: Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    if(appearance) visibility = View.VISIBLE
                    else visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {}
            })
        }
        if(appearance) {
            startAnimation(anim)
        }else{
            startAnimation(anim)
        }
    }
}