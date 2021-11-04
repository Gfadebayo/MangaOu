package com.exzell.mangaplayground.utils;

import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.ArrayRes;
import androidx.databinding.BindingAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.MangaSearch;
import com.exzell.mangaplayground.customview.ImageViewTarget;
import com.exzell.mangaplayground.fragment.EmptyFragment;
import com.exzell.mangaplayground.models.Manga;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textview.MaterialTextView;
import com.tiper.MaterialSpinner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class BindingUtils {

    //Used in fragment_manga, list_manga
    @BindingAdapter(value = {"thumbnailLink", "imageTarget"})
    public static void addThumbnail(ImageView v, String link, ImageViewTarget target) {
        if (target == null) target = new ImageViewTarget(v, null, null);

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

    public static void setSpinnerItems(MaterialSpinner spinner, @ArrayRes int arrayRes, String selectedItem, BiConsumer<Integer, String> callback) {
        String[] array = spinner.getContext().getResources().getStringArray(arrayRes);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(spinner.getContext(), R.layout.spinner_textview, array);
        spinner.setAdapter(spinAdapter);

        if (!selectedItem.isEmpty()) {

            int index = Arrays.stream(array).map(t -> t.toLowerCase())
                    .collect(Collectors.toList()).indexOf(selectedItem.toLowerCase());

            if (index >= 0) spinner.setSelection(index);
        }

        MaterialSpinner.OnItemSelectedListener listener = new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner materialSpinner, @Nullable View view, int i, long l) {
                callback.accept(materialSpinner.getId(), spinAdapter.getItem(i));
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner materialSpinner) {
            }
        };
        spinner.setOnItemSelectedListener(listener);
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
        if (left > 0) IntStream.range(parent.getChildCount() - left, parent.getChildCount())
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

    public static void textChangeListeners(EditText editText, BiConsumer<Integer, String> callback) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                callback.accept(editText.getId(), s.toString());
            }
        });
    }

    public static void populateGrid(GridLayout layout, MangaSearch search, BiConsumer<String, Boolean> callback) {
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) ->
                callback.accept(buttonView.getText().toString(), isChecked);

        int colIndex = 0;
        int rowIndex = 0;
        List<Genre> selectedGenres = search.getGenre();

        for (Genre genre : Genre.values()) {
            String stringGenre = genre.dispName;

            MaterialCheckBox box = new MaterialCheckBox(layout.getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(rowIndex), GridLayout.spec(colIndex, 0f));

            if (colIndex >= layout.getColumnCount() - 1) {
                colIndex = 0;
                rowIndex++;
            } else colIndex++;

            box.setText(stringGenre);
            if (selectedGenres != null && selectedGenres.contains(genre)) box.setChecked(true);
            box.setOnCheckedChangeListener(listener);
            layout.addView(box, params);
        }
    }

    @BindingAdapter(value = {"title", "content"})
    public static void splitTitleAndHeaderText(MaterialTextView textView, String title, String content) {
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(textView.getResources().getColor(R.color.manga_detail_title, null));

        SpannableStringBuilder spanString = SpannableStringBuilder.valueOf(title + "\n" + content);
        spanString.setSpan(colorSpan, 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spanString);
    }
}
