package com.exzell.mangaplayground.download

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.exzell.mangaplayground.BuildConfig
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.io.internet.InternetManager
import com.exzell.mangaplayground.utils.isConnectedToNetwork
import com.exzell.mangaplayground.utils.ChapterUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class Downloader(val mManager: DownloadManager, val context: Context): DownloadChangeListener {

    companion object{
        val tag = "Downloader"
    }

    val mPublishObserver: PublishSubject<Download> = PublishSubject.create()

    val mDisposer = CompositeDisposable()

    private val mClient = OkHttpClient.Builder().connectTimeout(2, TimeUnit.MINUTES).build()

    var isRunning = false

    init {
        mManager.addListener(this)
    }

    fun startDownloading(){
        if(isRunning) return

        mDisposer.clear()

        mDisposer.add(mPublishObserver.startWithIterable(mManager.downloads)
                .filter { !it.state.equals(Download.State.CANCELLED) }
                .subscribeOn(Schedulers.io())
                .flatMap({ createDownloadPages(it) }, 5)
                .doOnNext {
                    if(it.state.equals(DownloadPage.State.DOWNLOADED)) incrementProgress(it.parent)
                }.doOnError {
//                    Log.i("Downloader", "Error thrown when downloading")
                }.doOnComplete {
                    Log.i("Downloader", "Downloader is stopping")
                    isRunning = false
                    mDisposer.clear()
                    mManager.stopService()
                }.subscribe())

        isRunning = true
    }

    fun stopDownloading(){
        if(!isRunning) return

        mDisposer.clear()
        isRunning = false
    }

    private fun createDownloadPages(d: Download): Observable<DownloadPage>{
        return Observable.range(1, d.length).subscribeOn(Schedulers.io())
                .map { DownloadPage(createPageNumber(it), createPagePath(d.path, it), createPageUrl(d.link, it), d) }
                .doOnError {
                    Log.i("Downloader", "Error thrown when downloading")
                    d.state = Download.State.ERROR
                    mManager.updateDownload(d.id, DownloadChangeListener.FLAG_STATE)
                }
                .doOnNext {
                    if(checkDownloadState(it.parent)) {
                        if (checkPagePath(it)) it.state = DownloadPage.State.DOWNLOADED
                        else downloadPage(it)
                    }
                }
    }

    /**
     * Checks before any page is downloaded the state of a download.
     * If it is in a queued state, it changes to downloading since its can only start creating pages
     * once a download is ready. Any other state should be ignored as the are user defined
     */
    private fun checkDownloadState(d: Download): Boolean{
        if(d.state.equals(Download.State.QUEUED) || d.state.equals(Download.State.DOWNLOADING)){
            d.state = Download.State.DOWNLOADING
            return true
        }
        return false
    }

    private fun createPageNumber(num: Int) = if(num < 10) "0$num" else num.toString()

    private fun createPagePath(path: String, num: Int) = File(path, num.toString()).path

    private fun createPageUrl(link: String, num: Int): String{
        val lastIndex = link.lastIndexOf('/')
        return link.replaceRange(lastIndex+1, link.length, num.toString())
    }

    /**
     * Checks the particular file to see if it already exists and is not empty
     * @return returns true if this file already exists
     */
    private fun checkPagePath(page: DownloadPage) = File(page.parent.path).list { dir, name -> name.contains(page.number)}?.isNotEmpty() ?: false

    private fun downloadPage(page: DownloadPage){
        if(!context.isConnectedToNetwork()) throw IOException("Device is not connected to the internet")

        val url = HttpUrl.get(InternetManager.mBaseUrl + page.url)

        val request = Request.Builder().url(url).build()

        val response = mClient.newCall(request).execute()

        if(response.isSuccessful){
            val html = response.body()?.string()
            response.close()

            val link = ChapterUtils.fetchDownloadLink(Jsoup.parse(html))

            downloadAndWriteImages(link, page)

        }else{
            if(BuildConfig.DEBUG) Log.i(tag, response.code().toString())
            page.state = DownloadPage.State.ERROR
            val code = response.code()
            throw IOException("Error downloading page: Response code is $code")
        }
    }

    private fun downloadAndWriteImages(link: String?, page: DownloadPage) {
        val imageRequest = Request.Builder().url(link).build()

        val response = mClient.newCall(imageRequest).execute()
        val code = response.code()
        if(!response.isSuccessful) throw IOException("Failed: $code")

        val by = response.body()?.bytes()
        var extension = response.body()?.contentType()?.let { decipherExtension(it) } ?: "png"
        extension = ".$extension"
        response.close()

        File(page.path + extension).apply {
            if(!exists()) createNewFile()

            val outStream = FileOutputStream(this)
            outStream.write(by)
            outStream.close()

            page.state = DownloadPage.State.DOWNLOADED
        }
    }

    private fun decipherExtension(mediaType: MediaType): String{

        val type = mediaType.type()
        val sub = mediaType.subtype()
        val mimeType = "$type/$sub"

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
    }

    private fun incrementProgress(down: Download){
        down.progress++
        mManager.updateDownload(down.id, DownloadChangeListener.FLAG_PROGRESS)

        if(down.progress == down.length){
            down.state = Download.State.DOWNLOADED
            mManager.updateDownload(down.id, DownloadChangeListener.FLAG_STATE)
        }
    }

    override fun onDownloadChange(down: Download, flag: String) {
        if(flag.equals(DownloadChangeListener.FLAG_NEW)){
            mPublishObserver.onNext(down)
        }else if(flag.equals(DownloadChangeListener.FLAG_STATE)){

            if(down.state == Download.State.DOWNLOADING){
                if(mPublishObserver.contains(down).blockingGet()) mPublishObserver.onNext(down)
            }
        }
    }
}