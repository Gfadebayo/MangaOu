package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.MangaListAdapter;
import com.exzell.mangaplayground.databinding.GenericLoadingRecyclerViewBinding;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import kotlin.Unit;

public class SearchFragment extends SelectionFragment implements SearchDialogFragment.OnSearchClickedListener{
    private SearchViewModel mViewModel;
    private MangaListAdapter mAdapter;
    private GenericLoadingRecyclerViewBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(
                requireActivity().getApplication(), requireActivity())).get(SearchViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);

        mViewModel.handlerDefaults();
        setContextMenuResource(0, R.menu.menu_cab_bookmark);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = GenericLoadingRecyclerViewBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.progressLoad.setVisibility(View.GONE);
        setFab(true);

        mAdapter = new MangaListAdapter(requireContext(), new ArrayList<>(), R.layout.list_manga);
        mAdapter.showSummary(true);

        mBinding.recyclerLoad.setAdapter(mAdapter);

        createTracker(mBinding.recyclerLoad);
        mAdapter.setTracker(getTracker());

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (itemCount > 0) {
                    mBinding.progressLoad.setVisibility(View.GONE);
                    mBinding.recyclerLoad.setVisibility(View.VISIBLE);
                }
            }
        });

        onResultReturned().accept(mViewModel.mCurrentSearchResults);
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        ArrayList<String> mangaLinks = new ArrayList<>(getTracker().getSelection().size());
        getTracker().getSelection().forEach(id -> {
            RecyclerView.ViewHolder vh = mBinding.recyclerLoad.findViewHolderForItemId(id);
            Manga manga = mAdapter.getCurrentList().get(vh.getBindingAdapterPosition());
            mangaLinks.add(manga.getLink());
        });

        mViewModel.createAndBookmarkManga(mangaLinks, () -> {
            Toast.makeText(requireActivity(), "The mangas could not be added", Toast.LENGTH_SHORT).show();
            return Unit.INSTANCE;
        });

        return true;
    }

    @Override
    public void onSearchClicked() {
        mBinding.progressLoad.setVisibility(View.VISIBLE);
        mAdapter.submitList(Collections.emptyList());
        mViewModel.clearSearchResults();
        mViewModel.resolveSearch(mViewModel.search(), onResultReturned());
    }

    private Consumer<List<Manga>> onResultReturned(){
        return mangas -> {
            if(!isVisible()) return;
            requireActivity().runOnUiThread(() -> mAdapter.addMangas(mangas));
            if (mViewModel.getNextLink() != null)
                mViewModel.resolveSearch(mViewModel.getNextLink(), onResultReturned());
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        mBinding = null;

        setFab(false);
    }

    private void setFab(boolean show) {
        ExtendedFloatingActionButton fab = requireActivity().findViewById(R.id.fab);

        if (show) {
            fab.setText(R.string.search);
            fab.setIconResource(R.drawable.ic_round_search_24);
            fab.setOnClickListener(v -> {
                SearchDialogFragment searchDia = SearchDialogFragment.getInstance();
                searchDia.show(getChildFragmentManager(), null);
            });
        } else {
            fab.setOnClickListener(null);
        }
    }
}
