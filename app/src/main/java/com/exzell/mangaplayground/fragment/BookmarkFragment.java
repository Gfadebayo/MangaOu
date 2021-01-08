package com.exzell.mangaplayground.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.UpdateService;
import com.exzell.mangaplayground.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class BookmarkFragment extends Fragment {

    public static int BOOKMARK_BOOKMARK = 0;
    private ViewPager2 mPager;
    private TabLayout mTab;
    private TabLayoutMediator mTabMediator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mPager = view.findViewById(R.id.pager_bookmark);
        mTab = view.findViewById(R.id.tab_bookmark);

        ViewPagerAdapter mAdapter = new ViewPagerAdapter(this, requireActivity().getResources().getStringArray(R.array.bookmark_titles));

        mPager.setAdapter(mAdapter);

        TabLayoutMediator.TabConfigurationStrategy stra = (tab, position) -> {
            String s = mAdapter.getTitle(position);
            tab.setText(s);
        };
        mTabMediator = new TabLayoutMediator(mTab, mPager, stra);

        mTabMediator.attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabMediator.detach();
        mPager.setAdapter(null);
        mPager = null;
        mTab = null;
        mTabMediator = null;
    }
}
