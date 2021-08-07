package com.exzell.mangaplayground.di

import android.content.Context
import com.exzell.mangaplayground.AppExecutors
import com.exzell.mangaplayground.MangaApplication
import com.exzell.mangaplayground.UpdateService
import com.exzell.mangaplayground.download.DownloadManager
import com.exzell.mangaplayground.download.DownloadService
import com.exzell.mangaplayground.fragment.DownloadQueueFragment
import com.exzell.mangaplayground.fragment.DownloadQueueMangaFragment
import com.exzell.mangaplayground.io.database.AppDatabase
import com.exzell.mangaplayground.io.internet.InternetManager
import com.exzell.mangaplayground.viewmodels.*
import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = [SubcomponentsModule::class, InternetManager::class, AppDatabase::class])
interface AppComponent{

    fun mainComponent(): MainActivityComponent.Factory

    fun injectRepo(updateService: UpdateService)

    fun injectRepo(bookmarkViewModel: BookmarkViewModel)

    fun injectRepo(homeViewModel: HomeViewModel)

    fun injectRepo(mangaViewModel: MangaViewModel)

    fun injectRepo(searchViewModel: SearchViewModel)

    fun injectRepo(readerViewModel: ReaderViewModel)

    fun injectRepo(historyViewModel: HistoryViewModel)

    fun injectDownloadManager(downloadService: DownloadService)

    fun injectDownloadManager(downloadFragment: DownloadQueueFragment)

    fun injectDownloadManager(downloadMangaFragment: DownloadQueueMangaFragment)

    fun injectPrefs(downloadManager: DownloadManager)

    fun injectPrefs(downloadFragment: DownloadQueueFragment)

    fun injectPrefs(downloadQueueMangaFragment: DownloadQueueMangaFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun bindContext(context: Context): Builder

        @BindsInstance
        fun bindActivity(application: MangaApplication): Builder

        @BindsInstance
        fun bindExecutors(appExecutors: AppExecutors): Builder

        @BindsInstance
        fun bindNetworkClient(okhttp: OkHttpClient): Builder

        fun build(): AppComponent
    }
}

@Subcomponent
interface MainActivityComponent{

    @Subcomponent.Factory
    interface Factory{
        fun create(): MainActivityComponent
    }
}