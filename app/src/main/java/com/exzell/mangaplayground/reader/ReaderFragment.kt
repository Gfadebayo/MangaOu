package com.exzell.mangaplayground.reader

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.exzell.mangaplayground.customview.ImageViewTarget
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.fragment.base.DisposableFragment
import com.exzell.mangaplayground.utils.ChapterUtils
import com.exzell.mangaplayground.viewmodels.ReaderViewModel
import io.reactivex.rxjava3.functions.Consumer
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import java.io.File

class ReaderFragment : DisposableFragment() {

    companion object INSTANCE {
        const val TAG = "Reader Fragment"


        const val PAGE_LINK = "page link"
        const val PATH = "page path"

        fun getInstance(link: String, path: String): ReaderFragment {
            val frag = ReaderFragment()

            val bund = Bundle(2).apply {
                putString(PAGE_LINK, link)
                putString(PATH, path)
            }

            frag.arguments = bund

            return frag
        }
    }

    /** The link to the image */
    private var mImageLink: String = ""

    /** The link to the page where the actual image link is */
    private lateinit var mImagePageLink: String

    private lateinit var mImagePath: String

    private val mTarget: ImageViewTarget by lazy { ImageViewTarget(view!!.findViewById(R.id.image_pager), view!!.findViewById(R.id.text_pager), view!!.findViewById(R.id.progress_pager)) }

    private val mViewModel: ReaderViewModel by lazy {
        ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(ReaderViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        mImagePageLink = arguments!!.getString(PAGE_LINK)!!
        mImagePath = arguments!!.getString(PATH)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.reader_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.text_pager).setOnClickListener {
                view.findViewById<View>(R.id.progress_pager).visibility = View.VISIBLE
                view.findViewById<View>(R.id.text_pager).visibility = View.GONE

            //Its possible the connection broke during the actual image loading, not when fetching the html
            if(mImageLink.isEmpty()) addDisposable(mViewModel.getImageLink(mImagePageLink, {onNext(it)}, {onError()}))
            else displayImage(mImageLink)
        }


            if (!assertFileExists()) addDisposable(mViewModel.getImageLink(mImagePageLink, { onNext(it) }, { onError() }))
            else {displayImage(mImagePath)}
    }

    private fun onNext(link: String) {

        mImageLink = ChapterUtils.fetchDownloadLink(Jsoup.parse(link))
        mViewModel.getImageBytes(mImageLink, {displayImage(it)}, {onError()})
    }

    private fun onError(){
        requireActivity().runOnUiThread{
            view!!.findViewById<View>(R.id.progress_pager).visibility = View.GONE
            view!!.findViewById<View>(R.id.text_pager).visibility = View.VISIBLE
        }
    }

    private fun displayImage(any: Any) {

        Glide.with(this)
                .load(any)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .into(mTarget)
                .request!!.apply { if(!isRunning) begin()}
    }

    private fun assertFileExists(): Boolean {
        val file = File(mImagePath)
        return file.exists()
    }
}