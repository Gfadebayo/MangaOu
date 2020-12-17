package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
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

        mRecyclerView = view.findViewById(R.id.recycler_home);

        String[] names = getResources().getStringArray(R.array.home_names);

        Arrays.stream(names).forEach(s -> {

            MangaListAdapter ad = new MangaListAdapter(requireActivity(), new ArrayList<>(), R.layout.list_manga_home);
            RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(ad, requireActivity(),
                    new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

            TitleAdapter titleAd = new TitleAdapter(requireActivity(), s, rvAdapter);
            titleAd.setImageListener(v -> getLink(s));
            titleAd.setDrawableResource(R.drawable.ic_arrow_forward_black_24dp);

            if (s.equals("Latest")) ad.showMoreInfo(true);

            if (mRecyclerView.getAdapter() == null) {
                ConcatAdapter adapter = new ConcatAdapter(titleAd);
                mRecyclerView.setAdapter(adapter);

            } else ((ConcatAdapter) mRecyclerView.getAdapter()).addAdapter(titleAd);

            ((ConcatAdapter) mRecyclerView.getAdapter()).addAdapter(rvAdapter);
        });

        onRefresh();

        ((SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh)).setOnRefreshListener(this);
    }

//    private BiConsumer<Manga, Integer> ioConsumer() {
//
//        return (manga, which) -> {
//            List<? extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> adapters = ((ConcatAdapter) mRecyclerView.getAdapter()).getAdapters();
//
//            MangaListAdapter adapter = (MangaListAdapter) ((RecyclerViewAdapter) adapters.get(which)).getMViewAdapter();
//
//            requireActivity().runOnUiThread(() -> {
//                //Swipe refresh possibility
////                if (adapter.getItemCount() > 0) adapter.clearMangas();
//
//                adapter.addManga(manga);
//
//                ((RecyclerViewAdapter) adapters.get(which)).hideProgressBar(true);
//            });
//        };
//    }

    private BiConsumer<List<? extends Manga>, Integer> consumer() {

        return (manga, which) -> {
            List<? extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> adapters = ((ConcatAdapter) mRecyclerView.getAdapter()).getAdapters();

            requireActivity().runOnUiThread(() -> {
                MangaListAdapter adapter = (MangaListAdapter) ((RecyclerViewAdapter) adapters.get(which)).getMViewAdapter();
                if (adapter.getItemCount() > 0) adapter.clearMangas();

                adapter.addMangas(manga);

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
            Bundle linkBund = new Bundle(1);
            linkBund.putString(EmptyFragment.TAG, link);
            control.navigate(R.id.action_nav_home_to_nav_empty, linkBund);
        }
    }

    @Override
    public void onRefresh() {

        BiConsumer<List<? extends Manga>, Integer> consumer = consumer();
        mViewModel.parseHome(consumer, 1, 3);
        mViewModel.queryDb(consumer, 5, 7, this);

        ((SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh)).setRefreshing(false);
    }
}
