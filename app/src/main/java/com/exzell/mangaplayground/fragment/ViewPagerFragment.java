package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.UpdateService;
import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerFragment extends SelectionFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ViewPagerFragment";
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

        mViewModel.getBookmarks(forBookmark).observe(this, dbMangas -> {
            adapter.clearMangas();
            adapter.addMangas(dbMangas);
        });

        mRecyclerView.setAdapter(adapter);
        createTracker(mRecyclerView);
        adapter.setTracker(getTracker());

        ((SwipeRefreshLayout) view.findViewById(R.id.load_refresh)).setOnRefreshListener(this);
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        return false;
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
    }
}
