package com.exzell.mangaplayground.viewmodels.factory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.exzell.mangaplayground.viewmodels.MangaViewModel;

public class MangaModelFactory implements ViewModelProvider.Factory {

    private String mLink;
    private Application mApplication;

    public MangaModelFactory(Application application, String link) {
        this.mLink = link;
        this.mApplication = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MangaViewModel(mApplication, mLink);
    }
}
