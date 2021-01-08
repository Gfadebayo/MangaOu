package com.exzell.mangaplayground.selection;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.CallSuper;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.customview.BottomCab;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.fragment.base.DisposableFragment;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class SelectionFragment extends DisposableFragment {
    public static final String SELECTION_ID = "selection view";
    private SelectionTracker<Long> mTracker;
    private ActionMode mActionMode;
    private BottomCab mCab;
    private ActionMode.Callback mActionCallback;

    private int mMenuRes;
    private MaterialToolbar mToolbar;

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mCab = requireActivity().findViewById(R.id.bottom_cab);
        mToolbar = requireActivity().findViewById(R.id.toolbar);
    }

    protected void createTracker(RecyclerView recyclerView){

        StableStringKeyProvider pro = new StableStringKeyProvider(recyclerView);
        DetailsLookup dl = new DetailsLookup(recyclerView);
        StorageStrategy<Long> ss = StorageStrategy.createLongStorage();


        mTracker = new SelectionTracker.Builder(
                SELECTION_ID, recyclerView, pro, dl, ss
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build();


        mTracker.addObserver(mObserver);
    }

    public SelectionTracker<Long> getTracker(){return mTracker;}

    protected abstract boolean onActionItemClicked(MenuItem item);

    public void setMenuResource(@MenuRes int res){
        mMenuRes = res;
    }

    private SelectionTracker.SelectionObserver mObserver = new SelectionTracker.SelectionObserver() {
        @Override
        public void onSelectionChanged() {

            int size = mTracker.getSelection().size();
            createActionMode();

            if (size == 1) {
                if(mActionMode == null) mActionMode = requireActivity()
                        .startActionMode(mActionCallback, ActionMode.TYPE_PRIMARY);

            } else if(size > 1) {
                mActionMode.setTitle(String.valueOf(size));
            }else {
                if(mActionMode != null) mActionMode.finish();
            }
        }
    };

    private void createActionMode(){
        if(mActionCallback != null) return;

        mActionCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                mCab.show(mMenuRes, mode.getMenuInflater(), SelectionFragment.this::onActionItemClicked);
                mode.setTitle(String.valueOf(mTracker.getSelection().size()));

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return SelectionFragment.this.onActionItemClicked(item);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                mTracker.clearSelection();
                mCab.hide();
                mActionCallback = null;
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCab.clearMenu();
        mTracker = null;
        if(mActionMode != null) mActionMode.finish();
        mCab = null;
        mToolbar = null;
    }

    public class DetailsLookup extends ItemDetailsLookup{

        private RecyclerView mRv;

        public DetailsLookup(RecyclerView rv){mRv = rv;}
        @Nullable
        @Override
        public ItemDetails getItemDetails(@NonNull MotionEvent e) {
            View pointer = mRv.findChildViewUnder(e.getX(), e.getY());
            RecyclerView.ViewHolder holder = mRv.findContainingViewHolder(pointer);

            /*if(holder instanceof TitleAdapter.BodyViewHolder){
                View bodyView = ((TitleAdapter.BodyViewHolder) holder).mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                RecyclerView.ViewHolder bodyHolder = ((TitleAdapter.BodyViewHolder) holder).mRecyclerView.findContainingViewHolder(bodyView);

                return ((DetailsViewHolder) bodyHolder).getDetails();

            }else*/ return holder instanceof DetailsViewHolder ? ((DetailsViewHolder) holder).getDetails() : null;
        }
    }
}
