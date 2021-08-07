package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapter.SearchDialogAdapter;
import com.exzell.mangaplayground.databinding.DialogSearchBinding;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.BiConsumer;



public class SearchDialogFragment extends BottomSheetDialogFragment {

    private SearchViewModel mViewModel;
    private OnSearchClickedListener mListener;
    private DialogSearchBinding mBinding;

    public static SearchDialogFragment getInstance(){return new SearchDialogFragment();}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory
                (requireActivity().getApplication(), this)).get(SearchViewModel.class);

        mViewModel.search();
        mListener = (SearchFragment) getParentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogSearchBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SearchDialogAdapter adapter = new SearchDialogAdapter(requireContext(), mViewModel.mSearch);
        adapter.setEditTextListener(setTextChangeListener());
        adapter.setSpinnerListener(onSpinnerItemSelected());
        adapter.setRatingListener((ratingBar, rating) -> mViewModel.setRating((int) rating));
        adapter.setButtonListener((buttonView, isChecked) -> {
            View itemView = mBinding.recyclerSearch.findContainingItemView(buttonView);

            RecyclerView.ViewHolder vh = mBinding.recyclerSearch.findContainingViewHolder(itemView);

            if (vh == null) return;
            int position = vh.getAbsoluteAdapterPosition();

            String text = buttonView.getText().toString();
            if (position == 2 && isChecked) mViewModel.setStatus(text.toLowerCase());
            else if (position == 4 && isChecked) mViewModel.setType(text);
            else if (position == 6) mViewModel.setGenres(text, isChecked);
        });
        mBinding.recyclerSearch.setAdapter(adapter);


        mBinding.buttonSearch.setOnClickListener(v -> {
            mListener.onSearchClicked();
            dismiss();
        });

        mBinding.buttonReset.setOnClickListener(v -> {
            mViewModel.resetValues();
//            mListener.onSearchClicked();
//            dismiss();
        });
    }


    private BiConsumer<Integer, String> onSpinnerItemSelected() {
        return (id, s) -> {

            if (id == R.id.spin_auth_contain) mViewModel.setContainValue(true, s.toLowerCase());

            else if (id == R.id.spin_title_contain)
                mViewModel.setContainValue(false, s.toLowerCase());

            else if (id == R.id.spin_genre_incl) mViewModel.setGenreInclusion(s.toLowerCase());

            else if (id == R.id.spin_order) mViewModel.setOrder(s);

            else if (id == R.id.spin_release) mViewModel.setRelease(Integer.parseInt(s));
            else if (id == R.id.spin_chapter_amount) {
                String chapter = s.split("\\D")[0];
                if (Character.isDigit(chapter.charAt(0)))
                    mViewModel.setChapters(Integer.parseInt(chapter));
            }
        };
    }

    private BiConsumer<Integer, String> setTextChangeListener() {
        return (id, text) -> mViewModel.setName(id != R.id.edit_search_title, text);
    }

    public interface OnSearchClickedListener {
        void onSearchClicked();
    }
}
