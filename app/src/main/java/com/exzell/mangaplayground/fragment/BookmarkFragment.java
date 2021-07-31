package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.ViewPagerAdapter;
import com.exzell.mangaplayground.databinding.FragmentBookmarkBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class BookmarkFragment extends Fragment {

    public static final String KEY_PAGE = "current page";

    /**
     * The page for Bookmarks, anything other than this is for Downloads
     */
    public static int PAGE_BOOKMARK = 0;
    private TabLayoutMediator mTabMediator;
    private FragmentBookmarkBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentBookmarkBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, requireActivity().getResources().getStringArray(R.array.bookmark_titles));

        mBinding.pagerBookmark.setAdapter(adapter);

        TabLayoutMediator.TabConfigurationStrategy stra = (tab, position) -> {
            String s = adapter.getTitle(position);
            tab.setText(s);
        };
        mTabMediator = new TabLayoutMediator(mBinding.tabBookmark, mBinding.pagerBookmark, stra);

        mTabMediator.attach();

        mBinding.pagerBookmark.post(() -> {
            if (getArguments() != null) {
                int val = getArguments().getInt(KEY_PAGE);
                mBinding.pagerBookmark.setCurrentItem(val, false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabMediator.detach();
        mBinding.pagerBookmark.setAdapter(null);
        mTabMediator = null;
        mBinding = null;
    }
}
