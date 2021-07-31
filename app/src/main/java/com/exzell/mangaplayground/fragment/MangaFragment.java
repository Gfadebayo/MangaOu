package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.MangaInfoAdapter;
import com.exzell.mangaplayground.databinding.SwiperefreshLoadingRecyclerViewBinding;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Download;
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

    private SwiperefreshLoadingRecyclerViewBinding mBinding;
    private MangaViewModel mViewModel;

    //give users the option to choose in the Settings
    private boolean autoUpdate = true;

    private Snackbar mErrorSnackbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MangaApplication app = (MangaApplication) requireActivity().getApplication();

        autoUpdate = !getArguments().containsKey(AUTO_UPDATE) || getArguments().getBoolean(AUTO_UPDATE);

        String link = getArguments().getString(MANGA_LINK);
        mViewModel = new ViewModelProvider(this, new MangaModelFactory(app, link)).get(MangaViewModel.class);

        app.mAppComponent.injectRepo(mViewModel);

        //Called here instead of in the init block as fetching requires the repo to be available
        //which is out of the viewmodel's hand
        mViewModel.fetchMangaInfo();

        setContextMenuResource(0, R.menu.menu_chapter);
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
        mBinding.loadRefresh.setRefreshing(autoUpdate);
        //Access the network to re-update manga information
        //if there's no internet connection, simply display the bookmarked manga

        MangaInfoAdapter adapter = new MangaInfoAdapter(requireContext(), mViewModel.getManga());
        adapter.setBookmarkListener(v -> {
            boolean bookmark = mViewModel.alterBookmark();

            String book = getString(bookmark ? R.string.bookmark_add : R.string.bookmark_remove);
            Toast.makeText(requireActivity(), book, Toast.LENGTH_SHORT).show();
        });

        mBinding.recyclerLoad.setAdapter(adapter);
        createTracker(mBinding.recyclerLoad);
        adapter.addTracker(getTracker());

        onComplete();

        if (autoUpdate)
            mViewModel.updateManga(noError -> {
                mBinding.loadRefresh.setRefreshing(false);

                if (noError) onComplete();
                    //Only display the snackbar when no existing manga is available
                else errorSnackBar();

                return Unit.INSTANCE;
            });

        mBinding.loadRefresh.setOnRefreshListener(this);
    }

    private void errorSnackBar() {
        if (mErrorSnackbar == null) {
            String message = requireActivity().getResources().getString(R.string.error_fetch);
            String retry = requireActivity().getResources().getString(R.string.retry);
            mErrorSnackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(retry, v -> {
                        mBinding.loadRefresh.setRefreshing(true);
                        onRefresh();
                        mErrorSnackbar.dismiss();
                    });
        }
        mErrorSnackbar.show();
    }

    private void onComplete() {
        if (mViewModel.getManga() == null) return;

        MangaInfoAdapter adapter = (MangaInfoAdapter) mBinding.recyclerLoad.getAdapter();

        adapter.updateMangaInfo(mViewModel.getManga());


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

    @Override
    public boolean onActionItemClicked(MenuItem item) {
        List<Chapter> chosen = new ArrayList<>();

        getTracker().getSelection().forEach(c -> {
//            MangaInfoAdapter.ChapterViewHolder hold = (MangaInfoAdapter.ChapterViewHolder) mBinding.recyclerLoad.findViewHolderForItemId(c);
//            int bindPos = hold.getBindingAdapterPosition();
//
//            List<Chapter> chaps = ((MangaInfoAdapter) hold.getBindingAdapter()).getCurrentList();
            Chapter chap = mViewModel.getManga().getChapters().stream().filter(ch -> c == ch.getId()).findFirst().get();
//            Chapter chap = chaps.get(bindPos);

            chosen.add(chap);
        });

        if (item.getItemId() == R.id.action_chapter_bookmark) {

            chosen.forEach(c -> c.setBookmarked(true));
            mViewModel.updateChapter(chosen);

            return true;
        } else if (item.getItemId() == R.id.action_download) {

            List<Download> downloads = chosen.stream().map(chap -> {
                String parentDir = mViewModel.getManga().getTitle() + "/" + chap.getVersion();
                String dir = FileUtilsKt.createDownloadFolder(requireActivity(), parentDir, String.valueOf(chap.getNumberString()));

                return new Download(chap.getId(), mViewModel.getManga().getTitle(),
                        String.valueOf(chap.getNumberString()), dir, chap.getLink(), chap.getLength(), mViewModel.getManga().getId());
            }).collect(Collectors.toList());

            mViewModel.queueDownloads(downloads);

            return true;
        } else if (item.getItemId() == R.id.action_done) {

            chosen.forEach(c -> c.setCompleted(true));
            mViewModel.updateChapter(chosen);
        }

        return false;
    }

    @Override
    public void onRefresh() {
        mViewModel.updateManga(noError -> {
            mBinding.loadRefresh.setRefreshing(false);

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
        mBinding = null;
    }
}
