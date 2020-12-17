package com.exzell.mangaplayground.download;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.MainActivity;
import com.exzell.mangaplayground.MangaApplication;
import com.exzell.mangaplayground.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import javax.inject.Inject;

public class DownloadQueueFragment extends Fragment implements DownloadChangeListener {

    private RecyclerView mRecyclerView;
    private DownloadAdapter mAdapter;
    @Inject public DownloadManager mManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        ((MangaApplication) requireActivity().getApplication()).mAppComponent
                .injectDownloadManager(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManager.addListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.generic_loading_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mRecyclerView = view.findViewById(R.id.recycler_load);
        mAdapter = new DownloadAdapter(new ArrayList<>(mManager.getDownloads()), requireActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setListener(menuListener());
    }

    @Override
    public void onDownloadChange(@NotNull Download down, @NotNull String flag) {

        requireActivity().runOnUiThread(() -> {
            if (flag.equals(DownloadChangeListener.FLAG_NEW)) {
                mAdapter.addDownload(down);
            } else if (flag.equals(DownloadChangeListener.FLAG_STATE)) {

                Download.State st = down.getState();
                if (st.equals(Download.State.DOWNLOADED) || st.equals(Download.State.CANCELLED))
                    mAdapter.removeDownload(down);

            } else if (flag.equals(DownloadChangeListener.FLAG_PROGRESS))
                mAdapter.updateDownload(down);
        });
    }

    private DownloadAdapter.OnPopupItemClicked menuListener() {
        return (d, item) -> {

            if (item.getItemId() == R.id.action_cancel) d.setState(Download.State.CANCELLED);

            else if (item.getTitle().equals("Pause")) {
                d.setState(Download.State.PAUSED);
                item.setTitle("Resume");
            } else {
                d.setState(Download.State.DOWNLOADING);
                item.setTitle("Pause");
            }

            mAdapter.notifyItemChanged(mAdapter.getItems().indexOf(d));
            mManager.updateDownload(d.getId(), DownloadChangeListener.FLAG_STATE);
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mManager = null;

        mAdapter = null;
        mRecyclerView.setAdapter(null);
    }
}
