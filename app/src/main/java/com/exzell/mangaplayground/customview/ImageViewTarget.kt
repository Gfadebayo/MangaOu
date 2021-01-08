package com.exzell.mangaplayground.customview

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.request.target.ImageViewTarget

import com.bumptech.glide.request.transition.Transition
import com.google.android.material.textview.MaterialTextView

class ImageViewTarget(private val mImageView: ImageView,
                      private val mErrorText: MaterialTextView? = null,
                      private val mProgress: ProgressBar? = null): ImageViewTarget<Drawable>(mImageView) {

    override fun setResource(resource: Drawable?) {
        mImageView.setImageDrawable(resource)
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {

        mProgress?.visibility = View.GONE
        mErrorText?.visibility = View.VISIBLE
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        mProgress?.visibility = View.GONE
        mErrorText?.visibility = View.GONE

        mImageView.visibility = View.VISIBLE

        mImageView.setImageDrawable(resource)
    }

    override fun onLoadStarted(placeholder: Drawable?) {
        super.onLoadStarted(placeholder)

        mProgress?.visibility = View.VISIBLE
    }
}