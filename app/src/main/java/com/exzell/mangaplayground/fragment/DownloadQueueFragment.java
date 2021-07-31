package com.exzell.mangaplayground.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.exzell.mangaplayground.BuildConfig;
import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.DownloadAdapter;
import com.exzell.mangaplayground.databinding.FragmentDownloadBinding;
import com.exzell.mangaplayground.di.AppComponent;
import com.exzell.mangaplayground.download.DownloadChangeListener;
import com.exzell.mangaplayground.download.DownloadManager;
import com.exzell.mangaplayground.models.Download;
import com.google.android.material.appbar.MaterialToolbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class DownloadQueueFragment extends Fragment implements DownloadChangeListener {

    public static final String KEY_MANGA_ID = BuildConfig.APPLICATION_ID + ".manga_id";
    @Inject
    public DownloadManager mManager;

    private FragmentDownloadBinding mBinding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        AppComponent appComp = ((MangaApplication) requireActivity().getApplication()).mAppComponent;
        appComp.injectDownloadManager(this);
        appComp.injectPrefs(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManager.addListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentDownloadBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mBinding.view.progressLoad.setVisibility(View.GONE);

        showNoDownloadText();

        long mangaId = getArguments().getLong(KEY_MANGA_ID);
        DownloadAdapter adapter = new DownloadAdapter(new ArrayList<>(mManager.getDownloads(mangaId)), requireActivity());
        adapter.setListener(menuListener(adapter));

        mBinding.view.recyclerLoad.setAdapter(adapter);

        ((MaterialToolbar) requireActivity().findViewById(R.id.toolbar)).setTitle(mManager.getManga(mangaId).getTitle());
    }

//    @Override
//    protected boolean onActionItemClicked(MenuItem item) {
//        return false;
//    }

    private void showNoDownloadText() {
        boolean show = mManager.getDownloads().isEmpty();
        mBinding.textNoDownload.setVisibility(show ? View.VISIBLE : View.GONE);
        mBinding.view.getRoot().setVisibility(!show ? View.VISIBLE : View.GONE);
    }

    private DownloadAdapter.OnPopupItemClicked menuListener(DownloadAdapter adapter) {
        return (d, item) -> {

            if (item.getItemId() == R.id.action_cancel) d.setState(Download.State.CANCELLED);

            else if (item.getTitle().equals(requireContext().getString(R.string.pause))) {
                d.setState(Download.State.PAUSED);
                item.setTitle(requireContext().getString(R.string.resume));
            } else {
                d.setState(Download.State.DOWNLOADING);
                item.setTitle(requireContext().getString(R.string.pause));
            }

            adapter.notifyItemChanged(adapter.getItems().indexOf(d));
            mManager.updateDownload(d.getId(), DownloadChangeListener.FLAG_STATE);
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mManager.removeListener(this);
        mManager = null;

        mBinding.view.recyclerLoad.setAdapter(null);
        mBinding = null;
    }

    @Override
    public void onDownloadChange(@NotNull List<Download> downloads, @NotNull String flag) {
        if (!isAdded() || mBinding == null) return;

        requireActivity().runOnUiThread(() -> {

            long mangaId = getArguments().getLong(KEY_MANGA_ID);

            List<Download> downs = downloads.stream().filter(d -> d.getMangaId() == mangaId).collect(Collectors.toList());

            DownloadAdapter adapter = (DownloadAdapter) mBinding.view.recyclerLoad.getAdapter();
            if (adapter == null) return;

            boolean done = false;

            if (flag.equals(FLAG_NEW)) {
                adapter.addDownloads(downs);
                done = true;
            } else if (flag.equals(FLAG_STATE)) {

                Download.State st = downs.get(0).getState();
                if (st.equals(Download.State.DOWNLOADED) || st.equals(Download.State.CANCELLED)) {
                    adapter.removeDownloads(downs);
                    done = true;
                }

            }

            if (!done) adapter.updateDownloads(downs);
        });
    }
}
