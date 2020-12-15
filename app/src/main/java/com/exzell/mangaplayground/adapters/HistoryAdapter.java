package com.exzell.mangaplayground.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.exzell.mangaplayground.R;
import com.exzell.mangaplayground.io.database.DBManga;
import com.exzell.mangaplayground.reader.ReadActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final Context mContext;
    private List<DBManga> mMangas;

    private View.OnClickListener mResumeClicked;
    private View.OnClickListener mDeleteClicked;

    public HistoryAdapter(Context context, List<DBManga> mangas) {
        mContext = context;
        mMangas = mangas;
    }

    public void setOnButtonsClickedListener(View.OnClickListener onResume, View.OnClickListener onDelete){
        mResumeClicked = onResume;

        mDeleteClicked = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(mContext).inflate(R.layout.list_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DBManga man = mMangas.get(position);

        holder.mTitle.setText(man.getTitle());
        holder.mChapter.setText(man.getLastChapter().getTitle());

        Request request = Glide.with(mContext).load(man.getThumbnailLink()).into(holder.mImage).getRequest();
        if(!request.isRunning()) request.begin();
    }

    @Override
    public int getItemCount() {
        return mMangas.size();
    }

    public List<DBManga> getMangas() {
        return mMangas;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ShapeableImageView mImage;
        public MaterialTextView mTitle;
        public MaterialTextView mChapter;
        public MaterialButton mResume;
        public MaterialButton mDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mImage = itemView.findViewById(R.id.image_manga);
            mTitle = itemView.findViewById(R.id.text_title_history);
            mChapter = itemView.findViewById(R.id.text_chapter_history);
            mResume = itemView.findViewById(R.id.button_resume);
            mDelete = itemView.findViewById(R.id.button_delete);

            mResume.setOnClickListener(mResumeClicked);

            mDelete.setOnClickListener(mDeleteClicked);
        }
    }
}
