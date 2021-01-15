package com.exzell.mangaplayground.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.reader.ReadActivity;
import com.exzell.mangaplayground.download.Download;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.ChapterAdapter;
import com.exzell.mangaplayground.adapters.TitleAdapter;
import com.exzell.mangaplayground.databinding.FragmentMangaBinding;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.utils.ChapterUtils;
import com.exzell.mangaplayground.utils.FileUtilsKt;

import com.exzell.mangaplayground.viewmodels.MangaViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class MangaFragment extends SelectionFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String MANGA_LINK = "new manga";

    private Manga mManga;
    private FragmentMangaBinding mBinding;
    private ConcatAdapter mAdapter;
    private MangaViewModel mViewModel;
    private boolean isDoneFetching;
    private boolean doAutomaticUpdate = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory
                (requireActivity().getApplication())).get(MangaViewModel.class);

        ((MangaApplication) requireActivity().getApplication())
                .mAppComponent.injectRepo(mViewModel);

        setMenuResource(R.menu.chapter_menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = FragmentMangaBinding.bind(view);
        mBinding.setLifecycleOwner(this);
        mBinding.swipeRefresh.setRefreshing(true);
        //Access the network to re-update manga information
        //if there's no internet connection, simply display the bookmarked manga

        String link = getArguments().getString(MANGA_LINK);
        mManga = mViewModel.getDbManga(link);

        if (mManga != null) {
            onComplete();
            if (doAutomaticUpdate) updateManga(link);

        } else updateManga(link);

        mBinding.swipeRefresh.setOnRefreshListener(this);
        setSwipeRefreshView(mBinding.swipeRefresh);
    }

    private void updateManga(String link){
        if(isDoneFetching) return;

        List<Chapter> oldChapters = new ArrayList<>();
        if(mManga != null) oldChapters.addAll(mManga.getChapters());

        Consumer<Manga> onNext = manga -> {
            if(mManga != null){
                manga.setId(mManga.getId());
                manga.setBookmark(mManga.isBookmark());
            }
            mManga = manga;
        };

        Action onComplete = () -> {
            List<Chapter> updatedChaps = ChapterUtils.transferChapterInfo(mManga.getChapters(), oldChapters);
            mManga.setChapters(updatedChaps);

            mViewModel.updateDB(mManga);
            isDoneFetching = true;
            onComplete();
        };

        addDisposable(mViewModel.fetchMangaInfo(link, onNext, onComplete));
    }

    private void onComplete(){

        mBinding.swipeRefresh.setRefreshing(false);
        mBinding.root.setVisibility(View.VISIBLE);

        mBinding.setManga(mManga);

        ConcatAdapter.Config config = new ConcatAdapter.Config.Builder()
                .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS).setIsolateViewTypes(false).build();

        createTitleAdapter(config);

        mBinding.bookmark.setOnClickListener(v -> {
            mManga.setBookmark(!mManga.isBookmark());
            mViewModel.bookmarkManga(mManga);

            String book = mManga.isBookmark() ? "Manga bookmarked successfully" : "Manga removed from bookmark";
            Toast.makeText(requireActivity(), book, Toast.LENGTH_SHORT).show();
        });

        mViewModel.getDownloads(this, downloads -> downloads.forEach(d -> {
            mManga.getChapters().stream().filter(p -> p.getId() == d.getChapterId())
                    .findAny().ifPresent(chap -> {
                chap.setDownloadState(d.getState());

//                int chapIndex = mManga.getChapters().indexOf(chap);
//                mManga.getChapters()
//                mAdapter.notifyItemChanged(chapIndex);
            });
        }));

        mBinding.swipeRefresh.setRefreshing(false);
    }

    private void clearConcatAdapter(){
        if(mAdapter == null) return;

        List<? extends RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> adapters = mAdapter.getAdapters();
        for(RecyclerView.Adapter<? extends RecyclerView.ViewHolder> r : adapters){
            mAdapter.removeAdapter(r);
        }
    }

    private void createTitleAdapter(ConcatAdapter.Config config){
        clearConcatAdapter();

        List<Chapter.Version> versions = mManga.getChapters().stream().map(Chapter::getVersion)
                .distinct().collect(Collectors.toList());

        for (Chapter.Version ver : versions) {
            List<Chapter> versionChapters = mManga.getChapters().stream()
                    .filter(p -> p.getVersion().equals(ver)).collect(Collectors.toList());
            String title = ver.getDispName() + "(" + versionChapters.size() + " Chapters" + ")";

            versionChapters.sort((o1, o2) -> Integer.compare(o2.getPosition(), o1.getPosition()));

            ChapterAdapter versionAdapter = new ChapterAdapter(requireActivity(), versionChapters);
            versionAdapter.setListener(showChapter());

//
            TitleAdapter titleAdapter = new TitleAdapter(requireActivity(), title, versionAdapter);

            titleAdapter.setDrawableResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            titleAdapter.setParentListener(onHeaderClicked(titleAdapter));

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

    private View.OnClickListener onHeaderClicked(TitleAdapter adapter){
        return v -> {
            boolean expand = true;
            if(mAdapter.getAdapters().contains(adapter.getBodyAdapter())){
                mAdapter.removeAdapter(adapter.getBodyAdapter());
                expand = false;
            }else {
                int index = mAdapter.getAdapters().indexOf(adapter);
                mAdapter.addAdapter(index+1, adapter.getBodyAdapter());
            }

            ObjectAnimator.ofFloat(v.findViewById(R.id.button_header), "rotation", expand ? 180f : 0f).start();
        };
    }

    private View.OnClickListener showChapter(){
        return v -> {

            ChapterAdapter.ChapterViewHolder vh = (ChapterAdapter.ChapterViewHolder) mBinding.recyclerChapters
                    .findContainingViewHolder(v);

            int index = vh.getBindingAdapterPosition();

            Chapter chap = ((ChapterAdapter) vh.getBindingAdapter()).getCurrentList().get(index);

            Intent intent = new Intent(requireContext(), ReadActivity.class);

            intent.putExtra(ReadActivity.TAG, chap.getId());

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
                String parentDir = mManga.getTitle() + "/" + chap.getVersion();
                String dir = FileUtilsKt.createDownloadFolder(requireActivity(), parentDir, chap.getNumber());

                return new Download(chap.getId(), mManga.getTitle(),
                        chap.getNumber(), dir, chap.getLink(), chap.getLength());
            }).collect(Collectors.toList());

            mViewModel.queueDownloads(downloads);
        }
        return true;
    }

    @Override
    public void onRefresh() {
        isDoneFetching = false;
        updateManga(mManga.getLink());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
    }
}
