package com.exzell.mangaplayground.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.exzell.mangaplayground.io.Repository;
import com.exzell.mangaplayground.download.Download;

import java.util.Collections;
import java.util.List;

public class DownloadViewModel extends AndroidViewModel {
    private Context mContext;
    private Repository mRepo;

    public DownloadViewModel(@NonNull Application application) {
        super(application);

        mContext = application.getApplicationContext();
        mRepo = Repository.getInstance(application);
    }

    public List<Download> watchDownloads(LifecycleOwner o, Observer<? super List<Download>> observe){
        LiveData<List<Download>> downs = mRepo.getLiveDownloads();
        downs.observe(o, observe);
        return downs.getValue();
    }

    public void deleteDownload(Download d){
        mRepo.deleteDownloads(Collections.singletonList(d));
    }
}
