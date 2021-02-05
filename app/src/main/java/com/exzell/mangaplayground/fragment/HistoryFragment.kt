package com.exzell.mangaplayground.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.recyclerview.widget.ConcatAdapter
import com.exzell.mangaplayground.MangaApplication
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.adapters.HistoryAdapter
import com.exzell.mangaplayground.adapters.TitleAdapter
import com.exzell.mangaplayground.databinding.GenericLoadingRecyclerViewBinding
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.reader.ReadActivity
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel
import timber.log.Timber
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

class HistoryFragment : Fragment() {
    private var mViewModel: BookmarkViewModel? = null
    private var mBinding: GenericLoadingRecyclerViewBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mViewModel = ViewModelProvider(this, AndroidViewModelFactory(requireActivity().application)).get(BookmarkViewModel::class.java)
        (requireActivity().application as MangaApplication).mAppComponent.injectRepo(mViewModel!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = GenericLoadingRecyclerViewBinding.inflate(layoutInflater)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding!!.progressLoad.visibility = View.GONE
        val mAdapter = ConcatAdapter()
        mBinding!!.recyclerLoad.adapter = mAdapter
        setHistory()
    }

    private fun setHistory() {
        Thread {
            val times: List<Long> = mViewModel!!.times.stream().map<Any>(())
            //To be used to remove mangas that have already been added to the adapter
            val foundMangas: MutableList<Long> = ArrayList()
            val `in` = Calendar.getInstance()
            val today = `in`.timeInMillis
            val todayInDays = Math.floorDiv(today, (1000 * 60 * 60 * 24).toLong()).toInt()
            Timber.d("Today is %d", today)
            times.forEach(Consumer { c: Long? ->
                val dayCalen = Calendar.getInstance()
                dayCalen.timeInMillis = c!!
                dayCalen[Calendar.MINUTE] = 0
                dayCalen[Calendar.HOUR] = 0
                dayCalen[Calendar.SECOND] = 0
                dayCalen[Calendar.MILLISECOND] = 0
                val day = dayCalen.timeInMillis
                val historyManga = mViewModel!!.getHistoryManga(c).stream()
                        .filter { man: DBManga -> !foundMangas.contains(man.id) }.collect(Collectors.toList())
                foundMangas.addAll(historyManga.stream().map { man: DBManga -> man.id }.collect(Collectors.toList()))
                if (historyManga.isEmpty()) return@forEach
                val dayInDays = Math.floorDiv(day, (1000 * 60 * 60 * 24).toLong()).toInt()
                val days = todayInDays - dayInDays
                val dayTitle = mViewModel!!.getDayTitle(days)
                requireActivity().runOnUiThread {
                    val hAdapter = HistoryAdapter(requireActivity(), historyManga)
                    val tAdapter = TitleAdapter(requireActivity(), dayTitle, hAdapter)
                    hAdapter.setOnButtonsClickedListener(onButtonClicked(), onButtonClicked())
                    (mBinding!!.recyclerLoad.adapter as ConcatAdapter?)!!.addAdapter(tAdapter)
                    (mBinding!!.recyclerLoad.adapter as ConcatAdapter?)!!.addAdapter(hAdapter)
                }
            })
        }.start()
    }

    private fun onButtonClicked(): View.OnClickListener {
        return View.OnClickListener { v: View ->
            val viewHolder = mBinding!!.recyclerLoad.findContainingViewHolder(v) as HistoryAdapter.ViewHolder?
            val manga = (viewHolder!!.bindingAdapter as HistoryAdapter?)!!.mangas[viewHolder.bindingAdapterPosition]
            if (v.id == R.id.button_resume) {
                val resumeIntent = Intent(requireActivity(), ReadActivity::class.java)
                resumeIntent.putExtra(ReadActivity.CHAPTER, manga.lastChapter.id)
                ContextCompat.startActivity(requireActivity(), resumeIntent, null)
            } else {
                mViewModel!!.removeFromHistory(manga.lastChapter)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.recyclerLoad.adapter = null
        mBinding = null
    }
}