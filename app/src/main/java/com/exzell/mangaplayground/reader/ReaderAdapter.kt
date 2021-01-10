package com.exzell.mangaplayground.reader

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.utils.getDownloadFolder
import java.io.File

class ReaderAdapter(private val mActivity: FragmentActivity,
                    private val mManga: Manga,
                    chapter: Chapter): FragmentStatePagerAdapter(mActivity.supportFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var mCurrentChapter = chapter
    private set

    private var mFilePath = downloadPath(mCurrentChapter)

    var mPrevChapter: Chapter?
    private set

    var mNextChapter: Chapter?
    private set

    init {

        mPrevChapter = mManga.chapters.singleOrNull { it.version == mCurrentChapter.version && it.position == mCurrentChapter.position-1 }

        mNextChapter = mManga.chapters.singleOrNull { it.version == mCurrentChapter.version && it.position == mCurrentChapter.position+1 }
    }

    override fun getCount() = totalPages()

    override fun getItem(pos: Int): Fragment {
        val change = checkChapterOffset(pos)

        return if(change) EmptyPageFragment.getInstance(mCurrentChapter.number, mNextChapter?.number ?: "No New Chapter")

        else {
            val posOff = (if(mCurrentChapter.offset == 0) pos else pos - mCurrentChapter.offset)+1
            val link = makeLink(mCurrentChapter.link, posOff)
            val path = makePagePath(mFilePath, posOff)

            Log.d("Reader", "Link is $link and Path is $path")


            ReaderFragment.getInstance(link, path)
        }
    }

    private fun makeLink(link: String, pos: Int): String {
        return link.replaceAfterLast("/", pos.toString(), link)
    }

    private fun makePagePath(basePath: String, pos: Int): String{
        val number = if(pos < 10) "0$pos" else pos.toString()

        val list = File(basePath).listFiles { dir, name ->
            name.contains(number)
        }

        return if(list?.isNotEmpty() == true) list.single().path else ""
    }

    /**Gets the total number of pages and also adds the offset to the chapters*/
    private fun totalPages(): Int{

        return mManga.chapters.filter { it.version == mCurrentChapter.version }.map {
            if(it.position != 0) {
                val prevChapter = mManga.chapters.get(it.position - 1)

                it.offset = prevChapter.offset + prevChapter.length+1
            }
            //Increasing it by 1 accounts for each of the empty pages to be shown after every chapter
            it.length+1
        }.sum()
    }

    /**
     * Checks the position to know if a current chapter switch is necessary and switches it accordingly
     * @return True if the fragment for an emty page should be displayed of not
     */
    private fun checkChapterOffset(pos: Int): Boolean{

        //Show the empty page
        when {
            (pos == mCurrentChapter.offset+mCurrentChapter.length) || (pos == mCurrentChapter.offset-1) -> return true

            mNextChapter?.let { pos >= it.offset } == true -> {

                mPrevChapter = mCurrentChapter
                mCurrentChapter = mNextChapter!!
                mNextChapter = mManga.chapters.find { it.version == mCurrentChapter.version && it.position == mCurrentChapter.position+1 }!!

                mFilePath = downloadPath(mCurrentChapter)

                return false
            }

            mPrevChapter?.let { pos <= it.offset + it.length } == true -> {
                //It has entered the previous chapter range so we switch
                mNextChapter = mCurrentChapter
                mCurrentChapter = mPrevChapter!!
                mPrevChapter = mManga.chapters.find { it.version == mCurrentChapter.version && it.position == mCurrentChapter.position-1 }!!

                mFilePath = downloadPath(mCurrentChapter)

                //Since we are no longer using an exact value to determine when this condition is true
                //We need to do this so as to know when the empty page should be displayed
                return pos == mCurrentChapter.offset+mCurrentChapter.length
            }
            else -> return false
        }

    }

    //Since the download db seems to be failing, we temporarily create the dir path ourselves here
    private fun downloadPath(chapter: Chapter): String{

        val parentDir = mManga.title + "/" + chapter.version

        return getDownloadFolder(mActivity, parentDir, chapter.number)
    }

    interface OnChapterChangeListener {

        fun onChapterChange(newChapter: Chapter)
    }
}