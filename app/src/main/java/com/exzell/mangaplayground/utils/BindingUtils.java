package com.exzell.mangaplayground.utils;

import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.Type;
import com.exzell.mangaplayground.fragment.EmptyFragment;
import com.exzell.mangaplayground.models.Chapter;
import com.exzell.mangaplayground.models.Manga;
import com.exzell.mangaplayground.viewmodels.SearchViewModel;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import timber.log.Timber;

public class BindingUtils {
    public static final String TAG = "BindingUtils";

    //Used in fragment_manga, list_manga
    @BindingAdapter("thumbnail")
    public static void addThumbnail(ImageView v, String link){
        new Handler().post(() -> {

            Request req = Glide.with(v)
                    .load(link)
                    .placeholder(R.drawable.ic_done_all_black_24dp)
                    .skipMemoryCache(true)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            Timber.i("Failed to get Image");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(v)
                    .getRequest();
            if(!req.isRunning()) req.begin();
        });
    }

    //The 5 below are all used in dialog_search

//    @BindingAdapter(value = "ratingListener")
//    public static void setRatingListener(RatingBar bar, SearchViewModel vm){
//        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                if(fromUser) vm.setRating((int) rating);
//            }
//        });
//    }
//
//    @BindingAdapter(value = {"addGrids", "checkboxListener"})
//    public static void generateGrids(GridLayout grids, List<String> checkedGrids, SearchViewModel vm){
//        int colIndex = 0;
//        int rowIndex = 0;
//
//        for (Genre genre : Genre.values()) {
//            MaterialCheckBox box = new MaterialCheckBox(grids.getMContext());
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(rowIndex), GridLayout.spec(colIndex, 0f));
//
//            if (colIndex > 2) {
//                colIndex = 0;
//                rowIndex++;
//            }else colIndex++;
//
//            box.setText(genre.getDispName());
//            if(checkedGrids.contains(genre.getDispName())) box.setChecked(true);
//            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    vm.setGenres(genre.getDispName(), isChecked);
//                }
//            });
//            grids.addView(box, params);
//        }
//    }
//
//    //used in list_manga
//    @BindingAdapter("createTextViews")
//    public static void create(LinearLayout layout, Manga manga){
//
//        List<Chapter> chaps = new ArrayList<>(manga.getChapters());
//        chaps.stream().filter(Chapter::isNewChap).forEach(chap -> {
//            MaterialTextView text = new MaterialTextView(layout.getMContext());
//            String chapText = chap.getTitle() + "\t\t" + chap.getReleaseDate();
//            text.setText(chapText);
//            text.setOnClickListener(v -> chap.getLink());
//
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layout.addView(text, params);
//        });
//
//
//    }

    @BindingAdapter("createChips")
    public static void chips(ChipGroup parent, Manga manga){
        if(manga == null) return;

        NavController con = Navigation.findNavController(parent);

        createOrHideChips(manga.getGenres().size() - parent.getChildCount(), parent);

        for(int i = 0; i < manga.getGenres().size(); i++){
            Genre g = manga.getGenres().get(i);
            Chip ch = (Chip) parent.getChildAt(i);
            ch.setText(g.dispName);
            ch.setOnClickListener(v -> {
                Bundle bund = new Bundle(2);

                bund.putString(EmptyFragment.TAG, g.link);
                bund.putString(EmptyFragment.TITLE, g.dispName);

                con.navigate(R.id.nav_empty, bund);
            });
            ch.setVisibility(View.VISIBLE);
        }

        int left = parent.getChildCount() - manga.getGenres().size();
        if(left > 0) IntStream.range(parent.getChildCount()-left, parent.getChildCount())
                .forEach(i -> parent.getChildAt(i).setVisibility(View.GONE));
    }

    private static void createOrHideChips(int size, ChipGroup parent){
        for(int i = 0; i < size; i++) {
            Chip ch = new Chip(parent.getContext());
            ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            parent.addView(ch, params);
        }
    }
}
