package com.exzell.mangaplayground.selection;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.customview.BottomCab;
import com.exzell.mangaplayground.fragment.base.DisposableFragment;

import org.jetbrains.annotations.NotNull;

public abstract class SelectionFragment extends DisposableFragment {
    public static final String SELECTION_ID = "selection view";
    private SelectionTracker<Long> mTracker;
    private ActionMode mActionMode;
    private BottomCab mCab;
    private ActionMode.Callback mActionCallback;

    private int mRes;
    private int mMenuRes;
    private SelectionTracker.SelectionObserver<Long> mObserver = new SelectionTracker.SelectionObserver<Long>() {

        @Override
        public void onSelectionChanged() {

            int size = mTracker.getSelection().size();
            createActionMode();

            if (size > 0 && mActionMode == null) {
                mActionMode = requireActivity().startActionMode(mActionCallback, ActionMode.TYPE_PRIMARY);

            } else if (mActionMode != null) mActionMode.finish();

            if (mActionMode != null) mActionMode.setTitle(String.valueOf(size));
        }
    };

    @Override
    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCab = requireActivity().findViewById(R.id.bottom_cab);
    }

    public SelectionTracker<Long> getTracker() {
        return mTracker;
    }

    protected abstract boolean onActionItemClicked(MenuItem item);

    protected void createTracker(RecyclerView recyclerView) {

        StableStringKeyProvider pro = new StableStringKeyProvider(recyclerView);
        DetailsLookup dl = new DetailsLookup(recyclerView);
        StorageStrategy<Long> ss = StorageStrategy.createLongStorage();


        mTracker = new SelectionTracker.Builder<>(
                SELECTION_ID, recyclerView, pro, dl, ss
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build();


        mTracker.addObserver(mObserver);
    }

    /**
     * pass the menu resource to use
     *
     * @param res       the resource to inflate for the cab on the toolbar
     * @param bottomRes the resource to inflate on the bottom cab
     */
    public void setMenuResource(@MenuRes int res, @MenuRes int bottomRes) {
        mRes = res;
        mMenuRes = bottomRes;
    }

    private void createActionMode() {
        if (mActionCallback != null) return;

        mActionCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                if (mRes != 0) mode.getMenuInflater().inflate(mRes, menu);
                mCab.show(mMenuRes, mode.getMenuInflater(), menuItem -> {
                    boolean handle = SelectionFragment.this.onActionItemClicked(menuItem);
                    mode.finish();
                    return handle;
                });

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean handle = SelectionFragment.this.onActionItemClicked(item);
                mode.finish();
                return handle;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                mTracker.clearSelection();
                mCab.hide();
//                mActionCallback = null;
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mActionMode != null) mActionMode.finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCab.clearMenu();
        mTracker = null;
//        mCab = null;
    }

    public class DetailsLookup extends ItemDetailsLookup<Long> {

        private RecyclerView mRv;

        public DetailsLookup(RecyclerView rv) {
            mRv = rv;
        }

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
            View pointer = mRv.findChildViewUnder(e.getX(), e.getY());
            RecyclerView.ViewHolder holder = pointer != null ? mRv.findContainingViewHolder(pointer) : null;

            /*if(holder instanceof TitleAdapter.BodyViewHolder){
                View bodyView = ((TitleAdapter.BodyViewHolder) holder).mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                RecyclerView.ViewHolder bodyHolder = ((TitleAdapter.BodyViewHolder) holder).mRecyclerView.findContainingViewHolder(bodyView);

                return ((DetailsViewHolder) bodyHolder).getDetails();

            }else*/
            return holder instanceof DetailsViewHolder ? ((DetailsViewHolder) holder).getDetails() : null;
        }
    }
}
