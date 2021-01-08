package com.exzell.mangaplayground.reader

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.exzell.mangaplayground.MangaApplication
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.customview.VisibilityGroup
import com.exzell.mangaplayground.databinding.ActivityReaderBinding
import com.exzell.mangaplayground.databinding.ContentReaderBinding
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.viewmodels.ReaderViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class ReadActivity : AppCompatActivity() {

    companion object {
        const val TAG: String = "manga"
    }

    private lateinit var mManga: DBManga
    private val mBinding: ContentReaderBinding by lazy { ContentReaderBinding.bind(findViewById(R.id.content_root)) }
    private lateinit var mVisibilityGroup: VisibilityGroup

    private val mViewModel: ReaderViewModel by lazy{ ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application)).get(ReaderViewModel::class.java) }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)
        (application as MangaApplication).mAppComponent.injectRepo(mViewModel)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mVisibilityGroup = VisibilityGroup(findViewById(R.id.toolbar), mBinding.contentControl.parentControl, decorView = window.decorView)

        val id = intent.getLongExtra(TAG, 0)


        mManga = mViewModel.getMangaWithChapterId(id)

        val currentChapter = mManga.chapters.single { it.id == id }

        mManga.chapters.removeIf { it.version != currentChapter.version }
        mManga.chapters.sortBy { it.position }


        mBinding.pagerReader.apply {

            setOnSingleTap {
                mVisibilityGroup.apply()
                true
            }

            adapter = ReaderAdapter(this@ReadActivity, mManga, currentChapter)

            setOnPageChanged { updateControls(it, findChapter(it)) }

            setCurrentItem(currentChapter.offset+currentChapter.lastReadingPosition, false)

            offscreenPageLimit = 4
        }

        mBinding.contentControl.seekbarReader.setOnSeekBarChangeListener(SeekbarListener((mBinding.pagerReader.adapter as ReaderAdapter)){
            mBinding.pagerReader.currentItem = it
        })

        mBinding.contentControl.buttonReaderPrevious.setOnClickListener{ onClickButton(it.id) }

        mBinding.contentControl.buttonReaderNext.setOnClickListener{ onClickButton(it.id)}

        onChapterChange(currentChapter)
    }

    private fun onClickButton(id: Int) {
        with((mBinding.pagerReader.adapter as ReaderAdapter)) {

            if (id == R.id.button_reader_next) mNextChapter?.let { mBinding.pagerReader.setCurrentItem(it.offset+it.lastReadingPosition, false) }

            else mPrevChapter?.let { mBinding.pagerReader.setCurrentItem(it.offset+it.lastReadingPosition, false) }
        }
    }

    private fun updateControls(newPosition: Int, chapter: Chapter?){
        if(chapter == null || (newPosition == chapter.offset+chapter.length)) return

        val pos = (newPosition - chapter.offset) % chapter.length
        val length = chapter.length-1

        if(pos > length) return

        chapter.lastReadingPosition = pos

        mBinding.contentControl.seekbarReader.apply { progress = (pos * max) / length }

        mBinding.contentControl.textReaderCurrent.apply { text = (pos+1).toString() }

        onChapterChange(chapter)
    }

    private fun findChapter(pos: Int): Chapter?{
        with((mBinding.pagerReader.adapter as ReaderAdapter)){

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

        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            title = mManga.title
            subtitle = newChapter.number
        }

        mBinding.contentControl.textReaderLength.apply { text = newChapter.length.toString() }
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