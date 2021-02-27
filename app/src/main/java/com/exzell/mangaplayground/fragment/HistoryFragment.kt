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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ConcatAdapter
import com.exzell.mangaplayground.MangaApplication
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.adapters.HistoryAdapter
import com.exzell.mangaplayground.adapters.TitleAdapter
import com.exzell.mangaplayground.databinding.GenericLoadingRecyclerViewBinding
import com.exzell.mangaplayground.reader.ReadActivity
import com.exzell.mangaplayground.utils.reset
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel
import java.util.*

class HistoryFragment : Fragment() {
    private var mViewModel: BookmarkViewModel? = null
    private var mBinding: GenericLoadingRecyclerViewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mViewModel = ViewModelProvider(this, AndroidViewModelFactory(requireActivity().application)).get(BookmarkViewModel::class.java)
        (requireActivity().application as MangaApplication).mAppComponent.injectRepo(mViewModel!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = GenericLoadingRecyclerViewBinding.inflate(layoutInflater)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding!!.progressLoad.visibility = View.GONE
        mBinding!!.recyclerLoad.adapter = ConcatAdapter()

        mViewModel!!.times.observe(viewLifecycleOwner, {setHistory(it!!, mBinding!!.recyclerLoad.adapter as ConcatAdapter?)})
    }

    private fun setHistory(timestamps: List<Long>, adapter: ConcatAdapter?) {

        val titleAdapters: List<TitleAdapter> = adapter?.let {
            adapter.adapters.filterIsInstance<TitleAdapter>()
        } ?: emptyList()

        Thread {

            val times = timestamps.map { Calendar.getInstance().reset(it).timeInMillis }.distinct()

            //resetting the mangas time with the calendar extension should also work
            val historyMangas = mViewModel!!.getHistoryManga(times.minOrNull()!!).groupBy {
                times.find { time -> it.lastReadTime >= time }
            }

            val today = Calendar.getInstance().reset().timeInMillis
            val todayInDays = Math.floorDiv(today, (1000 * 60 * 60 * 24).toLong())

            historyMangas.keys.forEach { time ->
                val day = Calendar.getInstance().reset(time!!).timeInMillis

                val historyManga = historyMangas.getOrDefault(time, emptyList())

                if (historyManga.isNotEmpty()) {
                    val dayInDays = Math.floorDiv(day, (1000 * 60 * 60 * 24).toLong())
                    val days = todayInDays - dayInDays
                    val dayTitle = mViewModel!!.getDayTitle(days.toInt())

                    if (isAdded)
                        requireActivity().runOnUiThread {
                            //Its possible the concat adapter already has the adapters so we should just update them
                            val index = times.indexOf(time)

                            with(adapter!!) {
                                if(index < titleAdapters.size){
                                        titleAdapters[index].apply {
                                            title = dayTitle
                                            (bodyAdapter as HistoryAdapter).apply {
                                                mangas = historyManga
                                            }
                                        }
                                }else {
                                    val hAdapter = HistoryAdapter(requireActivity(), historyManga)
                                    val tAdapter = TitleAdapter(requireActivity(), dayTitle, hAdapter)
                                    hAdapter.setOnClickListener(onBodyClicked())
                                    hAdapter.setOnButtonsClickedListener(onButtonClicked(), onButtonClicked())
                                    addAdapter(tAdapter)
                                    addAdapter(hAdapter)
                                }
                            }
                        }
                }
            }

            //Trim the Concat Adapter of unneeded adapters
            requireActivity().runOnUiThread {
                if (times.size < titleAdapters.size) {
                    val diff = titleAdapters.size - times.size
                    for (i in 0 until diff) {
                        val titleAdapter = titleAdapters[titleAdapters.size - i - 1]
                        adapter!!.removeAdapter(titleAdapter)
                        adapter.removeAdapter(titleAdapter.bodyAdapter)
                    }
                }
            }
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

    private fun onBodyClicked() = View.OnClickListener {
        val viewHolder = mBinding!!.recyclerLoad.findContainingViewHolder(it) as HistoryAdapter.ViewHolder?
        val manga = (viewHolder!!.bindingAdapter as HistoryAdapter?)!!.mangas[viewHolder.bindingAdapterPosition]

        Navigation.findNavController(it).navigate(R.id.action_nav_history_to_frag_manga, Bundle(1).apply { putString(MangaFragment.MANGA_LINK, manga.link)})
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.recyclerLoad.adapter = null
        mBinding = null
    }
}