package com.exzell.mangaplayground.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.databinding.ListMangaBinding;
import com.exzell.mangaplayground.databinding.ListMangaHomeBinding;
import com.exzell.mangaplayground.fragment.MangaFragment;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.selection.DetailsViewHolder;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MangaListAdapter extends ListAdapter<Manga, MangaListAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Manga> DIFF_CALLBACK = new DiffUtil.ItemCallback<Manga>() {
        @Override
        public boolean areItemsTheSame(@NonNull Manga oldItem, @NonNull Manga newItem) { return oldItem.getId() == newItem.getId(); }

        @Override
        public boolean areContentsTheSame(@NonNull Manga oldItem, @NonNull Manga newItem) { return oldItem.equals(newItem); }
    };
    private final String TAG = "MangaListAdapter";
    private final Context mContext;
    private final LayoutInflater mInflater;
    private boolean mSummaryShow;
    private int mItemType;
    private boolean mShowMoreInfo;
    private SelectionTracker mTracker;

    public MangaListAdapter(Context context, List<? extends Manga> mangas, @LayoutRes int itemType){
        super(DIFF_CALLBACK);
        mContext = context;
        submitList(new ArrayList<>(mangas));

        mInflater = LayoutInflater.from(context);
        mItemType = itemType;
        setHasStableIds(true);
    }

    public void setTracker(SelectionTracker track){
        mTracker = track;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(viewType, parent, false);

        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Manga manga = getCurrentList().isEmpty() ? null : getCurrentList().get(position);

        if(holder.mBinding != null) {
            holder.mBinding.setManga(manga);

            int vis1 = mSummaryShow ? View.VISIBLE : View.GONE;

        }else if(holder.mHomeBinding != null) {

            holder.mHomeBinding.setManga(manga);
            if(mShowMoreInfo) setColor(holder.mHomeBinding.buttonMore);
        }

        if(mTracker != null) holder.itemView.setSelected(mTracker.isSelected(getItemId(position)));
    }

    private void setColor(ImageView v){
        IconicsDrawable draw = new IconicsDrawable(mContext, "faw_info");
        int color = mContext.getResources().getColor(R.color.accent, null);
        draw.color(color).sizeDp(24);
        v.setImageDrawable(draw);
    }

    @Override
    public int getItemViewType(int position) {
        return mItemType;
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        return getCurrentList().get(position).getTitle().chars().sum();
    }

    public void clearMangas(){
        submitList(Collections.EMPTY_LIST);
    }

    public void addManga(Manga newManga){
        addMangas(Collections.singletonList(newManga));
    }

    public void addMangas(List<? extends Manga> mangas){

        ArrayList<Manga> newMangas = new ArrayList<>(getCurrentList());
        newMangas.addAll(mangas);


        submitList(newMangas.stream().distinct().collect(Collectors.toList()));
    }

    public void showSummary(boolean show){
        mSummaryShow = show;
        notifyItemRangeChanged(0, getCurrentList().size());
    }

    public void showMoreInfo(boolean show){
        mShowMoreInfo = show;
        notifyItemRangeChanged(0, getCurrentList().size());
    }

    public class ViewHolder extends DetailsViewHolder {

        private ListMangaBinding mBinding;
        private ListMangaHomeBinding mHomeBinding;
        private ItemDetailsLookup.ItemDetails<Long> mDetails;

        public ViewHolder(@NonNull View itemView, int type) {
            super(itemView);

            if(type == R.layout.list_manga) mBinding =  DataBindingUtil.bind(itemView);
            else mHomeBinding = DataBindingUtil.bind(itemView);


            itemView.setOnClickListener(v -> {
                int pos = getAbsoluteAdapterPosition();
                Bundle bund = new Bundle();


                bund.putString(MangaFragment.MANGA_ID, getCurrentList().get(pos).getLink());
                Navigation.findNavController(v).navigate(R.id.frag_manga, bund);
            });

            mDetails = new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getAbsoluteAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    return MangaListAdapter.this.getItemId(getPosition());
                }
            };
        }


        @Override
        public ItemDetailsLookup.ItemDetails getDetails() {
            return mDetails;
        }
    }
}
