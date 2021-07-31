package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.UpdateService;
import com.exzell.mangaplayground.adapter.MangaListAdapter;
import com.exzell.mangaplayground.databinding.SwiperefreshLoadingRecyclerViewBinding;
import com.exzell.mangaplayground.fragment.base.SearchViewFragment;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.viewmodels.BookmarkViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class BookmarkViewPagerFragment extends SearchViewFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String BUNDLE_KEY = "for which";
    private boolean forBookmark;

    private BookmarkViewModel mViewModel;
    private SwiperefreshLoadingRecyclerViewBinding mBinding;

    public static BookmarkViewPagerFragment getInstance(int which) {
        Bundle b = new Bundle(1);
        b.putInt(BUNDLE_KEY, which);
        BookmarkViewPagerFragment f = new BookmarkViewPagerFragment();
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContextMenuResource(0, R.menu.menu_cab_bookmark);

        forBookmark = getArguments().getInt(BUNDLE_KEY) == BookmarkFragment.PAGE_BOOKMARK;

        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity()
                .getApplication(), requireActivity())).get(BookmarkViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = SwiperefreshLoadingRecyclerViewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setSwipeRefreshView(mBinding.loadRefresh, mBinding.recyclerLoad);
        super.onViewCreated(view, savedInstanceState);
        mBinding.progressLoad.setVisibility(View.GONE);

        boolean isPortrait = requireContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        GridLayoutManager manager = new GridLayoutManager(requireActivity(), isPortrait ? 3 : 5);
        mBinding.recyclerLoad.setLayoutManager(manager);

        MangaListAdapter adapter = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
        mBinding.recyclerLoad.setAdapter(adapter);

        mViewModel.getBookmarks(forBookmark, consume(adapter));

        createTracker(mBinding.recyclerLoad);
        adapter.setTracker(getTracker());

        mBinding.loadRefresh.setOnRefreshListener(this);

        setSearchListeners(filter(adapter), () -> {
            filter(adapter).invoke("");
            return Unit.INSTANCE;
        });
    }

    private Function1<String, Unit> filter(MangaListAdapter adapter) {

        return keyword -> {
            List<Manga> mangas = new ArrayList<>(mViewModel.getMMangas());

            adapter.submitList(mangas.stream().filter(manga -> manga.getTitle().toLowerCase()
                    .contains(keyword)).collect(Collectors.toList()));

            return Unit.INSTANCE;
        };
    }

    private Consumer<List<Manga>> consume(MangaListAdapter adapter) {
        return dbMangas -> adapter.submitList(dbMangas);
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        ArrayList<Manga> mangas = new ArrayList<>(getTracker().getSelection().size());

        getTracker().getSelection().forEach(c -> {
            MangaListAdapter.ViewHolder viewHolder = (MangaListAdapter.ViewHolder) mBinding.recyclerLoad.findViewHolderForItemId(c);

            mangas.add(((MangaListAdapter) mBinding.recyclerLoad.getAdapter())
                    .getCurrentList().get(viewHolder.getBindingAdapterPosition()));
        });

        if(item.getItemId() == R.id.cab_refresh){
            Bundle bund = new Bundle(1);
            bund.putLongArray(UpdateService.UPDATE_MANGAS, mangas.stream().mapToLong(Manga::getId).toArray());

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
        mBinding.loadRefresh.setOnRefreshListener(null);
        mBinding.recyclerLoad.setAdapter(null);
        mBinding = null;
    }
}
