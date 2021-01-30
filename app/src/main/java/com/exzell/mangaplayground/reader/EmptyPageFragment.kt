package com.exzell.mangaplayground.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.exzell.mangaplayground.R

class EmptyPageFragment: Fragment() {

    companion object{
        private const val CURRENT_CHAP = "current chapter"
        private const val NEXT_CHAP = "next chapter"

        fun getInstance(current: String, next: String): EmptyPageFragment {

            val bund = Bundle(2).apply {
                putString(CURRENT_CHAP, current)
                putString(NEXT_CHAP, next)
            }

            return EmptyPageFragment().apply {
                arguments = bund
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.empty_reader_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Try to set the offscreen limit to 1 so we wont start loading the next chapter without
        //the users consent
//        val parent = view.parent as View
//        if(parent is ViewPager2) parent.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
    }

    override fun onDestroyView() {
        super.onDestroyView()

//        val parent = view!!.parent as View
//        if(parent is ViewPager2) parent.offscreenPageLimit = 2
    }
}