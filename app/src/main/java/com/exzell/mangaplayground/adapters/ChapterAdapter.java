package com.exzell.mangaplayground.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.selection.DetailsViewHolder;
import com.exzell.mangaplayground.utils.BindingUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChapterAdapter extends ListAdapter<Chapter, ChapterAdapter.ViewHolder> {
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
    private final String TAG = "ChapterAdapter";
    private final Context mContext;
    private View.OnClickListener listen;
    private SelectionTracker mTracker;

    public ChapterAdapter(Context context, List<Chapter> chapters){
        super(DIFF_CALLBACK);
        submitList(chapters);

        mContext = context;
        setHasStableIds(true);
    }

    public void addTracker(SelectionTracker track){
        mTracker = track;
    }

    public void setListener(View.OnClickListener l){
        listen = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_chapter, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chap = getCurrentList().get(position);
        holder.itemView.setOnClickListener(listen);

        holder.mChapterTitle.setText(chap.getTitle());
        holder.mChapterNumber.setText(chap.getNumber());
        holder.mChapterLength.setText(String.valueOf(chap.getLength()));

//        BindingUtils.addThumbnail(holder.mChapterThumb, chap.getLink());
        holder.itemView.setSelected(mTracker.isSelected(getItemId(position)));

        String date = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(new Date(chap.getReleaseDate()));
        holder.mChapterRelease.setText(date);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().isEmpty() ? 0 : getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        long sum = getCurrentList().get(position).getId();

        return position == 0 ? sum : Math.abs(sum*position);
    }

    public class ViewHolder extends DetailsViewHolder {

        public MaterialTextView mChapterTitle;

        private ItemDetailsLookup.ItemDetails<Long> mDetails;
        private MaterialTextView mChapterLength;
        private MaterialTextView mChapterRelease;
        private MaterialTextView mChapterNumber;
        private ShapeableImageView mChapterThumb;

        public ViewHolder(View itemView) {
            super(itemView);


            mChapterTitle = itemView.findViewById(R.id.text_chap_title);
            mChapterLength = itemView.findViewById(R.id.text_chap_length);
            mChapterRelease = itemView.findViewById(R.id.text_chap_release);
            mChapterNumber = itemView.findViewById(R.id.text_chap_number);
            mChapterThumb = itemView.findViewById(R.id.image_chap_thumb);

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
        public ItemDetailsLookup.ItemDetails getDetails() {
            return mDetails;
        }
    }
}
