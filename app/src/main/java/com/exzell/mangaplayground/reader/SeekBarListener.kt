package com.exzell.mangaplayground.reader

import android.util.Log
import android.widget.SeekBar
import com.exzell.mangaplayground.models.Chapter
import kotlin.math.abs

/**
 * A wrapper class around the Seekbar listener interface for the seekbar used in the reader.
 * Before the progress is changed, the current chapter is stored and used instead of directly
 * getting the chapter from the adapter since the current chapter the adapter is working with
 * can change due to the offscreen page limit set.
 */
class SeekBarListener(val mAdapter: ReaderAdapter, private val onProgressResolved: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {

    private var mSeekChapter: Chapter? = null


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (!fromUser) return

        mSeekChapter?.let{
            var value = abs((progress * it.length - 1) / seekBar!!.max) % it.length


            value += it.offset

            onProgressResolved.invoke(value)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        mSeekChapter = mAdapter.mCurrentChapter
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) { mSeekChapter = null}
}