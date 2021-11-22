package com.exzell.mangaplayground.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.databinding.ListHistoryBinding;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.utils.DateUtilsKt;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryAdapter extends ListAdapter<DBManga, HistoryAdapter.ViewHolder> {

    private final Context mContext;

    private View.OnClickListener mClickListener;
    private static final DiffUtil.ItemCallback<DBManga> CALLBACK = new DiffUtil.ItemCallback<DBManga>() {
        @Override
        public boolean areItemsTheSame(@NonNull DBManga oldItem, @NonNull DBManga newItem) {
            return oldItem.getLastChapter().getId() == newItem.getLastChapter().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DBManga oldItem, @NonNull DBManga newItem) {
            return oldItem.getLastChapter().equals(newItem.getLastChapter()) &&
                    oldItem.getLastReadTime() == newItem.getLastReadTime();
        }
    };
    private View.OnClickListener onButtonClick;

    public HistoryAdapter(Context context, List<DBManga> mangas) {
        super(CALLBACK);
        mContext = context;
        submitList(mangas);
    }

    public void setMangas(List<DBManga> mangas) {
        submitList(mangas);
    }

    public void setOnClickListener(@NotNull View.OnClickListener listener) {
        mClickListener = listener;
    }

    public void setOnButtonsClickedListener(View.OnClickListener onClick) {
        this.onButtonClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DBManga man = getCurrentList().get(position);

        addSpans(holder.mBinding.textTitleHistory, man.getTitle(), String.valueOf(man.getLastChapter().getNumber()), man.getLastReadTime());

        Request request = Glide.with(mContext)
                .load(man.getThumbnailLink())
                .into(holder.mBinding.imageManga)
                .getRequest();
        if (!request.isRunning()) request.begin();
    }

    private void addSpans(MaterialTextView textView, String title, String chapterNumber, long timestamp) {
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(textView.getContext().getColor(R.color.title_only));
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(14, true);

        String chapterWithTime = chapterNumber + " - " + DateUtilsKt.getTimeOnly(timestamp);

        SpannableString spanString = SpannableString.valueOf(title + "\n" + chapterWithTime);
        spanString.setSpan(colorSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanString.setSpan(sizeSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spanString);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    public List<DBManga> getMangas() {
        return getCurrentList();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ListHistoryBinding mBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mBinding = ListHistoryBinding.bind(itemView);

            itemView.setOnClickListener(mClickListener);

            mBinding.imageResume.setOnClickListener(onButtonClick);

            mBinding.imageDelete.setOnClickListener(onButtonClick);

            mBinding.imageMore.setOnClickListener(onButtonClick);
        }
    }
}
