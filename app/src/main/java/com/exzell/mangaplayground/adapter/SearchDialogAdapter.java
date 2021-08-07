package com.exzell.mangaplayground.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.advancedsearch.Genre;
import com.exzell.mangaplayground.advancedsearch.MangaSearch;
import com.exzell.mangaplayground.advancedsearch.Order;
import com.exzell.mangaplayground.advancedsearch.Type;
import com.exzell.mangaplayground.databinding.HeaderBinding;
import com.exzell.mangaplayground.databinding.LayoutRadioCheckboxBinding;
import com.exzell.mangaplayground.databinding.SearchEditViewBinding;
import com.exzell.mangaplayground.utils.BindingUtils;
import com.exzell.mangaplayground.utils.ContextExtKt;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class SearchDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String PAYLOAD_CHANGE_VISIBILITY = "change view visibility";
    private Context mContext;
    private MangaSearch mSearch;
    private List<Integer> mHiddenViews = new ArrayList<>(3);
    private CompoundButton.OnCheckedChangeListener mButtonListener;
    private BiConsumer<Integer, String> mEditTextListener;
    private BiConsumer<Integer, String> mSpinnerListener;
    private MaterialRatingBar.OnRatingChangeListener mRatingListener;

    public SearchDialogAdapter(Context context, MangaSearch search) {
        this.mContext = context;
        this.mSearch = search;

        Collections.addAll(mHiddenViews, R.string.status, R.string.type, R.string.genre);
    }

    public void setButtonListener(CompoundButton.OnCheckedChangeListener buttonListener) {
        mButtonListener = buttonListener;
    }

    public void setEditTextListener(BiConsumer<Integer, String> textListener) {
        mEditTextListener = textListener;
    }

    public void setSpinnerListener(BiConsumer<Integer, String> spinnerListener) {
        mSpinnerListener = spinnerListener;
    }

    public void setRatingListener(MaterialRatingBar.OnRatingChangeListener ratingListener) {
        mRatingListener = ratingListener;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(viewType, parent, false);

        if (viewType == R.layout.search_edit_view) return new EditableViewHolder(v);
        else if (viewType == R.layout.header) return new HeaderViewHolder(v);
        else return new BoxGroupViewModel(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof EditableViewHolder) {
            EditableViewHolder hold = (EditableViewHolder) holder;

            hold.mBinding.editSearchTitle.setText(mSearch.getTitle());
            hold.mBinding.editSearchAuth.setText(mSearch.getAuthor());

            BindingUtils.setRating(hold.mBinding.ratingRating, mSearch.getRating());

            List<String> containsList = Arrays.asList(mContext.getResources().getStringArray(R.array.contain_values));
            BindingUtils.setSpinnerItems(hold.mBinding.spinAuthContain.getRoot(), containsList, mSearch.getAuthorContain());
            BindingUtils.setSpinnerItems(hold.mBinding.spinTitleContain.getRoot(), containsList, mSearch.getTitleContain());


//         List<String> chapAmount = Arrays.stream(MangaSearch.CHAPTER_AMOUNT).mapToObj(val -> val + "+").collect(Collectors.toList());
            List<String> chapAmount = Arrays.asList(mContext.getResources().getStringArray(R.array.chapter_values));
            BindingUtils.setSpinnerItems(hold.mBinding.spinChapterAmount.getRoot(), chapAmount, mSearch.getChapterAmount() + "+");


            List<String> orders = Arrays.stream(Order.values()).map(order -> order.dispName).collect(Collectors.toList());
            String dispName = mSearch.getOrder() != null ? mSearch.getOrder().dispName : "";
            BindingUtils.setSpinnerItems(hold.mBinding.spinOrder.getRoot(), orders, dispName);


            List<String> releaseDates = IntStream.rangeClosed(MangaSearch.RELEASE_POINTS[0], MangaSearch.RELEASE_POINTS[1])
                    .mapToObj(String::valueOf).collect(Collectors.toList());
            BindingUtils.setSpinnerItems(hold.mBinding.spinRelease.getRoot(), releaseDates, String.valueOf(mSearch.getRelease()));

        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder hold = (HeaderViewHolder) holder;

            hold.mBinding.buttonHeader.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24);

            int textRes;

            if (position == 1) textRes = R.string.status;
            else if (position == 3) textRes = R.string.type;
            else textRes = R.string.genre;

            hold.mBinding.textHeader.setText(textRes);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position, @NonNull @NotNull List<Object> payloads) {
        if (payloads.contains(PAYLOAD_CHANGE_VISIBILITY)) {
            BoxGroupViewModel hold = (BoxGroupViewModel) holder;
            View view = hold.mBinding.getRoot().findViewById(hold.mViewId);
            int currentVisibility = view.getVisibility();

            view.setVisibility(currentVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);

        } else super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) return R.layout.search_edit_view;
        else if (position == 1 || position == 3 || position == 5) return R.layout.header;
        else return R.layout.layout_radio_checkbox;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderBinding mBinding;

        public HeaderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mBinding = HeaderBinding.bind(itemView);

            mBinding.getRoot().setCardBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT));
            mBinding.getRoot().setCardElevation(0);

            ColorStateList secondaryColor = ColorStateList.valueOf(ContextExtKt.getSecondaryColor(mContext));
            mBinding.buttonHeader.setImageTintList(secondaryColor);

            mBinding.textHeader.setTextAppearance(mContext, R.style.SearchTextAppearance);

            itemView.setOnClickListener(v -> {
                int pos = getAbsoluteAdapterPosition();

                notifyItemChanged(pos + 1, PAYLOAD_CHANGE_VISIBILITY);

                mBinding.buttonHeader.setSelected(!mBinding.buttonHeader.isSelected());
            });
        }
    }

    public class EditableViewHolder extends RecyclerView.ViewHolder {
        public SearchEditViewBinding mBinding;

        public EditableViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mBinding = SearchEditViewBinding.bind(itemView);

            itemView.post(() -> {
                TextWatcher watcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        View view = itemView.findFocus();
                        int id = view != null ? view.getId() : View.NO_ID;

                        if (mEditTextListener != null) mEditTextListener.accept(id, s.toString());
                    }
                };

                mBinding.editSearchAuth.addTextChangedListener(watcher);
                mBinding.editSearchTitle.addTextChangedListener(watcher);


                mBinding.spinOrder.getRoot().setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
                    if (mSpinnerListener != null)
                        mSpinnerListener.accept(mBinding.spinOrder.getRoot().getId(), s);
                });
                mBinding.spinRelease.getRoot().setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
                    if (mSpinnerListener != null)
                        mSpinnerListener.accept(mBinding.spinRelease.getRoot().getId(), s);
                });
                mBinding.spinChapterAmount.getRoot().setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
                    if (mSpinnerListener != null)
                        mSpinnerListener.accept(mBinding.spinChapterAmount.getRoot().getId(), s);
                });
                mBinding.spinAuthContain.getRoot().setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
                    if (mSpinnerListener != null)
                        mSpinnerListener.accept(mBinding.spinAuthContain.getRoot().getId(), s);
                });
                mBinding.spinTitleContain.getRoot().setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
                    if (mSpinnerListener != null)
                        mSpinnerListener.accept(mBinding.spinTitleContain.getRoot().getId(), s);
                });

                mBinding.ratingRating.setOnRatingChangeListener(mRatingListener);
            });
        }
    }

    public class BoxGroupViewModel extends RecyclerView.ViewHolder {
        public LayoutRadioCheckboxBinding mBinding;
        //the id of the view that is actually shown among the 2
        public int mViewId;

        public BoxGroupViewModel(@NonNull @NotNull View itemView) {
            super(itemView);

            mBinding = LayoutRadioCheckboxBinding.bind(itemView);

            itemView.post(() -> {
                int pos = getBindingAdapterPosition();
                if (pos == getItemCount() - 1) {
                    mViewId = mBinding.genreRoot.getId();
                    genreInclusion(mBinding.spinGenreIncl.getRoot());
                    populateGrid(mBinding.groupGrid);

                } else {
                    mViewId = mBinding.groupRadio.getId();

                    String typDisp = mSearch.getType() != null ? mSearch.getType().dispName : "";
                    String selected = pos == 2 ? mSearch.getStatus() : typDisp;
                    List<String> texts = pos == 2 ? Arrays.asList(mContext.getResources().getStringArray(R.array.search_status)) :
                            Arrays.stream(Type.values()).filter(type -> type.isSearchable)
                                    .map(type -> type.dispName).collect(Collectors.toList());
                    createButtons(mBinding.groupRadio, texts, selected);
                }


                mBinding.spinGenreIncl.getRoot().setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s) -> {
                    if (mSpinnerListener != null)
                        mSpinnerListener.accept(mBinding.spinGenreIncl.getRoot().getId(), s);
                });
            });
        }

        private void createButtons(RadioGroup parent, List<String> texts, String selectedButton) {
            texts.forEach(text -> {
                MaterialRadioButton butt = new MaterialRadioButton(mContext);
                butt.setText(text);
                butt.setOnCheckedChangeListener(mButtonListener);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                parent.addView(butt, params);

                if (text.equalsIgnoreCase(selectedButton)) butt.setChecked(true);
            });
        }

        private void populateGrid(GridLayout parent) {
            int colIndex = 0;
            int rowIndex = 0;
            List<Genre> selectedGenres = mSearch.getGenre();

            for (Genre genre : Genre.values()) {
                String stringGenre = genre.dispName;

                MaterialCheckBox box = new MaterialCheckBox(mContext);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(rowIndex), GridLayout.spec(colIndex, 0f));

                if (colIndex >= parent.getColumnCount() - 1) {
                    colIndex = 0;
                    rowIndex++;
                } else colIndex++;

                box.setText(stringGenre);
                if (selectedGenres != null && selectedGenres.contains(genre)) box.setChecked(true);
                box.setOnCheckedChangeListener(mButtonListener);
                parent.addView(box, params);
            }
        }

        private void genreInclusion(PowerSpinnerView spinner) {
            List<String> values = Arrays.asList(mContext.getResources().getStringArray(R.array.incl_values));
            spinner.setItems(R.array.incl_values);
            int index = values.indexOf(mSearch.getGenreInclusion());
            if (index > -1) spinner.selectItemByIndex(index);
            spinner.setSpinnerOutsideTouchListener((view, motionEvent) -> spinner.dismiss());
        }
    }
}
