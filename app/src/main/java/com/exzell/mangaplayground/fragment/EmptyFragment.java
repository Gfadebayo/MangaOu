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
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.HomeViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;

public class EmptyFragment extends SelectionFragment {

    public static final String TAG = "EmptyFragment";
    public static final String TITLE = "page title";
    private HomeViewModel mViewModel;
    private String mLink;
    private RecyclerView mRecyclerView;
    private MangaListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory
                (requireActivity().getApplication(), this)).get(HomeViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);

        setMenuResource(R.menu.cab_menu);
        mLink = getArguments().getString(TAG);

        mViewModel.initHandler(mLink);

        String title = getArguments().getString(TITLE);
        ((MaterialToolbar) requireActivity().findViewById(R.id.toolbar)).setTitle(title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.swiperefresh_loading_recycler_view, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_load);

        GridLayoutManager manager = new GridLayoutManager(requireActivity(), 3);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
        mRecyclerView.setAdapter(mAdapter);

        createTracker(mRecyclerView);
        mAdapter.setTracker(getTracker());


        try {
            onSuccess().accept(mViewModel.getCachedMangas());
        } catch (Throwable t) { t.printStackTrace(); }
    }

    private Consumer<List<Manga>> onSuccess(){
        return mangas -> {
            getView().findViewById(R.id.progress_load).setVisibility(View.GONE);
            mAdapter.addMangas(mangas);

            addDisposable(mViewModel.goToLink(mViewModel.getNextLink(), onSuccess()));
        };
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        mAdapter = null;
        mRecyclerView = null;
    }
}
