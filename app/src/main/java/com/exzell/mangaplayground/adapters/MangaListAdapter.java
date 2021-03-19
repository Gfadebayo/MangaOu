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
import com.exzell.mangaplayground.utils.BindingUtils;

import java.util.ArrayList;
import java.util.List;

public class MangaListAdapter extends ListAdapter<Manga, MangaListAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Manga> DIFF_CALLBACK = new DiffUtil.ItemCallback<Manga>() {
        @Override
        public boolean areItemsTheSame(@NonNull Manga oldItem, @NonNull Manga newItem) { return oldItem.getLink().hashCode() == newItem.getLink().hashCode(); }

        @Override
        public boolean areContentsTheSame(@NonNull Manga oldItem, @NonNull Manga newItem) { return oldItem.equals(newItem); }
    };

    private final Context mContext;
    private final LayoutInflater mInflater;
    private boolean mSummaryShow;
    private int mItemType;
    private boolean mShowMoreInfo;
    private SelectionTracker<Long> mTracker;

    public MangaListAdapter(Context context, List<? extends Manga> mangas, @LayoutRes int itemType){
        super(DIFF_CALLBACK);
        mContext = context;
        submitList(new ArrayList<>(mangas));

        mInflater = LayoutInflater.from(context);
        mItemType = itemType;
        setHasStableIds(true);

    }

    public void setTracker(SelectionTracker<Long> track){
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
        Manga manga = getCurrentList().get(position);

        if(holder.mBinding != null) {
            holder.mBinding.setManga(manga);

        }else if(holder.mHomeBinding != null) {

            holder.mHomeBinding.textMangaTitle.setText(manga.getTitle());

            BindingUtils.addThumbnail(holder.mHomeBinding.imageManga, manga.getThumbnailLink());
            if (mShowMoreInfo) setColor(holder.mHomeBinding.buttonMore);
        }

        if(mTracker != null) holder.itemView.setSelected(mTracker.isSelected(getItemId(position)));
    }

    private void setColor(ImageView v){
        v.setImageResource(R.drawable.ic_outline_info_24);
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
        return getCurrentList().get(position).hashCode();
    }

    public void addMangas(List<? extends Manga> mangas){

        ArrayList<Manga> newMangas = new ArrayList<>(getCurrentList());
        newMangas.addAll(mangas);

        submitList(newMangas);
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

            if (type == R.layout.list_manga) mBinding = ListMangaBinding.bind(itemView);
            else mHomeBinding = ListMangaHomeBinding.bind(itemView);


            itemView.setOnClickListener(v -> {
                int pos = getAbsoluteAdapterPosition();
                Bundle bund = new Bundle();


                bund.putString(MangaFragment.MANGA_LINK, getCurrentList().get(pos).getLink());
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
        public ItemDetailsLookup.ItemDetails<Long> getDetails() {
            return mDetails;
        }
    }
}
