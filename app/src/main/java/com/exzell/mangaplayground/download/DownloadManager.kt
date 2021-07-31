package com.exzell.mangaplayground.download

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.exzell.mangaplayground.AppExecutors
import com.exzell.mangaplayground.MangaPrefs
import com.exzell.mangaplayground.download.model.DownloadManga
import com.exzell.mangaplayground.io.Repository
import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.models.Manga
import com.exzell.mangaplayground.notification.Notifications
import com.exzell.mangaplayground.utils.removeDuplicates
import com.exzell.mangaplayground.utils.toList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class DownloadManager @Inject constructor(private val context: Context,
                                          private val repo: Repository,
                                          private val pref: MangaPrefs,
                                          private val client: OkHttpClient,
                                          private val executor: AppExecutors) {

    /** A mapping of each download to be performed to its id */
    private val downloads: HashMap<Long, Download> = hashMapOf()

    /** List of listeners notified for anything related to the downloads */
    private val listeners: MutableList<DownloadChangeListener> = ArrayList()

    /** The class that sets up download notifications */
    private val notification: DownloadNotifier = DownloadNotifier(context, this)

    /** A flag to track when the service is active inorder to stop unnecessary calls */
    private var isServiceRunning = false

    private val mServiceIntent: Intent by lazy { Intent(context, DownloadService::class.java) }

    /** To know if downloading is allowed or not */
    var canDownload = pref.getDownloadValue()
        private set

    /** A mapping of a [Downloader] to top parent of each [Download] */
    private val downloaders: HashMap<DownloadManga, Downloader> = hashMapOf()

    /** The lineup of the downloaders. The key is the name of the lineup.
     * Each LinkedList is a FIFO queue prioritizing them by either the users preference
     * or their arrival time
     */
    private val downloadersQueue: HashMap<String, LinkedList<Long>> = hashMapOf()

    init {
        setQueues()

        //init
        dbChanged(repo.getCurrentDownloads())

        addListener(notification)

        GlobalScope.launch {
            repo.getLiveDownloads().collect {
                dbChanged(it)
            }
        }

        pref.addListener {
            if (it == MangaPrefs.PREF_DOWNLOAD_VALUE) {
                val value = pref.getDownloadValue()
                if (!value) persistQueues()

                /* canDownload needs to b true in order for updateDownload to work
                so regardless of the download value in pref we temporariy set it to true
                execute the codes then get the value in the pref*/
                canDownload = true
                val state = if (value) Download.State.QUEUED else Download.State.PAUSED
                downloads.filter {
                    it.value.state != state
                }.forEach { (id, down) ->
                    down.state = state
                }

                bulkUpdateDownloads(downloads.keys.toMutableList(), DownloadChangeListener.FLAG_STATE)

                canDownload = value
            }
        }
    }

    /** Reads the lineup of the queues from the preference file */
    private fun setQueues() {
        val readyQueue = pref.getStringPreferenceValue(READY_QUEUE)
                .toList(MangaPrefs.LIST_DELIMITER) { it.toLong() }
                .removeDuplicates()
        downloadersQueue[READY_QUEUE] = LinkedList(readyQueue)


        val waitQueue = pref.getStringPreferenceValue(WAITING_QUEUE)
                .toList(MangaPrefs.LIST_DELIMITER) { it.toLong() }
                .removeDuplicates()
        downloadersQueue[WAITING_QUEUE] = LinkedList(waitQueue)

        val runQueue = pref.getStringPreferenceValue(RUNNING_QUEUE)
                .toList(MangaPrefs.LIST_DELIMITER) { it.toLong() }
                .removeDuplicates()
        downloadersQueue[RUNNING_QUEUE] = LinkedList(runQueue)
    }

    /** Persist the lineup of the downloaders. Duplicates MUST be removed before persisting
     * Since the Ready Queue has the highest priority, its values are removed from the others
     * the Running Queue next.
     */
    private fun persistQueues() {
        downloadersQueue.keys.forEach { downloadersQueue[it]!!.removeDuplicates() }
        downloadersQueue[READY_QUEUE]!!.removeAll(downloadersQueue[RUNNING_QUEUE]!!)
        downloadersQueue[WAITING_QUEUE]!!.removeAll(downloadersQueue[RUNNING_QUEUE]!!)
        downloadersQueue[WAITING_QUEUE]!!.removeAll(downloadersQueue[READY_QUEUE]!!)

        downloadersQueue[RUNNING_QUEUE]!!.removeIf { getDownloads(it).isEmpty() }
        Timber.i("Running queue is now ${downloadersQueue[RUNNING_QUEUE]}")

        pref.writeMapList(downloadersQueue)
    }

    fun prepareToDownload() {
        if (canDownload) startDownloadService()
    }

    /**
     * Starts the [Downloader] in the running queue of [downloadersQueue].
     * The method first determines if the current running downloaders size is
     * equal to the number of concurrent downloads, then adds downloads from
     * the ready queue if it is not
     */
    fun startDownloading() {
        executor.ioExecutor.submit {
            val add = pref.getDownloadLimit() - downloadersQueue[RUNNING_QUEUE]!!.size

            for (i in 0 until add) {
                if (downloadersQueue[READY_QUEUE]!!.isEmpty()) break;

                val next = downloadersQueue[READY_QUEUE]!!.remove()
                downloadersQueue[RUNNING_QUEUE] = downloadersQueue[RUNNING_QUEUE]!!.apply {
                    add(next)
                    distinct()
                }
            }


            downloadersQueue[RUNNING_QUEUE]!!
                    .map { downloaders.values.find { down -> down.id == it } }
                    .forEach { it?.startDownloading() }

            persistQueues()
        }
    }

    /** Stop the [Downloader]s in the running queue of [downloadersQueue] */
    fun stopDownloading() {
        executor.ioExecutor.submit {
            downloadersQueue[RUNNING_QUEUE]!!.map {
                downloaders.values.find { down -> down.id == it }
            }.forEach {
                it?.stopDownloading()
            }

            persistQueues()
        }
    }

    /**
     * Starts the download service used to keep the [Downloader] alive.
     * This method is not called in the constructor due to the fact that
     * it is possible for there to not be an active download or a pause download
     * by the user
     */
    private fun startDownloadService() {
        if (isServiceRunning) return


        ContextCompat.startForegroundService(context, mServiceIntent!!)
        isServiceRunning = true
    }

    /** Stop the service */
    fun stopService() {
        if (!isServiceRunning) return

        notification.dismissNotification()
        context.stopService(mServiceIntent)
        NotificationManagerCompat.from(context).cancel(Notifications.DOWNLOAD_PROGRESS_NOTIFY_ID)
        isServiceRunning = false
    }

    /**
     * Create a downloader only if necessary for the download.
     * If the downloader already exists, the method does nothing
     */
    private fun organizeDownloaders(download: Download) {
        val mangaAvailable = downloaders.keys.map { it.id }.any { it == download.mangaId }

        //only create a new downloader if that manga is not available
        if (!mangaAvailable) {
            repo.getDownloadManga(download.mangaId)?.let { manga ->
//                val manga = it.createManga()
                downloaders[manga] = Downloader(manga.id, this, context, client)

                //add the downloader to the queue if necessary
                if (!downloadersQueue.values.any { it.contains(manga.id) }) downloadersQueue[READY_QUEUE]!!.add(manga.id)
            }
        }
    }

    /**
     * Adds new downloads to the [downloads] map from the database and notifies [listeners] when it does
     * @param newDownloads The list of downloads to append to the map.
     * The method does nothing if this parameter is empty
     */
    private fun dbChanged(newDownloads: List<Download>) {
        if (downloads.values.containsAll(newDownloads)) return
        val copy = ArrayList(newDownloads)
        copy.removeAll(downloads.values)
        copy.forEach { c: Download ->
            c.state = if (canDownload) Download.State.QUEUED else Download.State.PAUSED
            downloads[c.id] = c
            organizeDownloaders(c)

            getManga(c.mangaId)?.let { it.totalLength += c.length }
        }

        bulkUpdateDownloads(copy.map { it.id }, DownloadChangeListener.FLAG_NEW)
    }

    /** Adds an observer which will be notified when a download changes */
    fun addListener(listener: DownloadChangeListener) {
        listeners.add(listener)
    }

    /** Removes listener from the download observer list */
    fun removeListener(listener: DownloadChangeListener) {
        listeners.remove(listener)
    }

    /** Some update calls needs to run regardless of whether [canDownload] is true or not */
    private fun performUpdate(download: Download?, flag: String): Boolean {
        return download?.let {
            flag == DownloadChangeListener.FLAG_NEW ||
                    (flag == DownloadChangeListener.FLAG_STATE && it.state == Download.State.CANCELLED)
        } ?: false
    }

    /**
     * Called by holders to this instance to inform the class that either a new download has been queued
     * or an ongoing download's state or progress has changed
     * @param flag The [DownloadChangeListener] flag that gives the action that happened
     */
    fun updateDownload(updateId: Long, flag: String) {
        val download = downloads[updateId]!!

        updateMangaState(download.mangaId)

        if (!canDownload && !performUpdate(download, flag)) return

        personalDownloadUpdate(listOf(download), flag)
        listeners.forEach(Consumer { l: DownloadChangeListener -> l.onDownloadChange(mutableListOf(download), flag) })
    }

    /** Same as [updateDownload] except it works for multiple downloads
     * and every download in the list must have the same id
     * @param updateIds List of [Download] id
     */
    fun bulkUpdateDownloads(updateIds: List<Long>, flag: String) {

        val downs = downloads.filter { updateIds.contains(it.key) }.values.toMutableList()
//        val any = downs.find { it.state == Download.State.CANCELLED }

        if (!canDownload && !performUpdate(downs[0], flag)) return

        personalDownloadUpdate(downs, flag)
        listeners.forEach { it.onDownloadChange(downs, flag) }
    }

    /** Updates performed by the manager itself */
    private fun personalDownloadUpdate(updates: List<Download>, flag: String) {

        updates.forEach {
            if (flag == DownloadChangeListener.FLAG_STATE) {
                removeCancelledDownloads()
                updateDb(updates)


                if (it.state == Download.State.DOWNLOADED) downloads.remove(it.id)
            }

            updateMangaState(it.mangaId)
        }

//        if (downloads.isEmpty()) stopService()

        if (!downloads.values.any { it.state == Download.State.QUEUED || it.state == Download.State.DOWNLOADING })
            stopService()
        else startDownloadService()
    }

    /** update the [DownloadManga] instance of a particular [Download] each time it changes */
    private fun updateMangaState(mangaId: Long) {
        getManga(mangaId)?.let { manga ->
            val mangaDownloads = getDownloads(manga.id)
            val downloadStates = mangaDownloads.map { it.state }
            manga.state = downloadStates.find { it == Download.State.DOWNLOADING }
                    ?: downloadStates.find { it == Download.State.QUEUED }
                            ?: downloadStates.find { it == Download.State.ERROR }
                            ?: downloadStates.find { it == Download.State.PAUSED }
                            ?: Download.State.CANCELLED

            manga.totalProgress = mangaDownloads.stream().mapToInt { it.progress }.sum()
        }
    }

    /** Returns an immutable list of the current downloads to be made */
    fun getDownloads(): List<Download> {
        return downloads.values.toList()
    }

    /** Returns an immutable list of downloads belonging to a  particular downloader/manga */
    fun getDownloads(groupId: Long): List<Download> {
        return downloads.values.filter {
            it.mangaId == groupId
        }.toList()
    }

    /**
     * Remove downloads that have been cancelled by the user from the database
     * also delete the folders created for them
     */
    private fun removeCancelledDownloads() {
        val remove = downloads.values.filter { it.state == Download.State.CANCELLED }

        remove.forEach { downloads.remove(it.id) }
        repo.deleteDownloads(remove)

        executor.diskExecutor.submit {
            remove.forEach {
                File(it.path).apply {
                    delete()
                }
            }
        }
    }

    /** Updates the downloads table in the database */
    private fun updateDb(downloads: List<Download>) {
        repo.updateDownloads(downloads)
    }

    /**
     * @param mangaId The id of the manga
     * @return The [Manga] from the [downloaders] map
     */
    fun getManga(mangaId: Long): DownloadManga? {
        return downloaders.keys.find { it.id == mangaId }
    }

    fun submitToExecutor(run: () -> Unit) {
//        executor.ioExecutor.submit(run)
    }

    /**
     * Called by a [Downloader] when it stops downloading for any reason
     * The method removes it from the Running Queue and places another downloader there
     */
    fun informDownloaderDone(downloaderId: Long) {

        //check if it being done with all its jobs is true
        val done = downloads.values.any { it.mangaId != downloaderId }

        if (done) {
            downloaders.remove(downloaders.keys.find { it.id == downloaderId })

        } else {
            downloadersQueue[READY_QUEUE]!!.add(downloaderId)
        }

        downloadersQueue[RUNNING_QUEUE]!!.remove(downloaderId)

        if (!downloads.isEmpty()) startDownloading()
    }

    companion object {
        val NO_ID = -1L

        /**For downloaders that are ready to start*/
        const val READY_QUEUE = "ready queue"

        /**For downloads that were interrupted by the user themselves] */
        const val WAITING_QUEUE = "waiting queue"

        /**For downloads that are already running*/
        const val RUNNING_QUEUE = "running queue"
    }
}