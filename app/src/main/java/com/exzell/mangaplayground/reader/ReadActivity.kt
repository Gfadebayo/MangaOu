package com.exzell.mangaplayground.reader

import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.exzell.mangaplayground.MangaApplication
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.customview.VisibilityGroup
import com.exzell.mangaplayground.databinding.ActivityReaderBinding
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.viewmodels.ReaderViewModel
import com.google.android.material.appbar.MaterialToolbar

class ReadActivity : AppCompatActivity() {

    companion object {
        const val CHAPTER: String = "chapter_id"
    }

    private lateinit var mManga: DBManga
    private val mBinding: ActivityReaderBinding by lazy { ActivityReaderBinding.inflate(layoutInflater) }
    private lateinit var mVisibilityGroup: VisibilityGroup

    private val mViewModel: ReaderViewModel by lazy{ ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application)).get(ReaderViewModel::class.java) }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }
        (application as MangaApplication).mAppComponent.injectRepo(mViewModel)


        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
        mVisibilityGroup = VisibilityGroup(mBinding.toolbarLayout, mBinding.content.parentControl, decorView = window.decorView)

        val id = intent.getLongExtra(CHAPTER, 0)


        mManga = mViewModel.getMangaWithChapterId(id)

        val currentChapter = mManga.chapters.single { it.id == id }

        mManga.chapters.removeIf { it.version != currentChapter.version }
        mManga.chapters.sortBy { it.position }

        mBinding.toolbar.title = mManga.title

        mBinding.content.pagerReader.apply {
            onSingleTap = {
                mVisibilityGroup.apply()
                true
            }

            adapter = ReaderAdapter(this@ReadActivity, mManga, currentChapter)

            onPageChanged = { updateControls(it, findChapter(it)) }

            setCurrentItem(currentChapter.offset+currentChapter.lastReadingPosition, false)

//            offscreenPageLimit = 4
        }

        mBinding.content.seekbarReader.setOnSeekBarChangeListener(SeekBarListener((mBinding.content.pagerReader.adapter as ReaderAdapter)){
            mBinding.content.pagerReader.currentItem = it
        })

        mBinding.content.buttonReaderPrevious.setOnClickListener{ onClickButton(it.id) }

        mBinding.content.buttonReaderNext.setOnClickListener{ onClickButton(it.id)}

        onChapterChange(currentChapter)
    }

    private fun onClickButton(id: Int) {
        with((mBinding.content.pagerReader.adapter as ReaderAdapter)) {

            if (id == R.id.button_reader_next) mNextChapter?.let { mBinding.content.pagerReader.setCurrentItem(it.offset+it.lastReadingPosition, false) }

            else mPrevChapter?.let { mBinding.content.pagerReader.setCurrentItem(it.offset+it.lastReadingPosition, false) }
        }
    }

    private fun updateControls(newPosition: Int, chapter: Chapter?){
        if(chapter == null || (newPosition == chapter.offset+chapter.length)) return

        val pos = (newPosition - chapter.offset) % chapter.length
        val length = chapter.length-1

        if(pos > length) return

        chapter.lastReadingPosition = pos

        mBinding.content.seekbarReader.apply { progress = (pos * max) / length }

        mBinding.content.textReaderCurrent.apply { text = (pos+1).toString() }

        onChapterChange(chapter)
    }

    private fun findChapter(pos: Int): Chapter?{
        with((mBinding.content.pagerReader.adapter as ReaderAdapter)){

            return when{
                pos < mCurrentChapter.offset -> mPrevChapter

                pos > mCurrentChapter.offset+ mCurrentChapter.length -> mNextChapter

                else -> mCurrentChapter
            }
        }
    }

    private fun onChapterChange(newChapter: Chapter) {

        newChapter.lastReadTime = mViewModel.today()

        if(newChapter.lastReadingPosition == newChapter.length-1) {
            newChapter.isCompleted = true
            newChapter.lastReadingPosition = 0
        }

        mBinding.toolbar.apply {
            subtitle = newChapter.number
        }

        mBinding.content.textReaderLength.apply { text = newChapter.length.toString() }
    }

    override fun onStop() {
        super.onStop()
        mViewModel.updateManga(mManga)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVisibilityGroup.clear()
    }
}