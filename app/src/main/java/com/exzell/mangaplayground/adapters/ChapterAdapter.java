package com.exzell.mangaplayground.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.databinding.LayoutChapterBinding;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.selection.DetailsViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChapterAdapter extends ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder> {
    private static final ItemCallback<Chapter> DIFF_CALLBACK = new ItemCallback<Chapter>() {
        @Override
        public boolean areItemsTheSame(@NonNull Chapter oldItem, @NonNull Chapter newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Chapter oldItem, @NonNull Chapter newItem) {
            return oldItem.equals(newItem);
        }
    };
    private Context mContext;
    private View.OnClickListener listen;
    private SelectionTracker<Long> mTracker;

    public ChapterAdapter(Context context, List<Chapter> chapters) {
        super(DIFF_CALLBACK);
        submitList(chapters);

        mContext = context;
        setHasStableIds(true);
    }

    public void addTracker(SelectionTracker<Long> track) {
        mTracker = track;
    }

    public void setListener(View.OnClickListener l) {
        listen = l;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_chapter, parent, false);
        return new ChapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {

        Chapter chap = getCurrentList().get(position);
        holder.itemView.setOnClickListener(listen);
        holder.itemView.setSelected(mTracker.isSelected(getItemId(position)));

        holder.mBinding.textChapTitle.setText(chap.getTitle());
        holder.mBinding.textChapNumber.setText(chap.getNumber());
        holder.mBinding.textChapLength.setText(String.valueOf(chap.getLength()));

        String date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(new Date(chap.getReleaseDate()));
        holder.mBinding.textChapRelease.setText(date);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        return getCurrentList().get(position).getId();
    }

    public class ChapterViewHolder extends DetailsViewHolder {

        private ItemDetailsLookup.ItemDetails<Long> mDetails;

        private LayoutChapterBinding mBinding;

        public ChapterViewHolder(View itemView) {
            super(itemView);
            mBinding = LayoutChapterBinding.bind(itemView);

            mDetails = new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getAbsoluteAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    return getItemId();
                }
            };
        }

        @Override
        public ItemDetailsLookup.ItemDetails<Long> getDetails() {
            return mDetails;
        }
    }
}
