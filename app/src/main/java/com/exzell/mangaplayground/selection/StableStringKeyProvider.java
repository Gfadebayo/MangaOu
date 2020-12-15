package com.exzell.mangaplayground.selection;

import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.adapters.TitleAdapter;

import java.util.function.Function;
import java.util.function.Predicate;

public class StableStringKeyProvider extends ItemKeyProvider<Long> {
    public static final String TAG = "Key Provider";
    private final RecyclerView mRecyclerView;
    private final RecyclerView.Adapter mAdapter;
    private LongSparseArray<Integer> mKeyToPosition = new LongSparseArray<>();


    public StableStringKeyProvider(RecyclerView rv) {
        super(SCOPE_MAPPED);

        mRecyclerView = rv;
        mAdapter = rv.getAdapter();

        if(mAdapter instanceof ConcatAdapter){
            ((ConcatAdapter) mAdapter).getAdapters().forEach(c -> observe(c));
            mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(@NonNull View view) {
                    RecyclerView.ViewHolder cVH = mRecyclerView.findContainingViewHolder(view);
                    long itemId = cVH.getItemId();
                    int pos = cVH.getAbsoluteAdapterPosition();
                    mKeyToPosition.put(itemId, pos);
                }

                @Override
                public void onChildViewDetachedFromWindow(@NonNull View view) {
                    RecyclerView.ViewHolder cVH = mRecyclerView.findContainingViewHolder(view);
                    long itemId = cVH.getItemId();
                    int pos = cVH.getAbsoluteAdapterPosition();
                    mKeyToPosition.remove(itemId);
                }
            });

        }else observe(mAdapter);
    }

    private void observe(RecyclerView.Adapter ad){
        ad.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                for(int i = positionStart; i < positionStart+itemCount; i++) {
                    long id = mAdapter.getItemId(i);
                    mKeyToPosition.put(id, i);
                }
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                Log.w(TAG, "Range Change called");
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                for(int i = positionStart; i < positionStart+itemCount; i++){
                    long id = mAdapter.getItemId(i);
                    mKeyToPosition.delete(id);
                }
            }
        });
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        DetailsViewHolder vhh = (DetailsViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);

        long id = vhh.getItemId();
        return id;
    }

    @Override
    public int getPosition(@NonNull Long key) {
//        RecyclerView.HeaderViewHolder vh = mRecyclerView.findViewHolderForItemId(key);
//        int relPos = mAdapter.findRelativeAdapterPositionIn(mAdapter, vh, vh.getAbsoluteAdapterPosition());
//        int pos = mAdapter instanceof ConcatAdapter ? vh.getBindingAdapterPosition() : vh.getAbsoluteAdapterPosition();
//
//        int position = ((DetailsViewHolder) vh).getDetails().getPosition();
//        Log.i(TAG, String.valueOf(position));
//        return position;

        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForItemId(key);
        int pos = RecyclerView.NO_POSITION;
        pos = mRecyclerView.getAdapter() instanceof ConcatAdapter ? vh.getBindingAdapterPosition() : vh.getAbsoluteAdapterPosition();
//        else if((vh = getBodyViewHolderId(key)) != null) return vh.getAbsoluteAdapterPosition();
        return vh.getAbsoluteAdapterPosition();
    }

//    private TitleAdapter.BodyViewHolder getBodyViewHolderId(long key){
//
//        return ((ConcatAdapter) mRecyclerView.getAdapter()).getAdapters().stream().map(new Function<RecyclerView.Adapter<? extends RecyclerView.ViewHolder>, TitleAdapter.BodyViewHolder>() {
//            @Override
//            public TitleAdapter.BodyViewHolder apply(RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter) {
//                long itemId = adapter.getItemId(1);
//                return (TitleAdapter.BodyViewHolder) mRecyclerView.findViewHolderForItemId(itemId);
//            }
//        }).filter(new Predicate<TitleAdapter.BodyViewHolder>() {
//            @Override
//            public boolean test(TitleAdapter.BodyViewHolder holder) {
//
//                RecyclerView.ViewHolder bodyHolder = holder.mRecyclerView.findViewHolderForItemId(key);
//
//                return bodyHolder != null;
//            }
//        }).findAny().get();
//    }
}
