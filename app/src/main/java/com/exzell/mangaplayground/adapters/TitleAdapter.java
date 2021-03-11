package com.exzell.mangaplayground.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.google.android.material.textview.MaterialTextView;

public class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.HeaderViewHolder> {
    private final Context mContext;
    private String mTitle;
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> mBodyAdapter;
    private View.OnClickListener mListener;
    private Drawable mDrawable;
    private View.OnClickListener mParentListener = null;
    private int mDrawableRes;

    public TitleAdapter(Context context, String title, RecyclerView.Adapter<? extends RecyclerView.ViewHolder> bodyAdapter){
        mContext = context;
        this.mTitle = title;
        mBodyAdapter = bodyAdapter;
        setHasStableIds(true);
    }

    public void setTitle(String title){
        if(title.equals(mTitle)) return;
        mTitle = title;
        notifyItemChanged(0);
    }

    public String getTitle(){
        return mTitle;
    }

    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(viewType, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {

        if(mDrawable == null) holder.mButton.setImageResource(mDrawableRes);
        else holder.mButton.setImageDrawable(mDrawable);

        holder.mText.setText(mTitle);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.header;
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> getBodyAdapter(){
        return mBodyAdapter;
    }

    public void setImageListener(View.OnClickListener listener){
        mListener = listener;
    }

    public void setParentListener(View.OnClickListener listener){
        mParentListener = listener;
    }

    public void setDrawable(Drawable drawable){
        mDrawable = drawable;
        notifyItemChanged(0);
    }

    public void setDrawableResource(@DrawableRes int res){
        mDrawableRes = res;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView mText;
        public ImageView mButton;

        public HeaderViewHolder(View itemView){
            super(itemView);
            mText = itemView.findViewById(R.id.text_header);
            mButton = itemView.findViewById(R.id.button_header);

            itemView.setOnClickListener(mParentListener);
            mButton.setOnClickListener(mListener);
        }
    }
}
