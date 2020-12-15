package com.exzell.mangaplayground.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.adapters.TitleAdapter;
import com.exzell.mangaplayground.adapters.ViewMultiplierAdapter;
import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.Type;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class SearchDialogFragment extends BottomSheetDialogFragment {

    private SearchViewModel mViewModel;
    private RecyclerView mRecyclerView;
    private ConcatAdapter mAdapter;
    private OnSearchClickedListener mListener;

    public static SearchDialogFragment getInstance(){return new SearchDialogFragment();}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory
                (requireActivity().getApplication(), this)).get(SearchViewModel.class);

        mListener = (SearchFragment) getParentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] names = {"Status", "Type", "Genre"};

        setDefaultValues(view);

        mRecyclerView = view.findViewById(R.id.recycler_search);

        Arrays.stream(names).forEach(s -> {
            ViewMultiplierAdapter multiAdapter = createMultiAdapter(s);
            TitleAdapter ta = new TitleAdapter(requireActivity(), s, multiAdapter);

            ta.setDrawableResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            ta.setParentListener(onHeaderClicked(ta));

            if(mAdapter == null) mAdapter = new ConcatAdapter(ta);
            else mAdapter.addAdapter(ta);
        });

        mRecyclerView.setAdapter(mAdapter);


        view.findViewById(R.id.button_search).setOnClickListener(v -> {
            mListener.onSearchClicked();
            dismiss();
        });

        view.findViewById(R.id.button_reset).setOnClickListener(v -> {
            mViewModel.resetValues();
            mListener.onSearchClicked();
            dismiss();
        });
    }

    private View.OnClickListener onHeaderClicked(TitleAdapter adapter){
        return v -> {
            if(mAdapter.getAdapters().contains(adapter.getBodyAdapter())){
                mAdapter.removeAdapter(adapter.getBodyAdapter());
            }else {
                int index = mAdapter.getAdapters().indexOf(adapter);
                mAdapter.addAdapter(index+1, adapter.getBodyAdapter());
            }


            ViewPropertyAnimator animate = v.findViewById(R.id.button_header).animate();
            animate.cancel();
            animate.rotationBy(180f).start();
        };
    }

    private ViewMultiplierAdapter createMultiAdapter(String name){
        //Deafult is status
        List<String> list = mViewModel.statusData;
        List<String> defaults = Collections.singletonList(mViewModel.getStatus());

        if(name.equals("Genre")) {
            defaults = mViewModel.getSelectedGenres();
            list = Stream.of(Genre.values()).map(genre -> genre.dispName).collect(Collectors.toList());
        } else if(name.equals("Type")) {
            defaults = Collections.singletonList(mViewModel.getType());
            list = Stream.of(Type.values()).filter(type -> type.isSearchable).map(type -> type.dispName).collect(Collectors.toList());
        }

        int type = name.equals("Genre") ? ViewMultiplierAdapter.TYPE_CHECKBOX : ViewMultiplierAdapter.TYPE_RADIO;
        ViewMultiplierAdapter ad = new ViewMultiplierAdapter(requireContext(), list, type);

        if(type == ViewMultiplierAdapter.TYPE_CHECKBOX) ad.setGenreSpinnerValue(mViewModel.getGenreInclusion());
        ad.addSpinnerListener((i, s) -> {
            if(Character.isDigit(s.charAt(0))) mViewModel.setGenreInclusion(s.toLowerCase());
        });

        ad.setDefaultValues(defaults);
        ad.setListener((buttonView, isChecked) -> {
            String text = buttonView.getText().toString();
            if(name.equals("Status") && isChecked) mViewModel.setStatus(text.toLowerCase());
            else if(name.equals("Type") && isChecked) mViewModel.setType(text);
            else mViewModel.setGenres(text, isChecked);
        });

        return ad;
    }

    private void setDefaultValues(View root){

        EditText editTitle = root.findViewById(R.id.edit_search_title);
        EditText editAuthor = root.findViewById(R.id.edit_search_auth);
        PowerSpinnerView chapSpinner = root.findViewById(R.id.spin_chapter);
        PowerSpinnerView relSpinner = root.findViewById(R.id.spin_release);
        PowerSpinnerView containAuthSpinner = root.findViewById(R.id.spin_auth_contain);
        PowerSpinnerView containTitleSpinner = root.findViewById(R.id.spin_title_contain);

        editTitle.setText(mViewModel.getName(false));
        editTitle.addTextChangedListener(setTextChangeListener(editTitle));

        editAuthor.setText(mViewModel.getName(true));
        editAuthor.addTextChangedListener(setTextChangeListener(editAuthor));


        containAuthSpinner.setItems(R.array.contain_values);
        containAuthSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>)
                (i, s) -> mViewModel.setContainValue(true, s.toLowerCase()));

        containTitleSpinner.setItems(R.array.contain_values);
        containTitleSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>)
                (i, s) -> mViewModel.setContainValue(false, s.toLowerCase()));

        relSpinner.setSpinnerOutsideTouchListener((v, m) -> relSpinner.dismiss());
        relSpinner.setItems(mViewModel.releaseData);
        if(mViewModel.getRelease() != -1) relSpinner.selectItemByIndex(mViewModel.releaseData.indexOf(String.valueOf(mViewModel.getRelease())));
        relSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, integer)
                -> mViewModel.setRelease(Integer.parseInt(integer)));


        chapSpinner.setItems(R.array.chapter_values);
        chapSpinner.setSpinnerOutsideTouchListener(((v, m) -> chapSpinner.dismiss()));
        if(mViewModel.getChapters() != -1) chapSpinner.selectItemByIndex(mViewModel.chapterData.indexOf(mViewModel.getChapters()));
        chapSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
            String chapter = s.split("\\s+")[0];
            if(Character.isDigit(chapter.charAt(0))) mViewModel.setChapters(Integer.parseInt(chapter));
        });


        ((MaterialRatingBar) root.findViewById(R.id.rating_rating)).setRating(mViewModel.getRating());
        ((MaterialRatingBar)root.findViewById(R.id.rating_rating))
                .setOnRatingChangeListener((ratingBar, rating) -> mViewModel.setRating((int) rating));
    }

    private TextWatcher setTextChangeListener(View v){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(v.getId() == R.id.edit_search_title)
                    mViewModel.setName(false, s.toString());

                else mViewModel.setName(true, s.toString());
            }
        };
    }

    public interface OnSearchClickedListener{
        void onSearchClicked();
    }
}
