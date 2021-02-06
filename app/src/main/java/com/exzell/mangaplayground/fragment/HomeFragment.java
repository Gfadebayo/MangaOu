package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.MangaListAdapter;
import com.exzell.mangaplayground.adapters.RecyclerViewAdapter;
import com.exzell.mangaplayground.adapters.TitleAdapter;
import com.exzell.mangaplayground.databinding.FragmentHomeBinding;
import com.exzell.mangaplayground.fragment.base.DisposableFragment;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class HomeFragment extends DisposableFragment implements SwipeRefreshLayout.OnRefreshListener {

    private FragmentHomeBinding mBinding;
    private HomeViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mViewModel = new ViewModelProvider(this, new SavedStateViewModelFactory(requireActivity()
                .getApplication(), this)).get(HomeViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.bind(view);

        String[] names = getResources().getStringArray(R.array.home_names);

        Arrays.stream(names).forEach(s -> {

            MangaListAdapter ad = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
            RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(ad, requireActivity(),
                    new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

            TitleAdapter titleAd = new TitleAdapter(requireActivity(), s, rvAdapter);
            titleAd.setImageListener(v -> getLink(s));
            titleAd.setDrawableResource(R.drawable.ic_arrow_forward_black_24dp);

            if (s.equals("Latest")) ad.showMoreInfo(true);

            if (mBinding.recyclerHome.getAdapter() == null) {
                ConcatAdapter adapter = new ConcatAdapter(titleAd);
                mBinding.recyclerHome.setAdapter(adapter);

            } else ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).addAdapter(titleAd);

            ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).addAdapter(rvAdapter);
        });

        onRefresh();

        mBinding.swipeRefresh.setOnRefreshListener(this);
        setSwipeRefreshView(mBinding.swipeRefresh, mBinding.recyclerHome);
    }

    private BiConsumer<List<? extends Manga>, Integer> consumer() {

        return (manga, which) -> {
            List<? extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> adapters = ((ConcatAdapter) mBinding.recyclerHome.getAdapter()).getAdapters();

            if (isAdded())
                requireActivity().runOnUiThread(() -> {
                    MangaListAdapter adapter = (MangaListAdapter) ((RecyclerViewAdapter) adapters.get(which)).getMViewAdapter();

                    adapter.submitList(null);
                    adapter.submitList(new ArrayList<>(manga));

                    ((RecyclerViewAdapter) adapters.get(which)).hideProgressBar(true);
                });
        };
    }

    private void getLink(String name) {
        NavController control = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        if (name.contains("Bookmark") || name.contains("Download"))
            control.navigate(R.id.action_nav_home_to_nav_bookamrk);
        else {
            String link = name.equalsIgnoreCase("popular") ? "/popular" : "/latest";
            String title = name.equalsIgnoreCase("popular") ? "Popular" : "Latest";

            Bundle linkBund = new Bundle(2);

            linkBund.putString(EmptyFragment.LINK, link);
            linkBund.putString(EmptyFragment.TITLE, title);
            control.navigate(R.id.action_nav_home_to_nav_empty, linkBund);
        }
    }

    @Override
    public void onRefresh() {

        BiConsumer<List<? extends Manga>, Integer> consumer = consumer();
        mViewModel.parseHome(consumer, 1, 3);
        mViewModel.queryDb(consumer, 5, 7, this);

        mBinding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.swipeRefresh.setOnRefreshListener(null);
    }
}
