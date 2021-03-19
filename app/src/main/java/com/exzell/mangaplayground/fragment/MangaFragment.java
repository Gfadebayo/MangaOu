package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.ChapterAdapter;
import com.exzell.mangaplayground.adapters.TitleAdapter;
import com.exzell.mangaplayground.databinding.FragmentMangaBinding;
import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.reader.ReadActivity;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.utils.FileUtilsKt;
import com.exzell.mangaplayground.viewmodels.MangaViewModel;
import com.exzell.mangaplayground.viewmodels.factory.MangaModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.Unit;

public class MangaFragment extends SelectionFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String MANGA_LINK = "new manga";
    public static final String AUTO_UPDATE = "start update";

    private FragmentMangaBinding mBinding;
    private ConcatAdapter mAdapter;
    private MangaViewModel mViewModel;
    private boolean doAutomaticUpdate = true;

    private Snackbar mErrorSnackbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MangaApplication app = (MangaApplication) requireActivity().getApplication();

        doAutomaticUpdate = !getArguments().containsKey(AUTO_UPDATE) || getArguments().getBoolean(AUTO_UPDATE);

        String link = getArguments().getString(MANGA_LINK);
        mViewModel = new ViewModelProvider(this, new MangaModelFactory(app, link)).get(MangaViewModel.class);

        app.mAppComponent.injectRepo(mViewModel);

        //Called here as fetching requires the repo to be available
        //which is out of the viewmodel's hand
        mViewModel.fetchMangaInfo();

        setMenuResource(R.menu.chapter_menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMangaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.swipeRefresh.setRefreshing(doAutomaticUpdate);
        //Access the network to re-update manga information
        //if there's no internet connection, simply display the bookmarked manga

        onComplete();

        if (doAutomaticUpdate)
            addDisposable(mViewModel.updateManga(noError -> {
                mBinding.swipeRefresh.setRefreshing(false);

                if (noError) onComplete();
                    //Only display the snackbar when no existing manga is available
                else if (mViewModel.getManga() == null) errorSnackBar();

                return Unit.INSTANCE;
            }));

        mBinding.swipeRefresh.setOnRefreshListener(this);
        setSwipeRefreshView(mBinding.swipeRefresh, mBinding.scrollView);
    }

    private void errorSnackBar() {
        if (mErrorSnackbar == null) {
            mErrorSnackbar = Snackbar.make(getView(), "Failed!, Please check your connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", v -> onRefresh());
        }
        mErrorSnackbar.show();
    }

    private void onComplete() {
        if (mViewModel.getManga() == null) return;

        mBinding.root.setVisibility(View.VISIBLE);

        mBinding.setManga(mViewModel.getManga());

        ConcatAdapter.Config config = new ConcatAdapter.Config.Builder()
                .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS).setIsolateViewTypes(false).build();

        createTitleAdapter(config, mViewModel.getManga().getChapters());

        mBinding.bookmark.setOnClickListener(v -> {
            boolean bookmark = mViewModel.alterBookmark();

            String book = bookmark ? "Manga bookmarked successfully" : "Manga removed from bookmark";
            Toast.makeText(requireActivity(), book, Toast.LENGTH_SHORT).show();
        });

        mViewModel.getDownloads(this, downloads -> downloads.forEach(d -> {
            mViewModel.getManga().getChapters().stream().filter(p -> p.getId() == d.getChapterId())
                    .findAny().ifPresent(chap -> {
                chap.setDownloadState(d.getState());

//                int chapIndex = mManga.getChapters().indexOf(chap);
//                mManga.getChapters()
//                mAdapter.notifyItemChanged(chapIndex);
            });
        }));
    }

    private void clearConcatAdapter(){
        if(mAdapter == null) return;

        List<? extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> adapters = mAdapter.getAdapters();
        for(RecyclerView.Adapter<? extends RecyclerView.ViewHolder> r : adapters){
            mAdapter.removeAdapter(r);
        }
    }

    private void createTitleAdapter(ConcatAdapter.Config config, List<Chapter> chapters) {
        clearConcatAdapter();

        List<Chapter.Version> versions = chapters.stream().map(Chapter::getVersion)
                .distinct().collect(Collectors.toList());

        for (Chapter.Version ver : versions) {
            List<Chapter> versionChapters = chapters.stream()
                    .filter(p -> p.getVersion().equals(ver)).collect(Collectors.toList());
            String title = ver.getDispName() + "(" + versionChapters.size() + " Chapters" + ")";

            versionChapters.sort((o1, o2) -> Integer.compare(o2.getPosition(), o1.getPosition()));

            ChapterAdapter versionAdapter = new ChapterAdapter(requireActivity(), versionChapters);
            versionAdapter.setListener(showChapter());

//
            TitleAdapter titleAdapter = new TitleAdapter(requireActivity(), title, versionAdapter);

            titleAdapter.setDrawableResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            titleAdapter.setParentListener(onHeaderClicked());

            if (mAdapter == null) {
                mAdapter = new ConcatAdapter(config, titleAdapter);
                mBinding.recyclerChapters.setAdapter(mAdapter);
                createTracker(mBinding.recyclerChapters);

            } else mAdapter.addAdapter(titleAdapter);

            //This needs to be called after the adapter is set
            // since the tracker requires the recycler view to have its adapter before creation
            versionAdapter.addTracker(getTracker());
        }
    }

    private View.OnClickListener onHeaderClicked() {
        return v -> {
            RecyclerView.ViewHolder holder = mBinding.recyclerChapters.findContainingViewHolder(v);
            TitleAdapter adapter = (TitleAdapter) holder.getBindingAdapter();
            boolean expand = true;

            if (mAdapter.getAdapters().contains(adapter.getBodyAdapter())) {
                mAdapter.removeAdapter(adapter.getBodyAdapter());
                expand = false;
            } else {
                int index = mAdapter.getAdapters().indexOf(adapter);

                mAdapter.addAdapter(index + 1, adapter.getBodyAdapter());
            }

            v.findViewById(R.id.button_header).setSelected(expand);
        };
    }

    private View.OnClickListener showChapter(){
        return v -> {

            ChapterAdapter.ChapterViewHolder vh = (ChapterAdapter.ChapterViewHolder) mBinding.recyclerChapters
                    .findContainingViewHolder(v);

            int index = vh.getBindingAdapterPosition();

            Chapter chap = ((ChapterAdapter) vh.getBindingAdapter()).getCurrentList().get(index);

            Intent intent = new Intent(requireContext(), ReadActivity.class);

            intent.putExtra(ReadActivity.CHAPTER, chap.getId());

            startActivity(intent);
        };
    }

    @Override
    public boolean onActionItemClicked(MenuItem item) {

        List<Chapter> chosen = new ArrayList<>();

        getTracker().getSelection().forEach(c -> {
            ChapterAdapter.ChapterViewHolder hold = (ChapterAdapter.ChapterViewHolder) mBinding.recyclerChapters.findViewHolderForItemId(c);
            int bindPos = hold.getBindingAdapterPosition();

            List<Chapter> chaps = ((ChapterAdapter) hold.getBindingAdapter()).getCurrentList();
            Chapter chap = chaps.get(bindPos);

            chosen.add(chap);
        });

        if(item.getItemId() == R.id.action_chapter_bookmark){

            chosen.forEach(c -> c.setBookmarked(true));
            mViewModel.updateChapter(chosen);

        }else if(item.getItemId() == R.id.action_chapter_download){

            List<Download> downloads = chosen.stream().map(chap -> {
                String parentDir = mViewModel.getManga().getTitle() + "/" + chap.getVersion();
                String dir = FileUtilsKt.createDownloadFolder(requireActivity(), parentDir, chap.getNumber());

                return new Download(chap.getId(), mViewModel.getManga().getTitle(),
                        chap.getNumber(), dir, chap.getLink(), chap.getLength());
            }).collect(Collectors.toList());

            mViewModel.queueDownloads(downloads);
        }
        return true;
    }

    @Override
    public void onRefresh() {
        mViewModel.updateManga(noError -> {
            mBinding.swipeRefresh.setRefreshing(false);

            if (noError) onComplete();
            else errorSnackBar();

            return Unit.INSTANCE;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mErrorSnackbar != null) mErrorSnackbar.dismiss();
        mErrorSnackbar = null;
        mAdapter = null;
        mBinding = null;
    }
}
