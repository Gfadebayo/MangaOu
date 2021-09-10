package com.exzell.mangaplayground.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TitleAdapter extends RecyclerView.Adapter<TitleAdapter.HeaderViewHolder> {
    private static final String PAYLOAD_DRAWABLE = "drawable change";

    private final Context mContext;
    private String mTitle;
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> mBodyAdapter;
    private Drawable mDrawable;
    private View.OnClickListener mParentListener = null;

    private boolean useDrawable = true;

    public TitleAdapter(Context context, String title, RecyclerView.Adapter<? extends RecyclerView.ViewHolder> bodyAdapter) {
        mContext = context;
        mTitle = title;
        mBodyAdapter = bodyAdapter;
        setHasStableIds(true);
    }

    public void setTitle(String title) {
        if (title.equals(mTitle)) return;
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
        holder.mText.setText(mTitle);
        if (!useDrawable) holder.mText.setCompoundDrawablesRelative(null, null, null, null);
        else if (mDrawable != null)
            holder.mText.setCompoundDrawablesRelative(null, null, mDrawable, null);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TitleAdapter.HeaderViewHolder holder, int position, @NotNull List<Object> payloads) {
        if (payloads.contains(PAYLOAD_DRAWABLE))
            holder.mText.setCompoundDrawablesRelative(null, null, mDrawable, null);
        else super.onBindViewHolder(holder, position, payloads);
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

    public void setParentListener(View.OnClickListener listener){
        mParentListener = listener;
    }

    public void setDrawable(Drawable drawable){
        useDrawable = drawable != null;
        mDrawable = drawable;
        notifyItemChanged(0, PAYLOAD_DRAWABLE);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView mText;

        public HeaderViewHolder(View itemView){
            super(itemView);
            mText = itemView.findViewById(R.id.text_header);

            itemView.setOnClickListener(mParentListener);
        }
    }
}
