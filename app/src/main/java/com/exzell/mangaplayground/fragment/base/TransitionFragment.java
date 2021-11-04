package com.exzell.mangaplayground.fragment.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.TransitionRes;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

public class TransitionFragment extends Fragment {

    private boolean isTransitionSet;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isTransitionSet) {
            setAllowEnterTransitionOverlap(false);
            postponeEnterTransition();
        }
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isTransitionSet) view.post(() -> startPostponedEnterTransition());
    }

    protected void setEnterTransitionRes(@TransitionRes int res) {
        isTransitionSet = true;
        setEnterTransition(TransitionInflater.from(requireContext()).inflateTransition(res));
    }

    protected void setExitTransitionRes(@TransitionRes int res) {
        setExitTransition(TransitionInflater.from(requireContext()).inflateTransition(res));
    }
}
