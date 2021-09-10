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
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import kotlin.Unit;

public class SearchFragment extends SelectionFragment implements SearchDialogFragment.OnSearchClickedListener {
    public static final String KEY_SEARCH_TITLE = "com.exzell.mangaplayground.search_title";

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

        mViewModel.setOnSuccess(onResultReturned());
        mViewModel.setOnError(onError());

        if (getArguments() != null && getArguments().containsKey(KEY_SEARCH_TITLE)) {
            mViewModel.resetValues();

            mViewModel.mSearch.setTitle(getArguments().getString(KEY_SEARCH_TITLE));
            mViewModel.saveSearchParams();
            onSearchClicked();
        }
        setContextMenuResource(R.menu.cab_fragment_search, 0);
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
                    mBinding.textOther.setVisibility(View.GONE);
                    mBinding.recyclerLoad.setVisibility(View.VISIBLE);
                }
            }
        });

        onResultReturned().accept(mViewModel.mCurrentSearchResults);
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.action_search_bookmark) {
            ArrayList<String> mangaLinks = new ArrayList<>(getTracker().getSelection().size());
            getTracker().getSelection().forEach(id -> {
                RecyclerView.ViewHolder vh = mBinding.recyclerLoad.findViewHolderForItemId(id);
                Manga manga = mAdapter.getCurrentList().get(vh.getBindingAdapterPosition());
                mangaLinks.add(manga.getLink());
            });

            mViewModel.createAndBookmarkManga(mangaLinks, () -> {
                Toast.makeText(requireActivity(), R.string.failed_add_mangas, Toast.LENGTH_SHORT).show();
                return Unit.INSTANCE;
            });
        }

        return true;
    }

    @Override
    public void onSearchClicked() {
        mBinding.progressLoad.setVisibility(View.VISIBLE);
        mAdapter.submitList(Collections.emptyList());
        mViewModel.clearSearchResults();
        mViewModel.resolveSearch(mViewModel.search());
    }

    private Consumer<List<Manga>> onResultReturned() {
        return mangas -> {
            if (!isVisible()) return;
            mAdapter.addMangas(mangas);
            if (mViewModel.getNextLink() != null)
                mViewModel.resolveSearch(mViewModel.getNextLink());
        };
    }

    private Consumer<String> onError() {
        return message -> {
            mBinding.progressLoad.setVisibility(View.GONE);

            if (message.equals(SearchViewModel.ERROR_OTHERS)) {
                Snackbar.make(mBinding.getRoot(), R.string.error_try_again, Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry, v ->
                        mViewModel.resolveSearch(mViewModel.search()))
                        .setAnchorView(R.id.bottom_nav_view).show();

            } else if (message.equals(SearchViewModel.ERROR_NO_RESULT)) {
                mBinding.textOther.setVisibility(View.VISIBLE);
                mBinding.textOther.setText(R.string.error_not_found);
            }
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
        if (fab == null) return;

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
