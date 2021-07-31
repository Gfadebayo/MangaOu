package com.exzell.mangaplayground.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.exzell.mangaplayground.fragment.BookmarkViewPagerFragment;

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
        return BookmarkViewPagerFragment.getInstance(position);
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
