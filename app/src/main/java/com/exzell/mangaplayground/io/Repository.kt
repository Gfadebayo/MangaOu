package com.exzell.mangaplayground.io

import com.exzell.mangaplayground.AppExecutors
import com.exzell.mangaplayground.download.model.DownloadManga
import com.exzell.mangaplayground.io.database.*
import com.exzell.mangaplayground.io.internet.InternetManager
import com.exzell.mangaplayground.io.internet.MangaParkApi
import com.exzell.mangaplayground.models.Chapter
import com.exzell.mangaplayground.models.Download
import com.exzell.mangaplayground.models.Manga
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(private val mExecutor: AppExecutors, service: Retrofit, db: AppDatabase) {
    private val mMangaPark: MangaParkApi = service.create(MangaParkApi::class.java)
    private val mChapterDao: ChapterDao = db.chapterDao
    private val mMangaDao: MangaDao = db.mangaDao
    private val mDownloadDao: DownloadDao = db.downloadDao


    //TODO: Consider changing the return type of both methods to Observables
    fun advancedSearch(queries: Map<String, String>): Observable<ResponseBody>? {
        return try {
            mMangaPark.advancedSearch(queries).subscribeOn(Schedulers.io())
        } catch (e: CancellationException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }

    fun home(): Response<ResponseBody>? {
        return try {
            mExecutor.ioExecutor.submit(Callable { mMangaPark.home().execute() }).get()
        } catch (e: CancellationException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Moves to a particular page in the same website
     */
    fun moveTo(link: String): Observable<ResponseBody> {
        return mMangaPark.next(link)
    }

    /**
     * Moves to an entirely different website
     */
    fun goTo(link: String): Observable<ResponseBody>? {
        return try {
            val req = Request.Builder().url(link).build()
            Observable.just(InternetManager.client.newCall(req).execute().body())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    //Database calls
    //Calls to MangaDao
    /**
     * Takes care of inserting the mangas into the DB. After the manga id is gotten from the DB,
     * it is set into its manga instance, so be sure not to replace the instances passed to this method
     */
    fun insertManga(vararg manga: Manga) {
        mExecutor.diskExecutor.submit {
            for (man in manga) {
                val id = mMangaDao.insertMangas(man)
                man.chapters.forEach(Consumer { chap: Chapter -> chap.mangaId = id })
                man.id = id
            }
            mChapterDao.insertChapters(manga.flatMap {
                it.chapters
            })
        }
    }

    fun updateManga(andChapters: Boolean, vararg manga: Manga) {
        mExecutor.diskExecutor.submit {
            mMangaDao.updateMangas(listOf(*manga))

            if (!andChapters) return@submit

            //separate new from old: key 1 for those with ids(i.e already in the DB) and 2 for those without one
            val chapterGroup = manga.flatMap {
                it.chapters.also { chapLst ->
                    chapLst.filter { ch -> ch.mangaId <= 0 }
                            .onEach { ch -> ch.mangaId = it.id }
                }
            }.groupBy { chap -> if (chap.id > 0) 1 else 2 }

            chapterGroup.keys.forEach {
                chapterGroup[it]?.let { group ->
                    if (it == 1) mChapterDao.updateChapters(group)
                    else mChapterDao.insertChapters(group)
                }
            }
        }
    }

    fun updateManga(vararg infos: BookmarkInfo) {
        mExecutor.diskExecutor.submit {
            mMangaDao.updateMangasPart(listOf(*infos))
        }
    }

    fun getMangaWithLink(link: String): Manga? {
        return try {
            mExecutor.diskExecutor.submit(Callable<Manga> { mMangaDao.getMangaFromLink(link) }).get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }

    fun getBookmarkedMangaNotLive(): List<DBManga> = try {
        mExecutor.diskExecutor.submit(Callable { mMangaDao.notLiveBookmarks() }).get()
    } catch (e: ExecutionException) {
        e.printStackTrace()
        emptyList()
    } catch (e: InterruptedException) {
        e.printStackTrace()
        emptyList()
    }

    fun getBookmarkedManga(): Flow<List<BookmarkInfo>> {
        return mMangaDao.bookmarks()
    }

    fun getDownloadedMangas(): Flow<List<BookmarkInfo>> {
        return mMangaDao.downloads()
    }

    fun getMangaWithIds(ids: List<Long?>): List<DBManga> {
        return try {
            mExecutor.diskExecutor.submit(Callable { ids.stream().map { s: Long? -> mMangaDao.getMangaFromId(s!!) }.collect(Collectors.toList()) }).get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            emptyList()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun lastReadMangas(time: Long): List<HistoryInfo> {
        return try {
            mExecutor.diskExecutor.submit(Callable { mMangaDao.lastReadInfo(time).distinctBy { it.id } }).get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            emptyList()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getMangaForChapter(id: Long): DBManga? {
        return try {
            mExecutor.diskExecutor.submit(Callable { mMangaDao.getMangaFromChapter(id) }).get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }

    fun getDownloadManga(chapterId: Long): DownloadManga? {
        return try {
            mExecutor.diskExecutor.submit(Callable { mMangaDao.getMangaInfoFromId(chapterId).createManga() }).get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }

    //Calls to ChapterDao
    fun updateChapters(chapter: List<Chapter>) {
        mExecutor.diskExecutor.submit { mChapterDao.updateChapters(chapter) }
    }

    fun updateChapterTime(chapters: List<ChapterTimeUpdate>) {
        mExecutor.diskExecutor.submit { mChapterDao.updateChaptersTime(chapters) }
    }

    /**
     * Returns the timestamp for every manga with atleast 1 chapter with a read time greater than 0
     */
    fun allMangaTime(): Flow<List<Long>> {
        return mChapterDao.allMangaTime()
    }

    //Calls to DownloadDao
    fun insertDownloads(downs: List<Download>) {
        mExecutor.diskExecutor.submit { mDownloadDao.addDownloads(downs) }
    }

    fun deleteDownloads(downs: List<Download>) {
        mExecutor.diskExecutor.submit { mDownloadDao.deleteDownloads(downs) }
    }

    /** Returns the list of all uncompleted downloads */
    fun getLiveDownloads(): Flow<List<Download>> {
        return mDownloadDao.getPendingDownloadsLive()
    }

    /** Returns a list of every download in the db*/
    fun getDownloads(): Flow<List<Download>> {
        return mDownloadDao.getAllDownloads()
    }

    /** Returns a list of chapter_ids for all the completed downloads **/
    fun getCompletedDownloadChapterIds(): Flow<List<Long>> {
        return mDownloadDao.getCompleteIds()
    }

    fun getCurrentDownloads(): List<Download> = try {
        mExecutor.diskExecutor.submit(Callable { mDownloadDao.getPendingDownloads() }).get()
    } catch (e: ExecutionException) {
        e.printStackTrace()
        emptyList()
    } catch (e: InterruptedException) {
        e.printStackTrace()
        emptyList()
    }

    fun updateDownloads(d: List<Download>) {
        mExecutor.diskExecutor.submit { mDownloadDao.updateDownloads(d) }
    }

    fun getDownloadPath(chapterId: Long): String? {
        return try {
            mExecutor.diskExecutor.submit<String> { mDownloadDao.getPathFromId(chapterId) }.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }
}