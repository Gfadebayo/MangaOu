package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.MangaListAdapter;
import com.exzell.mangaplayground.adapter.RecyclerViewAdapter;
import com.exzell.mangaplayground.adapter.TitleAdapter;
import com.exzell.mangaplayground.databinding.FragmentHomeBinding;
import com.exzell.mangaplayground.fragment.base.SwipeRefreshFragment;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class HomeFragment extends SwipeRefreshFragment implements SwipeRefreshLayout.OnRefreshListener {

    private FragmentHomeBinding mBinding;
    private HomeViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity()
                .getApplication(), this)).get(HomeViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setSwipeRefreshView(mBinding.swipeRefresh, mBinding.recyclerHome);
        super.onViewCreated(view, savedInstanceState);

        //index 0 is Popular Updates, 1 is Latest Updates, 2 is Bookmarks and 3 is Downloads
        List<String> names = Arrays.asList(getResources().getStringArray(R.array.home_names));

        names.forEach(name -> {
            int index = names.indexOf(name);

            String errorText = index < 2 ? getString(R.string.error_fetch) : getString(R.string.error_not_found);


            MangaListAdapter ad = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
            RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(ad, requireActivity(), errorText,
                    new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

            TitleAdapter titleAd = new TitleAdapter(requireActivity(), name, rvAdapter);
            titleAd.setParentListener(v -> getLink(index, name, names));

            if (index == 1) ad.showMoreInfo(true);

            if (mBinding.recyclerHome.getAdapter() == null) {
                ConcatAdapter adapter = new ConcatAdapter(titleAd);
                mBinding.recyclerHome.setAdapter(adapter);

            } else ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).addAdapter(titleAd);

            ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).addAdapter(rvAdapter);
        });

        onRefresh();

        mBinding.swipeRefresh.setOnRefreshListener(this);
    }

    private BiConsumer<List<Manga>, Pair<Integer, Boolean>> consumer() {

        return (manga, which) -> {
            if (isAdded()) {
                RecyclerViewAdapter rvAdapter = (RecyclerViewAdapter) ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).getAdapters().get(which.first);
                rvAdapter.progressBarVisiblity(false);

                MangaListAdapter adapter = (MangaListAdapter) rvAdapter.getMViewAdapter();
                if (which.second) {

                    adapter.submitList(null);
                    adapter.submitList(new ArrayList<>(manga));

                } else if (adapter.getItemCount() == 0) {
                    //which.second already confirmed to be false
                    rvAdapter.textVisiblilty(true);
                }
            }
        };
    }

    private void getLink(int index, String title, List<String> headers) {
        NavController control = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        if (headers.get(index).equals(getString(R.string.bookmarks)) || headers.get(index).equals(getString(R.string.downloads))) {
            boolean toBookmark = index == headers.indexOf(getString(R.string.bookmarks));

            Bundle b = new Bundle(1);
            b.putInt(BookmarkFragment.KEY_PAGE, toBookmark ? BookmarkFragment.PAGE_BOOKMARK : BookmarkFragment.PAGE_BOOKMARK + 1);

            control.navigate(R.id.action_nav_home_to_nav_bookamrk, b);
        } else {
            String link = index == 0 ? "/popular" : "/latest";

            Bundle linkBund = new Bundle(2);

            linkBund.putString(EmptyFragment.LINK, link);
            linkBund.putString(EmptyFragment.TITLE, title);
            control.navigate(R.id.action_nav_home_to_nav_empty, linkBund);
        }
    }

    @Override
    public void onRefresh() {
        mBinding.swipeRefresh.setRefreshing(false);

        ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).getAdapters()
                .forEach(adapter -> {
                    if (!(adapter instanceof RecyclerViewAdapter)) return;

                    if (((RecyclerViewAdapter) adapter).isTextVisible()) {
                        ((RecyclerViewAdapter) adapter).textVisiblilty(false);
                        ((RecyclerViewAdapter) adapter).progressBarVisiblity(true);
                    }
                });

        BiConsumer<List<Manga>, Pair<Integer, Boolean>> consumer = consumer();
        mViewModel.parseHome(consumer, 1, 3);
        mViewModel.queryDb(consumer, 5, 7);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.swipeRefresh.setOnRefreshListener(null);
        mBinding = null;
    }
}
