package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.UpdateService;
import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel;

import java.util.ArrayList;
import java.util.stream.Collectors;

import timber.log.Timber;

public class ViewPagerFragment extends SelectionFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String BUNDLE_KEY = "for which";
    private boolean forBookmark;

    private BookmarkViewModel mViewModel;
    private RecyclerView mRecyclerView;

    public static ViewPagerFragment getInstance(int which){
        Bundle b = new Bundle(1);
        b.putInt(BUNDLE_KEY, which);
        ViewPagerFragment f = new ViewPagerFragment();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setMenuResource(R.menu.cab_menu);

        forBookmark = getArguments().getInt(BUNDLE_KEY) == BookmarkFragment.BOOKMARK_BOOKMARK;

        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity()
                .getApplication(), requireActivity())).get(BookmarkViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.swiperefresh_loading_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.progress_load).setVisibility(View.GONE);
        mRecyclerView = view.findViewById(R.id.recycler_load);

        GridLayoutManager manager = new GridLayoutManager(requireActivity(), 3);
        mRecyclerView.setLayoutManager(manager);

        MangaListAdapter adapter = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
        mRecyclerView.setAdapter(adapter);

        Timber.i("View Created reached");
        mViewModel.getBookmarks(forBookmark).observe(getViewLifecycleOwner(), dbMangas -> {
            Timber.i("Mangas finally gotten");
            adapter.submitList(new ArrayList<>(dbMangas));
        });

        createTracker(mRecyclerView);
        adapter.setTracker(getTracker());

        ((SwipeRefreshLayout) view.findViewById(R.id.load_refresh)).setOnRefreshListener(this);
        setSwipeRefreshView((SwipeRefreshLayout) view.findViewById(R.id.load_refresh));
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        ArrayList<DBManga> mangas = new ArrayList<>(getTracker().getSelection().size());

        getTracker().getSelection().forEach(c -> {
            MangaListAdapter.ViewHolder viewHolder = (MangaListAdapter.ViewHolder) mRecyclerView.findViewHolderForItemId(c);

            mangas.add((DBManga) ((MangaListAdapter) mRecyclerView.getAdapter())
                    .getCurrentList().get(viewHolder.getBindingAdapterPosition()));
        });

        if(item.getItemId() == R.id.cab_refresh){
            Bundle bund = new Bundle(1);
            bund.putLongArray(UpdateService.MANGAS, mangas.stream().mapToLong(Manga::getId).toArray());

            Intent bookmark = new Intent(requireActivity(), UpdateService.class);
            bookmark.putExtras(bund);

            requireActivity().startService(bookmark);
        }else if(item.getItemId() == R.id.cab_delete){
            mViewModel.deleteBookmarks(mangas);
        }

        return true;
    }

    @Override
    public void onRefresh() {
        Intent bookmarkIntent = new Intent(requireActivity(), UpdateService.class);
        requireActivity().startService(bookmarkIntent);

        ((SwipeRefreshLayout) getView().findViewById(R.id.load_refresh)).setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((SwipeRefreshLayout) getView().findViewById(R.id.load_refresh)).setOnRefreshListener(null);
        mRecyclerView.setAdapter(null);
        mRecyclerView = null;
    }
}
