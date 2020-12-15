package com.exzell.mangaplayground.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.exzell.mangaplayground.fragment.BookmarkFragment;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.fragment.ViewPagerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final Context mContext;
    private String[] mTitles;

    public ViewPagerAdapter(Fragment parent, String[] titles){
        super(parent);
        mContext = parent.requireContext();
        mTitles = titles;
    }

    public String getTitle(int position){
        return mTitles[position];
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        //Else doesnt matter
        int type = position == 0 ? BookmarkFragment.BOOKMARK_BOOKMARK : -1;

        return ViewPagerFragment.getInstance(type);
    }

    @Override
    public int getItemCount() {
        return mTitles.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
