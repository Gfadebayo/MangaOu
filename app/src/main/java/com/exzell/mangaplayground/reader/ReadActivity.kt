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
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.customview.VisibilityGroup
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.viewmodels.ReaderViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class ReadActivity : AppCompatActivity() {

    companion object {
        const val TAG: String = "manga"
        const val OFFSCREEN_LIMIT = 4
    }

    private lateinit var mManga: DBManga
    private lateinit var mPager: ReaderPager
    private val mVisibilityGroup: VisibilityGroup by lazy { VisibilityGroup(findViewById(R.id.toolbar), findViewById(R.id.parent_control), decorView = window.decorView) }


    private val mViewModel: ReaderViewModel by lazy{ ViewModelProvider(this, ViewModelProvider
            .AndroidViewModelFactory.getInstance(application)).get(ReaderViewModel::class.java) }

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val id = intent.getLongExtra(TAG, 0)

        mManga = mViewModel.getMangaWithChapterId(id)

        val currentChapter = mManga.chapters.single { it.id == id }

        mManga.chapters.removeIf { it.version != currentChapter.version }
        mManga.chapters.sortBy { it.position }


        mPager = findViewById(R.id.pager_reader)
        mPager.apply {

            setOnSingleTap {
                mVisibilityGroup.apply()
                true
            }

            adapter = ReaderAdapter(this@ReadActivity, mManga, currentChapter)

            setOnPageChanged { updateControls(it, findChapter(it)) }

            setCurrentItem(currentChapter.offset+currentChapter.lastReadingPosition, false)

            offscreenPageLimit = 4
        }

        findViewById<SeekBar>(R.id.seekbar_reader).setOnSeekBarChangeListener(SeekbarListener((mPager.adapter as ReaderAdapter)){
            mPager.currentItem = it
        })

        findViewById<MaterialButton>(R.id.button_reader_previous).setOnClickListener{ onClickButton(it.id) }

        findViewById<MaterialButton>(R.id.button_reader_next).setOnClickListener{ onClickButton(it.id)}
    }

    private fun onClickButton(id: Int) {
        with((mPager.adapter as ReaderAdapter)) {

            if (id == R.id.button_reader_next) mNextChapter?.let { mPager.setCurrentItem(it.offset + it.lastReadingPosition, false) }

            else mPrevChapter?.let { mPager.setCurrentItem(it.offset+it.lastReadingPosition, false) }
        }
    }

    private fun updateControls(newPosition: Int, chapter: Chapter?){
        if(chapter == null || (newPosition == chapter.offset+chapter.length)) return

        val pos = (newPosition - chapter.offset) % chapter.length
        val length = chapter.length-1

        if(pos > length) return

        chapter.lastReadingPosition = pos

        findViewById<SeekBar>(R.id.seekbar_reader).apply { progress = (pos * max) / length }

        findViewById<MaterialTextView>(R.id.text_reader_current).apply { text = (pos+1).toString() }

        onChapterChange(chapter)
    }

    private fun findChapter(pos: Int): Chapter?{
        with((mPager.adapter as ReaderAdapter)){

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

        findViewById<MaterialTextView>(R.id.text_reader_length).apply { text = newChapter.length.toString() }
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