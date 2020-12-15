package com.exzell.mangaplayground.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ViewMultiplierAdapter extends RecyclerView.Adapter<ViewMultiplierAdapter.ViewHolder> {

    public static final int TYPE_CHECKBOX = 0;
    public static final int TYPE_RADIO = 1;

    private final Context mContext;
    private List<String> mTexts;
    private CompoundButton.OnCheckedChangeListener mListener;
    private List<String> mDefaultVals;
    private OnSpinnerItemSelectedListener mSpinnerListener;
    private int mType;
    private String mSpinnerDefaultValue;


    public ViewMultiplierAdapter(Context context, List<String> texts, int type){
        mContext = context;
        mTexts = texts;
        mType = type;
    }

    public void setGenreSpinnerValue(String value){
        mSpinnerDefaultValue = value;
    }

    private void capitalizeData(){
        for(String s : mTexts){
            s.substring(0, 1).toUpperCase();
        }

        if(!mDefaultVals.isEmpty()){
            for(String s : mDefaultVals){
                s.substring(0, 1).toUpperCase();
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mType == TYPE_RADIO){
            RadioGroup mParent = new RadioGroup(mContext);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            mParent.setLayoutParams(params);
            return new ViewHolder(mParent);
        }else{
            View v = LayoutInflater.from(mContext).inflate(R.layout.genre_layout, parent, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(mType == TYPE_CHECKBOX){
            genreInclusion(holder.mView.findViewById(R.id.spin_genre));
            populateGrid(holder.mView.findViewById(R.id.genre_grid));
        }else {
            RadioGroup group = ((RadioGroup) holder.mView);
            createButtons(group);
        }
    }

    public void addSpinnerListener(OnSpinnerItemSelectedListener<String> listener){
        mSpinnerListener = listener;
    }

    private void genreInclusion(PowerSpinnerView spinner){
        List<String> values = Arrays.asList(mContext.getResources().getStringArray(R.array.incl_values));
        spinner.setItems(R.array.incl_values);
        int index = values.indexOf(mSpinnerDefaultValue);
        if(index > -1) spinner.selectItemByIndex(index);
        spinner.setSpinnerOutsideTouchListener((view, motionEvent) -> spinner.dismiss());
        spinner.setOnSpinnerItemSelectedListener(mSpinnerListener);
    }

    private void populateGrid(GridLayout parent){
        int colIndex = 0;
        int rowIndex = 0;

        for (String genre : mTexts) {
            MaterialCheckBox box = new MaterialCheckBox(mContext);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(rowIndex), GridLayout.spec(colIndex, 0f));

            if (colIndex >= 2) {
                colIndex = 0;
                rowIndex++;
            }else colIndex++;

            box.setText(genre);
            if(mDefaultVals != null && mDefaultVals.contains(genre)) box.setChecked(true);
            box.setOnCheckedChangeListener(mListener);
            parent.addView(box, params);
        }
    }

    private void createButtons(RadioGroup parent){
        mTexts.forEach(pos -> {
            MaterialRadioButton butt = new MaterialRadioButton(mContext);
            butt.setText(pos);
            if(mDefaultVals != null && mDefaultVals.contains(pos)) butt.setChecked(true);
            butt.setOnCheckedChangeListener(mListener);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            parent.addView(butt, params);
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public void setListener(CompoundButton.OnCheckedChangeListener listener){
        mListener = listener;
    }

    public void setDefaultValues(List<String> values){
        mDefaultVals = values;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View mView;

        public ViewHolder(View itemView){
            super(itemView);

            mView = itemView;
        }
    }
}
