package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.UpdateService;
import com.exzell.mangaplayground.advancedsearch.MangaSearch;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.advancedsearch.Order;
import com.exzell.mangaplayground.databinding.FragmentSearchBinding;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.tiper.MaterialSpinner;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import timber.log.Timber;

public class SearchFragment extends SelectionFragment implements SearchDialogFragment.OnSearchClickedListener{
    private SearchViewModel mViewModel;
    private MangaListAdapter mAdapter;
    private FragmentSearchBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(
                requireActivity().getApplication(), requireActivity())).get(SearchViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);

        mViewModel.handlerDefaults();
        setMenuResource(R.menu.cab_menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSearchBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().findViewById(R.id.fab).setOnClickListener(v -> {
            SearchDialogFragment searchDia = SearchDialogFragment.getInstance();
            searchDia.show(getChildFragmentManager(), null);
        });

        configureOrder();

        mAdapter = new MangaListAdapter(requireContext(), new ArrayList<>(), R.layout.list_manga);
        mAdapter.showSummary(true);

        mBinding.recyclerSearchResult.setAdapter(mAdapter);

        createTracker(mBinding.recyclerSearchResult);
        mAdapter.setTracker(getTracker());

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (itemCount > 0) {
                    view.findViewById(R.id.progress_search).setVisibility(View.GONE);
                    mBinding.recyclerSearchResult.setVisibility(View.VISIBLE);
                }
            }
        });

        onResultReturned().accept(mViewModel.getCurrentSearchResults());
    }

    private void configureOrder(){
        PowerSpinnerView spin = mBinding.spinnerOrder.getRoot();

        List<String> data = Stream.of(Order.values()).map(order -> order.dispName).collect(Collectors.toList());
        spin.setItems(data);

        int index = data.indexOf(mViewModel.getOrder());
        if(index != -1) spin.selectItemByIndex(index);

        spin.setSpinnerOutsideTouchListener((view, motionEvent) -> spin.dismiss());
        spin.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
            mViewModel.setOrder(s);
            onSearchClicked();
        });
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        ArrayList<String> mangaLinks = new ArrayList<>(getTracker().getSelection().size());
        getTracker().getSelection().forEach(id -> {
            RecyclerView.ViewHolder vh = mBinding.recyclerSearchResult.findViewHolderForItemId(id);
            Manga manga = mAdapter.getCurrentList().get(vh.getBindingAdapterPosition());
            mangaLinks.add(manga.getLink());
        });

        Intent createMangaIntent = new Intent(requireContext(), UpdateService.class);

        createMangaIntent.putStringArrayListExtra(UpdateService.CREATE_MANGAS, mangaLinks);
        requireActivity().startService(createMangaIntent);
        return true;
    }

    @Override
    public void onSearchClicked() {
        getView().findViewById(R.id.progress_search).setVisibility(View.VISIBLE);
        mAdapter.submitList(Collections.emptyList());
        mViewModel.clearSearchResults();
        Map<String, String> search = mViewModel.search();
        mViewModel.resolveSearch(search, onResultReturned());
    }

    private Consumer<List<Manga>> onResultReturned(){
        return mangas -> {
            if(!isVisible()) return;
            requireActivity().runOnUiThread(() -> mAdapter.addMangas(mangas));
            if(mViewModel.getNextLink() != null) mViewModel.resolveSearch(mViewModel.getNextLink(), onResultReturned());
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        mBinding = null;
        requireActivity().findViewById(R.id.fab).setOnClickListener(null);
    }
}
