package com.exzell.mangaplayground.utils;

import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.databinding.BindingAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.customview.ImageViewTarget;
import com.exzell.mangaplayground.fragment.EmptyFragment;
import com.exzell.mangaplayground.models.Manga;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textview.MaterialTextView;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kotlin.Unit;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class BindingUtils {

    //Used in fragment_manga, list_manga
    @BindingAdapter("thumbnail")
    public static void addThumbnail(ImageView v, String link) {
        ProgressBar bar = v.getRootView().findViewById(R.id.indicator_thumbnail);
        ImageViewTarget target = new ImageViewTarget(v, null, bar);

        Request req = Glide.with(v)
                .load(link)
                .into(target)
                .getRequest();
        if (!req.isRunning()) req.begin();
    }

    @BindingAdapter("setRating")
    public static void setRating(MaterialRatingBar bar, double rating) {

        float rate = (float) ((rating * bar.getNumStars()) / 10);
        bar.setRating(rate);
    }

    @BindingAdapter("ellipsizeChecker")
    public static void checkTextEllipsize(MaterialTextView textView, int maxLines) {
        textView.post(() -> {
            Layout layout = textView.getLayout();

            if (layout == null) return;

            int lineCount = layout.getLineCount();

            if (layout.getEllipsisCount(lineCount - 1) > 0) {
                textView.setOnClickListener(v -> {
                    int currentMax = textView.getMaxLines();

                    if (currentMax < Integer.MAX_VALUE) {
                        textView.setMaxLines(Integer.MAX_VALUE);
                        textView.setEllipsize(null);

                    } else {
                        textView.setMaxLines(maxLines);
                        textView.setEllipsize(TextUtils.TruncateAt.END);
                    }
                });
            }
        });
    }

    public static void setSpinnerItems(PowerSpinnerView spinner, List<String> items, String selectedItem) {
        spinner.setItems(items);

        if (selectedItem == null) return;

        int selectedIndex = items.stream().map(String::toLowerCase).collect(Collectors.toList()).indexOf(selectedItem.toLowerCase());

        if (selectedIndex >= 0) spinner.selectItemByIndex(selectedIndex);

        spinner.setOnSpinnerOutsideTouchListener((view, motionEvent) -> {
            spinner.dismiss();
            return Unit.INSTANCE;
        });
    }

    @BindingAdapter("createChips")
    public static void chips(ChipGroup parent, Manga manga) {
        if (manga == null) return;

        NavController con = Navigation.findNavController(parent);

        createOrHideChips(manga.getGenres().size() - parent.getChildCount(), parent);

        for (int i = 0; i < manga.getGenres().size(); i++) {
            Genre g = manga.getGenres().get(i);
            Chip ch = (Chip) parent.getChildAt(i);
            ch.setText(g.dispName);
            ch.setOnClickListener(v -> {
                Bundle bund = new Bundle(2);

                bund.putString(EmptyFragment.LINK, g.link);
                bund.putString(EmptyFragment.TITLE, g.dispName);

                con.navigate(R.id.nav_empty, bund);
            });
            ch.setVisibility(View.VISIBLE);
        }

        int left = parent.getChildCount() - manga.getGenres().size();
        if(left > 0) IntStream.range(parent.getChildCount()-left, parent.getChildCount())
                .forEach(i -> parent.getChildAt(i).setVisibility(View.GONE));
    }

    private static void createOrHideChips(int size, ChipGroup parent) {
        for (int i = 0; i < size; i++) {
            Chip ch = new Chip(parent.getContext());
            ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            parent.addView(ch, params);
        }
    }


    public static String listToString(List<String> list, String delimiter) {
        return list.stream().collect(Collectors.joining(delimiter));
    }
}
