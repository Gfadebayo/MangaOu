package com.exzell.mangaplayground.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ConcatAdapter
import com.exzell.mangaplayground.MangaApplication
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.adapter.HistoryAdapter
import com.exzell.mangaplayground.adapter.TitleAdapter
import com.exzell.mangaplayground.databinding.GenericLoadingRecyclerViewBinding
import com.exzell.mangaplayground.fragment.base.SearchViewFragment
import com.exzell.mangaplayground.io.database.DBManga
import com.exzell.mangaplayground.reader.ReadActivity
import com.exzell.mangaplayground.utils.getTimeOnly
import com.exzell.mangaplayground.utils.reset
import com.exzell.mangaplayground.viewmodels.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class HistoryFragment : SearchViewFragment() {
    private var mViewModel: HistoryViewModel? = null
    private var mBinding: GenericLoadingRecyclerViewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mViewModel = ViewModelProvider(requireActivity(), AndroidViewModelFactory(requireActivity().application)).get(HistoryViewModel::class.java)
        (requireActivity().application as MangaApplication).mAppComponent.injectRepo(mViewModel!!)

        mViewModel!!.startWatching()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = GenericLoadingRecyclerViewBinding.inflate(layoutInflater)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.progressLoad.visibility = View.GONE

        mBinding!!.recyclerLoad.adapter = ConcatAdapter()

        setHistory(mViewModel!!.getHistory())

        mViewModel!!.onHistoryChanged = { map -> setHistory(map) }

        setSearchListeners({ onSearchQueryChanged(it) }, { onSearchQueryChanged("") })
    }

    private fun setHistory(historyMangas: Map<Long, List<DBManga>>) {
        if (!isAdded) return

        mBinding!!.textOther.apply {
            if (historyMangas.isNotEmpty()) visibility = View.GONE
            else {
                visibility = View.VISIBLE
                text = getString(R.string.no_recently_read_mangas)
            }
        }

        mBinding!!.textOther.apply {

        }
        val adapter: ConcatAdapter = mBinding!!.recyclerLoad.adapter as ConcatAdapter

        val titleAdapters: List<TitleAdapter> = adapter.let {
            adapter.adapters.filterIsInstance<TitleAdapter>()
        }

        historyMangas.keys.forEach { time ->

            val historyManga = historyMangas.getOrDefault(time, emptyList())

            if (historyManga.isNotEmpty()) {
                val dayTitle = mViewModel!!.getDayTitle(time)

                //Its possible the concat adapter already has the adapters so we should just update them
                val index = historyMangas.keys.indexOf(time)

                with(adapter) {
                    if (index < titleAdapters.size) {
                        titleAdapters[index].apply {
                            title = dayTitle
                            (bodyAdapter as HistoryAdapter).apply {
                                mangas = historyManga
                            }
                        }
                    } else {
                        val hAdapter = HistoryAdapter(requireActivity(), historyManga)
                        val tAdapter = TitleAdapter(requireActivity(), dayTitle, hAdapter).apply { setDrawable(null) }
                        hAdapter.setOnClickListener(onBodyClicked())
                        hAdapter.setOnButtonsClickedListener(onButtonClicked())
                        addAdapter(tAdapter)
                        addAdapter(hAdapter)
                    }
                }
            }
        }

        //Trim the Concat Adapter of unneeded adapters
        if (historyMangas.size < titleAdapters.size) {
            val diff = titleAdapters.size - historyMangas.size
            for (i in 0 until diff) {
                val titleAdapter = titleAdapters[titleAdapters.size - i - 1]
                adapter.removeAdapter(titleAdapter)
                adapter.removeAdapter(titleAdapter.bodyAdapter)
            }
        }
    }

    private fun onButtonClicked(): View.OnClickListener {
        return View.OnClickListener { v: View ->
            val viewHolder = mBinding!!.recyclerLoad.findContainingViewHolder(v) as HistoryAdapter.ViewHolder?
            val manga = (viewHolder!!.bindingAdapter as HistoryAdapter?)!!.mangas[viewHolder.bindingAdapterPosition]

            if (v.id == R.id.image_resume) launchReadActivity(manga.lastChapter.id)
            else if (v.id == R.id.image_more) createMoreDialog(manga)
            else mViewModel!!.removeFromHistory(manga)
        }
    }

    private fun createMoreDialog(manga: DBManga) {
        mViewModel!!.getChaptersWithMangaId(manga.id) {
            val list = it.map {
                val chapNumber = "Chapter: " + it.number
                val lastRead = "Last read position: " + it.lastReadingPosition + "/" + it.length
                val lastTime = "Last read time: " + mViewModel!!.getDayTitle(it.lastReadTime) + " at " + it.lastReadTime.getTimeOnly()

                chapNumber + "\n" + lastRead + "\n" + lastTime
            }

            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_textview, R.id.text_spinner, list)
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("${manga.title} History")
                    .setAdapter(adapter) { dialog, index ->

                        launchReadActivity(it[index].id)

                        dialog.cancel()
                    }.show()
        }
    }

    private fun onBodyClicked() = View.OnClickListener {
        val viewHolder = mBinding!!.recyclerLoad.findContainingViewHolder(it) as HistoryAdapter.ViewHolder?
        val manga = (viewHolder!!.bindingAdapter as HistoryAdapter?)!!.mangas[viewHolder.bindingAdapterPosition]

        Navigation.findNavController(it).navigate(R.id.frag_manga, Bundle(1).apply { putString(MangaFragment.MANGA_LINK, manga.link) })
    }

    private fun onSearchQueryChanged(newText: String) {
        setHistory(mViewModel!!.getMangas().filter {
            it.title.contains(newText, true)
        }.groupBy {
            Calendar.getInstance().reset(it.lastReadTime).timeInMillis
        })
    }

    private fun launchReadActivity(id: Long) {
        val resumeIntent = Intent(requireActivity(), ReadActivity::class.java)
        resumeIntent.putExtra(ReadActivity.CHAPTER, id)
        ContextCompat.startActivity(requireActivity(), resumeIntent, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding!!.recyclerLoad.adapter = null
        mBinding = null
    }
}