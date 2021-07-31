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
import com.exzell.mangaplayground.fragment.base.SwipeRefreshFragment;
import com.exzell.mangaplayground.utils.ViewExtKt;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public abstract class SelectionFragment extends SwipeRefreshFragment {
    public static final String SELECTION_ID = "selection view";
    private SelectionTracker<Long> mTracker;
    private ActionMode mActionMode;
    private BottomCab mCab;
    private ActionMode.Callback mActionCallback;

    private int mRes;
    private int mCabRes;
    private SelectionTracker.SelectionObserver<Long> mObserver = new SelectionTracker.SelectionObserver<Long>() {

        @Override
        public void onSelectionChanged() {

            int size = mTracker.getSelection().size();
            createActionMode();

            if (size > 0) {
                if (mActionMode == null)
                    mActionMode = requireActivity().startActionMode(mActionCallback, ActionMode.TYPE_PRIMARY);

                mActionMode.setTitle(String.valueOf(size));

            } else if (mActionMode != null) mActionMode.finish();
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
    public void setContextMenuResource(@MenuRes int res, @MenuRes int bottomRes) {
        mRes = res;
        mCabRes = bottomRes;
    }

    private void createActionMode() {
        if (mActionCallback != null) return;

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav_view);

        mActionCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                if (mRes != 0) mode.getMenuInflater().inflate(mRes, menu);

                if (mCabRes != 0) {
                    //hide the bottom nav view
                    ViewExtKt.toggleVisibility(bottomNav, false);


                    mCab.show(mCabRes, mode.getMenuInflater(), menuItem -> {
                        boolean handle = SelectionFragment.this.onActionItemClicked(menuItem);
                        mode.finish();
                        return handle;
                    });
                }

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
                ViewExtKt.toggleVisibility(bottomNav, true);
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
        if (mCab != null) mCab.clearMenu();
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

            return holder instanceof DetailsViewHolder ? ((DetailsViewHolder) holder).getDetails() : null;
        }
    }
}
