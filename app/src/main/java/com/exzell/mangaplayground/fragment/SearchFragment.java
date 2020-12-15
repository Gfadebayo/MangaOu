package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.advancedsearch.MangaSearch;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.advancedsearch.Order;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchFragment extends SelectionFragment implements SearchDialogFragment.OnSearchClickedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchViewModel mViewModel;
    private RecyclerView mRecyclerView;
    private MangaListAdapter mAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(
                requireActivity().getApplication(), requireActivity())).get(SearchViewModel.class);
        mViewModel.handlerDefaults();
        setMenuResource(R.menu.cab_menu);
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.fab_search).setOnClickListener(v -> {
            SearchDialogFragment searchDia = SearchDialogFragment.getInstance();
            searchDia.show(getChildFragmentManager(), null);
        });

        configureOrder(view);

        mRecyclerView = view.findViewById(R.id.recycler_search_result);

        mAdapter = new MangaListAdapter(requireContext(), new ArrayList<>(), R.layout.list_manga);
        mAdapter.showSummary(true);

        mRecyclerView.setAdapter(mAdapter);

        createTracker(mRecyclerView);
        mAdapter.setTracker(getTracker());

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (itemCount > 0) {
                    view.findViewById(R.id.progress_search).setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        onResultReturned().accept(mViewModel.getCurrentSearchResults());

        ((SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh)).setOnRefreshListener(this);
    }

    private void configureOrder(View parent){
        PowerSpinnerView spin = parent.findViewById(R.id.spinner_order);

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
    public void onSearchClicked() {
        getView().findViewById(R.id.progress_search).setVisibility(View.VISIBLE);
        mAdapter.clearMangas();
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
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onRefresh() {
        ((SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh)).setRefreshing(false);
        onSearchClicked();
    }
}
