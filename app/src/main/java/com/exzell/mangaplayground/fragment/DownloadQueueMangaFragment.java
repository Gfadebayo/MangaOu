package com.exzell.mangaplayground.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.MangaPrefs;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.DownloadMangaAdapter;
import com.exzell.mangaplayground.databinding.FragmentDownloadBinding;
import com.exzell.mangaplayground.di.AppComponent;
import com.exzell.mangaplayground.download.DownloadChangeListener;
import com.exzell.mangaplayground.download.DownloadManager;
import com.exzell.mangaplayground.download.model.DownloadManga;
import com.exzell.mangaplayground.models.Download;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class DownloadQueueMangaFragment extends Fragment implements DownloadChangeListener {

    /**
     * The level of the play drawable in the LevelList Drawable
     */
    private final int PLAY_LEVEL = 50;
    /**
     * The level of the play drawable in the LevelList Drawable
     */
    private final int STOP_LEVEL = 150;
    @Inject
    public DownloadManager mManager;
    @Inject
    public MangaPrefs mPrefs;
    private FragmentDownloadBinding mBinding;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);

        AppComponent component = ((MangaApplication) requireActivity().getApplication()).mAppComponent;

        component.injectDownloadManager(this);
        component.injectPrefs(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mManager.addListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentDownloadBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.view.progressLoad.setVisibility(View.GONE);

        showNoDownloadText();

        List<DownloadManga> mangas = mManager.getDownloads().stream().map(d -> mManager.getManga(d.getMangaId()))
                .distinct().collect(Collectors.toList());

        DownloadMangaAdapter adapter = new DownloadMangaAdapter(mangas, requireContext());
        mBinding.view.recyclerLoad.setAdapter(adapter);

        setFab();
        changeFabLevel(mPrefs.getDownloadValue());
    }

    private void setFab() {
        ExtendedFloatingActionButton fab = requireActivity().findViewById(R.id.fab);

        fab.setIconResource(R.drawable.play_stop_level);
        fab.setOnClickListener(v -> {

            int level = fab.getIcon().getLevel();
            boolean resume = level == PLAY_LEVEL;
            mPrefs.setDownloadValue(resume);
            changeFabLevel(resume);
        });
    }

    private void changeFabLevel(boolean download) {
        ExtendedFloatingActionButton fab = requireActivity().findViewById(R.id.fab);

        if (download) {
            fab.setText(R.string.pause);
            fab.getIcon().setLevel(STOP_LEVEL);
        } else {
            fab.setText(R.string.resume);
            fab.getIcon().setLevel(PLAY_LEVEL);
        }
    }

    private void showNoDownloadText() {
        boolean show = mManager.getDownloads().isEmpty();
        mBinding.textNoDownload.setVisibility(show ? View.VISIBLE : View.GONE);
        mBinding.view.getRoot().setVisibility(!show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        mManager.removeListener(this);
//        mManager = null;
//        mPrefs = null;


        mBinding.view.recyclerLoad.setAdapter(null);
        mBinding = null;
    }

    @Override
    public void onDownloadChange(@NotNull List<Download> downs, @NotNull String flag) {
        if (!isAdded() || mBinding == null) return;

        requireActivity().runOnUiThread(() -> {
            DownloadMangaAdapter adapter = (DownloadMangaAdapter) mBinding.view.recyclerLoad.getAdapter();
            List<DownloadManga> items = adapter.getItems();

            downs.forEach(d -> {
                Optional<DownloadManga> mangaFound = items.stream().filter(pair -> pair.getId() == d.getMangaId()).findAny();

                if (flag.equals(FLAG_NEW)) {

                    if (!mangaFound.isPresent()) adapter.addItem(mManager.getManga(d.getMangaId()));

                } else if (flag.equals(FLAG_PROGRESS)) {

                    if (mangaFound.isPresent()) {
                        if (mangaFound.get().getId() == d.getMangaId()) {
                            adapter.updateItem(mangaFound.get());
                        } else {
                            DownloadManga pair = mangaFound.get();
                            adapter.replace(mangaFound.get(), pair);
                        }
                    } else {
                        DownloadManga manga = mManager.getManga(d.getMangaId());
                        adapter.addItem(manga);
                    }
                } else {
                    if (mangaFound.isPresent()) {
                        if (d.getState().equals(Download.State.CANCELLED) || d.getState().equals(Download.State.DOWNLOADED)) {
                            boolean empty = mManager.getDownloads(d.getMangaId()).isEmpty();
                            if (empty) adapter.removeItem(mangaFound.get());

                        } else {
                            adapter.updateItem(mangaFound.get());
                        }
                    }
                }
            });
        });
    }
}
