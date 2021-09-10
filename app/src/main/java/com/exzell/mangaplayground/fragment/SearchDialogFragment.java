package com.exzell.mangaplayground.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.advancedsearch.MangaSearch;
import com.exzell.mangaplayground.advancedsearch.Order;
import com.exzell.mangaplayground.databinding.DialogSearchBinding;
import com.exzell.mangaplayground.utils.BindingUtils;
import com.exzell.mangaplayground.utils.ViewExtKt;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;


public class SearchDialogFragment extends BottomSheetDialogFragment {

    private SearchViewModel mViewModel;
    private OnSearchClickedListener mListener;
    private DialogSearchBinding mBinding;

    public static SearchDialogFragment getInstance() {
        return new SearchDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory
                (requireActivity().getApplication(), this)).get(SearchViewModel.class);

        mViewModel.createSearchFromHandle();
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

        setupHeaders();
        setupEditText();
        setupSpinner();
        setupSlider();
        setupRadioGroup();

        BindingUtils.populateGrid(mBinding.gridGenre, mViewModel.mSearch, (text, isChecked) -> mViewModel.setGenres(text, isChecked));

        mBinding.buttonSearch.setOnClickListener(v -> {
            mListener.onSearchClicked();
            dismiss();
        });

        mBinding.buttonReset.setOnClickListener(v -> {
            resetViews();
            mViewModel.resetValues();
//            mListener.onSearchClicked();
//            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mViewModel.saveSearchParams();
    }

    private void setupHeaders() {
        View.OnClickListener headerListener = v -> {
            View nextView = mBinding.layoutParent.getChildAt(mBinding.layoutParent.indexOfChild(v) + 1);


            int visibility = nextView.getVisibility();
            nextView.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);

            if (visibility != View.VISIBLE) ViewExtKt.rotateDrawable((TextView) v, 0, 80);
            else ViewExtKt.rotateDrawable((TextView) v, 80, 0);
        };

        mBinding.headerGenre.setOnClickListener(headerListener);
        mBinding.headerStatus.setOnClickListener(headerListener);
        mBinding.headerType.setOnClickListener(headerListener);

    }

    private void setupSlider() {
        String[] array = requireContext().getResources().getStringArray(R.array.chapter_values);

        Slider.OnSliderTouchListener listener = new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull @NotNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull @NotNull Slider slider) {
                int value = (int) slider.getValue();
                if (slider.getId() == R.id.slider_release) {
                    mViewModel.setRelease(value < MangaSearch.RELEASE_DATE_START ? 0 : value);

                } else if (slider.getId() == R.id.slider_rating) {
                    mViewModel.setRating(value);

                } else if (slider.getId() == R.id.slider_amount) {
                    mViewModel.setChapters(value);
                }
            }
        };


        //Throws an error cause release default value (0) is not in the slider's range
        //so we instead use 1 extra value below the start value to clear it(it'll represent 0)
        int release = mViewModel.mSearch.getRelease();
        mBinding.sliderRelease.setValueTo(MangaSearch.RELEASE_DATE_END);
        mBinding.sliderRelease.setValueFrom(MangaSearch.RELEASE_DATE_START - 1);
        mBinding.sliderRelease.setValue(release <= 0 ? MangaSearch.RELEASE_DATE_START - 1 : release);
        mBinding.sliderRelease.setLabelFormatter(i -> i >= MangaSearch.RELEASE_DATE_START ? String.valueOf((int) i) : requireContext().getString(R.string.any));
        mBinding.sliderRelease.addOnSliderTouchListener(listener);


        mBinding.sliderRating.setValue(mViewModel.mSearch.getRating());
        mBinding.sliderRating.setLabelFormatter(i -> i > 0 ? String.valueOf((int) i) : requireContext().getString(R.string.any));
        mBinding.sliderRating.addOnSliderTouchListener(listener);

        mBinding.sliderAmount.setValueTo(array.length - 1);
        mBinding.sliderAmount.setValue(mViewModel.mSearch.getChapterAmount());
        mBinding.sliderAmount.setLabelFormatter(i -> array[(int) i]);
        mBinding.sliderAmount.addOnSliderTouchListener(listener);
    }

    private void setupSpinner() {
        BiConsumer<Integer, String> listener = (id, value) -> {

            if (id == R.id.spin_auth_contain) mViewModel.setAuthorContain(value.toLowerCase());

            else if (id == R.id.spin_title_contain) mViewModel.setTitleContain(value.toLowerCase());

            else if (id == R.id.spin_genre_incl) mViewModel.setGenreInclusion(value.toLowerCase());

            else if (id == R.id.spin_order) mViewModel.setOrder(value);
        };

        BindingUtils.setSpinnerItems(mBinding.spinAuthContain, R.array.contain_values, mViewModel.mSearch.getAuthorContain(), listener);
        BindingUtils.setSpinnerItems(mBinding.spinTitleContain, R.array.contain_values, mViewModel.mSearch.getTitleContain(), listener);
        BindingUtils.setSpinnerItems(mBinding.spinGenreIncl, R.array.incl_values, mViewModel.mSearch.getGenreInclusion(), listener);

        Order order = mViewModel.mSearch.getOrder();
        BindingUtils.setSpinnerItems(mBinding.spinOrder, R.array.order_values, order != null ? order.dispName : "", listener);
    }

    private void setupRadioGroup() {

        RadioGroup.OnCheckedChangeListener listener = (group, checkedId) -> {
            RadioButton button = ((RadioButton) group.findViewById(checkedId));
            if (button == null || !button.isChecked()) return;

            if (group.getId() == R.id.radio_group_status)
                mViewModel.setStatus(button.getText().toString());
            else if (group.getId() == R.id.radio_group_type)
                mViewModel.setType(button.getText().toString());
        };

        mBinding.radioGroupStatus.setOnCheckedChangeListener(listener);
        mBinding.radioGroupType.setOnCheckedChangeListener(listener);
    }

    private void setupEditText() {
        mBinding.editTextAuthor.setText(mViewModel.mSearch.getAuthor());
        mBinding.editTextTitle.setText(mViewModel.mSearch.getTitle());

        BiConsumer<Integer, String> listener = (id, text) -> {
            if (id == R.id.edit_text_title) mViewModel.setTitle(text);
            else mViewModel.setAuthor(text);
        };

        BindingUtils.textChangeListeners(mBinding.editTextAuthor, listener);
        BindingUtils.textChangeListeners(mBinding.editTextTitle, listener);
    }

    private void resetViews() {
        mBinding.sliderRating.setValue(mBinding.sliderRating.getValueFrom());
        mBinding.sliderRelease.setValue(mBinding.sliderRelease.getValueFrom());
        mBinding.sliderAmount.setValue(mBinding.sliderAmount.getValueFrom());

        mBinding.editTextTitle.setText(null);
        mBinding.editTextAuthor.setText(null);

        mBinding.spinOrder.setSelection(-1);
        mBinding.spinTitleContain.setSelection(-1);
        mBinding.spinAuthContain.setSelection(-1);
        mBinding.spinGenreIncl.setSelection(-1);

        mBinding.radioGroupType.clearCheck();
        mBinding.radioGroupStatus.clearCheck();

        for (int i = 0; i < mBinding.gridGenre.getChildCount(); i++) {
            ((MaterialCheckBox) mBinding.gridGenre.getChildAt(i)).setChecked(false);
        }
    }

    public interface OnSearchClickedListener {
        void onSearchClicked();
    }
}
