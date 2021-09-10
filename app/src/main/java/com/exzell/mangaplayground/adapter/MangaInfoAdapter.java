package com.exzell.mangaplayground.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.databinding.LayoutChapterBinding;
import com.exzell.mangaplayground.databinding.LayoutMangaInfoBinding;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.reader.ReadActivity;
import com.exzell.mangaplayground.selection.DetailsViewHolder;
import com.exzell.mangaplayground.utils.ChapterUtilsKt;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MangaInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Manga mManga;
    private ArrayList<Chapter> mVisibleChapters = new ArrayList<>();
    private final int PAYLOAD_DOWNLOAD = 4;

    private final int PAYLOAD_BOOKMARK = 2;
    private ArrayList<Long> mDownloadIds = new ArrayList<>();

    private Chapter.Version mCurrentVersion;
    private Context mContext;
    private View.OnClickListener mListener;
    private View.OnClickListener mBookmarkListener;
    private SelectionTracker<Long> mTracker;
    private DateFormat mDateFormatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

    public MangaInfoAdapter(Context context, Manga manga) {
        mManga = manga;
        mContext = context;
        setHasStableIds(true);

        if (manga != null) setCurrentVisibleChapters();
    }

    public void setDownloads(@NotNull List<Long> ids) {
        List<Long> chapIds = mVisibleChapters.stream().map(Chapter::getId)
                .collect(Collectors.toList());

        ArrayList<Long> oldIds = new ArrayList<>(mDownloadIds);

        mDownloadIds.clear();
        mDownloadIds.addAll(ids);

        oldIds.addAll(ids);
        if (!oldIds.isEmpty()) oldIds.stream().distinct()
                .forEach(id -> notifyItemChanged(chapIds.indexOf(id) + 1, PAYLOAD_DOWNLOAD));
    }

    private void setCurrentVisibleChapters() {
        if (mManga != null && mCurrentVersion == null) {

            if (mManga.getChapters().isEmpty()) return;

            Stream<Chapter> chapterStream = mManga.getChapters().stream();
            if (mCurrentVersion == null)
                mCurrentVersion = chapterStream.map(ch -> ch.getVersion()).findFirst().get();
        }

        int currentSize = mVisibleChapters.size();

        mVisibleChapters.clear();
        mVisibleChapters.addAll(mManga.getChapters().stream()
                .filter(ch -> ch.getVersion().equals(mCurrentVersion))
                .collect(Collectors.toList()));

        notifyItemRangeChanged(1, currentSize);


        int diff = currentSize - mVisibleChapters.size();

        //items have been deleted since the last size is greater
        if (diff > 0) notifyItemRangeRemoved(currentSize + 1, Math.abs(diff));
            //items have been inserted since the last size is smaller
        else if (diff < 0) notifyItemRangeInserted(currentSize + 1, Math.abs(diff));
    }

    public void setCurrentChapterVersionShown(Chapter.Version version) {
        mCurrentVersion = version;
    }

    public void updateMangaInfo(Manga newManga, boolean onlyBookmark) {
        if (onlyBookmark) notifyItemChanged(0, PAYLOAD_BOOKMARK);
        else {
            mManga = newManga;
            notifyItemChanged(0);

            setCurrentVisibleChapters();
        }
    }

    public void addTracker(SelectionTracker<Long> track) {
        mTracker = track;
    }

    public void setListener(View.OnClickListener l) {
        mListener = l;
    }

    public void setBookmarkListener(View.OnClickListener listener) {
        mBookmarkListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(viewType, parent, false);

        return viewType == R.layout.layout_manga_info ? new MangaInfoViewHolder(v) : new ChapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ChapterViewHolder) {
            Chapter chap = mVisibleChapters.get(position - 1);
            ChapterViewHolder chapHolder = (ChapterViewHolder) holder;

            holder.itemView.setSelected(mTracker.isSelected(getItemId(position)));

            String chapterName = mContext.getString(R.string.chapter, ChapterUtilsKt.getChapterNumericValue(chap));
            chapHolder.mBinding.textChapNumber.setText(chapterName);

            String title = chap.getTitle().isEmpty() ? mContext.getString(R.string.no_title) : chap.getTitle();
            chapHolder.mBinding.textChapTitle.setText(title);

            chapHolder.mBinding.textChapReleaseLength.setText(createReleasePositionSpan(chap));

            chapHolder.mBinding.imageChapDownload.setVisibility(mDownloadIds.contains(chap.getId()) ? View.VISIBLE : View.GONE);
        } else {
            ((MangaInfoViewHolder) holder).mBinding.setManga(mManga);
        }
    }

    private String createReleasePositionSpan(Chapter chapter) {
        String date = mDateFormatter.format(new Date(chapter.getReleaseDate()));

        String readPosition = String.valueOf(chapter.getLength());

        if (chapter.getLastReadingPosition() > 0)
            readPosition = chapter.getLastReadingPosition() + "/" + readPosition;

        return date + " \u2022 " + readPosition;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {

        if (payloads.contains(PAYLOAD_BOOKMARK)) {
            ((MangaInfoViewHolder) holder).mBinding.bookmark.setSelected(mManga.isBookmark());

        } else if (payloads.contains(PAYLOAD_DOWNLOAD)) {
            if (position < 1) return;

            Chapter chap = mVisibleChapters.get(position - 1);
            int visibility = mDownloadIds.contains(chap.getId()) ? View.VISIBLE : View.GONE;
            ((ChapterViewHolder) holder).mBinding.imageChapDownload.setVisibility(visibility);

        } else super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemViewType(int pos) {
        return pos == 0 ? R.layout.layout_manga_info : R.layout.layout_chapter;
    }

    @Override
    public int getItemCount() {
        return mManga == null ? 0 : mVisibleChapters.size() + 1;
    }

    @Override
    public long getItemId(int pos) {
        return pos == 0 ? -1 : mVisibleChapters.get(pos - 1).hashCode();
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

            itemView.setOnClickListener(v -> {
                int index = getAbsoluteAdapterPosition() - 1;

                Chapter chap = mVisibleChapters.get(index);

                Intent intent = new Intent(mContext, ReadActivity.class);

                intent.putExtra(ReadActivity.CHAPTER, chap.getId());

                mContext.startActivity(intent);
            });
        }

        @Override
        public ItemDetailsLookup.ItemDetails<Long> getDetails() {
            return mDetails;
        }
    }

    public class MangaInfoViewHolder extends RecyclerView.ViewHolder {
        public LayoutMangaInfoBinding mBinding;

        public MangaInfoViewHolder(View itemView) {
            super(itemView);

            mBinding = LayoutMangaInfoBinding.bind(itemView);

            mBinding.bookmark.setSelected(mManga.isBookmark());
            //create tabs equal to the versions
            addTabs();

            mBinding.bookmark.setOnClickListener(mBookmarkListener);
        }

        private void addTabs() {

            TabLayout.OnTabSelectedListener tabListener = new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    mCurrentVersion = Arrays.stream(Chapter.Version.values())
                            .filter(ver -> String.valueOf(tab.getText()).contains(ver.getDispName()))
                            .findFirst().get();
                    setCurrentVisibleChapters();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            };

            mManga.getChapters().stream().map(ch -> ch.getVersion()).distinct()
                    .forEach(ver -> {

                        TabLayout.Tab versionTab = mBinding.tabChapters.newTab();
                        versionTab.setText(ver.getDispName() + "(" + mVisibleChapters.size() + ")");

                        mBinding.tabChapters.addTab(versionTab, mCurrentVersion.equals(ver));
                        mBinding.tabChapters.addOnTabSelectedListener(tabListener);
                    });
        }
    }
}
