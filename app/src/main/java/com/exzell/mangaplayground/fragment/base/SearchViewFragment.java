package com.exzell.mangaplayground.fragment.base;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.selection.SelectionFragment;
import com.exzell.mangaplayground.utils.ViewExtKt;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public abstract class SearchViewFragment extends SelectionFragment {

    private Function1<String, Unit> onTextChange;

    private Function0<Unit> onSearchClose;

    private SearchView mSearchView;
    private ActionMode mSearchMode;

    private BottomNavigationView mNavView;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mNavView = requireActivity().findViewById(R.id.bottom_nav_view);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSearchMode != null) mSearchMode.finish();
    }

    protected void setSearchListeners(@NonNull Function1<String, Unit> textChangeListener, Function0<Unit> closeListener) {
        onTextChange = textChangeListener;
        onSearchClose = closeListener;
    }


    @Override
    @CallSuper
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            if (mSearchView == null) {
                mSearchView = new SearchView(requireContext());
                mSearchView.setIconified(false);
                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText == null || onTextChange == null) return false;

                        onTextChange.invoke(newText);
                        return true;
                    }
                });

                mSearchView.setOnCloseListener(() -> {
                    mSearchMode.finish();
                    return false;
                });
            } else {
                mSearchView.setIconified(false);
                mSearchView.requestFocus();
            }

            mSearchMode = requireActivity().startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    if (mode == null || menu == null) return false;

                    menu.add("Search").setActionView(mSearchView);

                    ViewExtKt.toggleVisibility(mNavView, false);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    onSearchClose.invoke();
                    mSearchMode = null;
                    ViewExtKt.toggleVisibility(mNavView, true);
                }
            });
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSearchView = null;
        onTextChange = null;
    }

    @Override
    protected boolean onActionItemClicked(MenuItem item) {
        return false;
    }
}
