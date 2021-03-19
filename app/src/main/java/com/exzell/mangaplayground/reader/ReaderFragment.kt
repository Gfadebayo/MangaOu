package com.exzell.mangaplayground.reader

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.exzell.mangaplayground.GlideApp
import com.exzell.mangaplayground.R
import com.exzell.mangaplayground.customview.ImageViewTarget
import com.exzell.mangaplayground.databinding.ReaderDisplayBinding
import com.exzell.mangaplayground.fragment.base.DisposableFragment
import com.exzell.mangaplayground.utils.fetchDownloadLink
import com.exzell.mangaplayground.viewmodels.ReaderViewModel
import org.jsoup.Jsoup
import java.io.File

@SuppressLint("UseRequireInsteadOfGet")
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

    /** The link to the image itself */
    private var mImageLink: String = ""

    /** The link to the page where the actual image link is */
    private lateinit var mImagePageLink: String

    private lateinit var mImagePath: String

    private val mTarget: ImageViewTarget by lazy { ImageViewTarget(view!!.findViewById(R.id.image_pager), view!!.findViewById(R.id.text_pager), view!!.findViewById(R.id.progress_pager)) }

    private lateinit var mBinding: ReaderDisplayBinding

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
        mBinding = ReaderDisplayBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mBinding.textPager.setOnClickListener {
            Toast.makeText(context, "Loading Image Failed", Toast.LENGTH_SHORT).show()
            mBinding.progressPager.visibility = View.VISIBLE
            mBinding.textPager.visibility = View.GONE

            mBinding.root.requestDisallowInterceptTouchEvent(false)

            //Its possible the connection broke during the actual image loading, not when fetching the html
            if (mImageLink.isEmpty()) addDisposable(mViewModel.getImageLink(mImagePageLink, { onNext(it) }, { onError() }))
            else displayImage(mImageLink)
        }

        if (!assertFileExists()) addDisposable(mViewModel.getImageLink(mImagePageLink, { onNext(it) }, { onError() }))
        else displayImage(mImagePath)
    }

    private fun onNext(link: String) {
        Thread {
            mImageLink = fetchDownloadLink(Jsoup.parse(link))
            mViewModel.getImageBytes(mImageLink, { displayImage(it) }, { onError() })
        }.start()
    }

    private fun onError() {
        if (!isAdded) return

        requireActivity().runOnUiThread {
            Toast.makeText(context, "Loading Image Failed", Toast.LENGTH_SHORT).show()
            mBinding.progressPager.visibility = View.GONE
            mBinding.textPager.visibility = View.VISIBLE

            //Use this to block the pager gesture detector from triggering
//            mBinding.root.requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun displayImage(any: Any) {
        GlideApp.with(this)
                .load(any)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .into(mTarget)
                .request!!.apply { if (!isRunning) begin() }
    }

    private fun assertFileExists(): Boolean {
        val file = File(mImagePath)
        return file.exists()
    }
}