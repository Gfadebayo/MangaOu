package com.exzell.mangaplayground.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public abstract class DisposableViewModel extends AndroidViewModel {

    private List<Disposable> mDisposables = new ArrayList<>();

    public DisposableViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    protected void addDisposable(Disposable dispose) {
        mDisposables.add(dispose);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mDisposables.forEach(dis -> dis.dispose());
        mDisposables.clear();
    }
}
