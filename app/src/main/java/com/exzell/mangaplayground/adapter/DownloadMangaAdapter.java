package com.exzell.mangaplayground.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.databinding.ListDownloadBinding;
import com.exzell.mangaplayground.download.model.DownloadManga;
import com.exzell.mangaplayground.fragment.DownloadQueueFragment;
import com.exzell.mangaplayground.models.Download;
import com.exzell.mangaplayground.utils.BindingUtils;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DownloadMangaAdapter extends RecyclerView.Adapter<DownloadMangaAdapter.ViewHolder> {

    private ArrayList<DownloadManga> mDownloads;
    private Context mContext;
    private View.OnClickListener mClickListener;

    public DownloadMangaAdapter(List<DownloadManga> downloads, Context context) {
        this.mDownloads = new ArrayList<>(downloads);
        this.mContext = context;
    }

    public void setClickListener(View.OnClickListener listener) {
        mClickListener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        ViewHolder vh = new ViewHolder(inflater.inflate(R.layout.list_download, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DownloadMangaAdapter.ViewHolder holder, int position) {
        DownloadManga man = mDownloads.get(position);

        holder.mBinding.textDownloadManga.setText(man.getTitle());
        holder.mBinding.textDownloadChapter.setText(man.getState().toString());


        BindingUtils.addThumbnail(holder.mBinding.imageDownloadManga, man.getThumbnailLink(), null);


        int max = holder.mBinding.progressLength.getMax();
        int progress = (max * man.getTotalProgress()) / man.getTotalLength();
        holder.mBinding.progressLength.setProgressCompat(progress, true);
    }

    private void setChapterDetails(MaterialTextView textView, Download d) {

        String text = d.getTitle() + " " + d.getState().toString();
        int start = text.indexOf(d.getState().toString());

        SpannableString spanString = new SpannableString(text);

        spanString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spanString);
    }

    @Override
    public int getItemCount() {
        return mDownloads.size();
    }

    public List<DownloadManga> getItems() {
        return mDownloads;
    }

    public void setItems(List<DownloadManga> newItems) {
        mDownloads.clear();
        mDownloads.addAll(newItems);
    }

    public void addItem(DownloadManga dManga) {
        mDownloads.add(dManga);
        notifyItemInserted(mDownloads.size() - 1);
    }

    public void removeItem(DownloadManga dManga) {
        int index = mDownloads.indexOf(dManga);
        mDownloads.remove(dManga);
        notifyItemRemoved(index);
    }

    public void updateItem(DownloadManga dManga) {
        int index = mDownloads.indexOf(dManga);
        notifyItemChanged(index);
    }

    public void replace(DownloadManga oldPair, DownloadManga newPair) {
        int index = mDownloads.indexOf(oldPair);
        mDownloads.set(index, newPair);
        notifyItemChanged(index);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ListDownloadBinding mBinding;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mBinding = ListDownloadBinding.bind(itemView);

            itemView.setOnClickListener(v -> {
                DownloadManga man = mDownloads.get(getAbsoluteAdapterPosition());
                Bundle bund = new Bundle(1);
                bund.putLong(DownloadQueueFragment.KEY_MANGA_ID, man.getId());

                Navigation.findNavController(v).navigate(R.id.action_nav_downloads_to_nav_chapter_downloads, bund);
            });
        }
    }
}
