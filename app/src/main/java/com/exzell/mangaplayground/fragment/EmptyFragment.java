package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.MangaListAdapter;
import com.exzell.mangaplayground.databinding.SwiperefreshLoadingRecyclerViewBinding;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.HomeViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;

public class EmptyFragment extends SelectionFragment {

    public static final String LINK = "EmptyFragment";
    public static final String TITLE = "page title";
    private HomeViewModel mViewModel;
    private String mLink;
    private MangaListAdapter mAdapter;
    private SwiperefreshLoadingRecyclerViewBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory
                (requireActivity().getApplication(), this)).get(HomeViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);

        setContextMenuResource(0, R.menu.menu_cab_bookmark);
        mLink = getArguments().getString(LINK);

        mViewModel.initHandler(mLink);

        String title = getArguments().getString(TITLE);
        ((MaterialToolbar) requireActivity().findViewById(R.id.toolbar)).setTitle(title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = SwiperefreshLoadingRecyclerViewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayoutManager manager = new GridLayoutManager(requireActivity(), 3);
        mBinding.recyclerLoad.setLayoutManager(manager);

        mAdapter = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
        mBinding.recyclerLoad.setAdapter(mAdapter);

        createTracker(mBinding.recyclerLoad);
        mAdapter.setTracker(getTracker());


        try {
            onSuccess().accept(mViewModel.getCachedMangas());
        } catch (Throwable t) { t.printStackTrace(); }
    }

    private Consumer<List<Manga>> onSuccess(){
        return mangas -> {

            mBinding.progressLoad.setVisibility(View.GONE);
            mAdapter.addMangas(mangas);

            mViewModel.goToLink(mViewModel.getNextLink(), onSuccess());
        };
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.recyclerLoad.setAdapter(null);
        mAdapter = null;
        mBinding = null;
    }
}
