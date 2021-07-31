package com.exzell.mangaplayground.download

import android.content.Context
import android.webkit.MimeTypeMap
import com.exzell.mangaplayground.BuildConfig
import com.exzell.mangaplayground.download.model.DownloadPage
import com.exzell.mangaplayground.io.internet.InternetManager
import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.utils.fetchDownloadLink
import com.exzell.mangaplayground.utils.isConnectedToNetwork
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InterruptedIOException

class Downloader(val id: Long,
                 val mManager: DownloadManager,
                 val context: Context,
                 val client: OkHttpClient) : DownloadChangeListener {

    val mPublishObserver: PublishSubject<Download> = PublishSubject.create()

    val mDisposer = CompositeDisposable()

    var isRunning = false

    var concurrentDownloads = 3

    init {
        mManager.addListener(this)
    }

    fun startDownloading() {
        if (isRunning) return

        mDisposer.clear()

        mDisposer.add(mPublishObserver.startWithIterable(mManager.getDownloads(id))
                .filter { !it.state.equals(Download.State.CANCELLED) }
                .subscribeOn(Schedulers.io())
                .doOnEach { it.value.progress = 0 }
                .flatMap({ createDownloadPages(it) }, concurrentDownloads)
                .subscribe({ page ->
                    if (page.state == DownloadPage.State.DOWNLOADED) incrementProgress(page.parent)
                }, { error ->
                    Timber.d(error)
                }, {
                    Timber.i("Download Complete")
                    isRunning = false
                    mDisposer.clear()
                    mManager.informDownloaderDone(id)
                }))

        isRunning = true
    }

    fun stopDownloading() {
        if (!isRunning) return

        client.dispatcher().cancelAll()

        mPublishObserver.unsubscribeOn(Schedulers.io())

        mDisposer.clear()
        isRunning = false
    }

    private fun createDownloadPages(d: Download): Observable<DownloadPage> {
        return Observable.range(1, d.length - d.progress).subscribeOn(Schedulers.io())
                .map { DownloadPage(createPageNumber(it), createPagePath(d.path, it), createPageUrl(d.link, it), d) }
                .doOnError {
                    Timber.i("Error thrown when downloading")
                    d.state = Download.State.ERROR
                    mManager.updateDownload(d.id, DownloadChangeListener.FLAG_STATE)
                }
                .doOnNext {
                    if (checkDownloadState(it.parent)) {
                        if (checkPagePath(it)) it.state = DownloadPage.State.DOWNLOADED
                        else downloadPage(it)
                    }
                }
    }

    /**
     * Checks before any page is downloaded the state of a download.
     * If it is in a queued state, it changes to downloading since its can only start creating pages
     * once a download is ready. Any other state should be ignored as they are user defined
     */
    private fun checkDownloadState(d: Download): Boolean {
        if (d.state.equals(Download.State.QUEUED)) {
            d.state = Download.State.DOWNLOADING

            mManager.updateDownload(d.id, DownloadChangeListener.FLAG_STATE)
        }

        return d.state.equals(Download.State.DOWNLOADING)
    }

    private fun createPageNumber(num: Int) = if (num < 10) "0$num" else num.toString()

    private fun createPagePath(path: String, num: Int): String {
        val numStr = if (num >= 10) num.toString() else "0".plus(num)
        return File(path, numStr).path
    }

    private fun createPageUrl(link: String, num: Int): String {
        val lastIndex = link.lastIndexOf('/')
        return link.replaceRange(lastIndex + 1, link.length, num.toString())
    }

    /**
     * Checks the particular file to see if it already exists and is not empty
     * @return returns true if this file already exists
     */
    private fun checkPagePath(page: DownloadPage) = File(page.parent.path).list { dir, name -> name.contains(page.number) }?.isNotEmpty()
            ?: false

    private fun downloadPage(page: DownloadPage) {
        if (!context.isConnectedToNetwork()) {
            Timber.d("Device is not connected to the internet")
            return
        }

        //The cancel call causes unfinished calls to crash the app
        try {
            if (page.parent.state != Download.State.DOWNLOADING) return

            val url = HttpUrl.get(InternetManager.baseUrl + page.url)

            val request = Request.Builder().url(url).build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val html = response.body()?.string()
                response.close()

                val link = fetchDownloadLink(Jsoup.parse(html))

                downloadAndWriteImages(link, page)

            } else {
                if (BuildConfig.DEBUG) Timber.i(response.code().toString())
                page.state = DownloadPage.State.ERROR
                val code = response.code()
                Timber.d("Error downloading page: Response code is $code")
            }
        } catch (e: InterruptedIOException) {
        } catch (e: IOException) {
        }
    }

    private fun downloadAndWriteImages(link: String?, page: DownloadPage) {
        val imageRequest = Request.Builder().url(link).build()

        val response = client.newCall(imageRequest).execute()
        val code = response.code()
        if (!response.isSuccessful) {
            Timber.d("Failed to download page. Error code: $code")
            page.parent.state = Download.State.ERROR
            return
        }

        val by = response.body()?.bytes()
        var extension = response.body()?.contentType()?.let { decipherExtension(it) } ?: "png"
        extension = ".$extension"
        response.close()

        File(page.path + extension).apply {
            val parent = this.parentFile!!
            if (!parent.exists()) parent.mkdirs()

            if (!exists()) createNewFile()

            val outStream = FileOutputStream(this)
            outStream.write(by)
            outStream.close()

            page.state = DownloadPage.State.DOWNLOADED
        }
    }

    private fun decipherExtension(mediaType: MediaType): String {

        val type = mediaType.type()
        val sub = mediaType.subtype()
        val mimeType = "$type/$sub"

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
    }

    private fun incrementProgress(down: Download) {
        down.progress++
        mManager.updateDownload(down.id, DownloadChangeListener.FLAG_PROGRESS)

        if (down.progress == down.length) {
            down.state = Download.State.DOWNLOADED
            mManager.updateDownload(down.id, DownloadChangeListener.FLAG_STATE)
        }
    }

    override fun onDownloadChange(downs: MutableList<Download>, flag: String) {

        if (downs[0].mangaId != this.id && !isRunning) return

        if (flag.equals(DownloadChangeListener.FLAG_NEW)) downs.forEach { mPublishObserver.onNext(it) }
        else if (flag.equals(DownloadChangeListener.FLAG_STATE)) {

            if (downs[0].state == Download.State.DOWNLOADING || downs[0].state == Download.State.QUEUED) {
                downs.forEach {
                    mDisposer.add(mPublishObserver.contains(it)
                            .subscribeOn(Schedulers.computation())
                            .doOnSuccess { success ->
                                Timber.i("Checking if Download $it is available")
                                if (!success) mPublishObserver.onNext(it)
                            }
                            .subscribe())
                }
            }
        }
    }
}