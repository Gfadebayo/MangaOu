package com.exzell.mangaplayground.download;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.exzell.mangaplayground.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private List<Download> mDownloads;
    private final Context mContext;
    private OnPopupItemClicked mPopupListener;

    public DownloadAdapter(List<Download> mDownloads, Context mContext) {
        this.mDownloads = new ArrayList<>(mDownloads);
        this.mContext = mContext;
    }

    public void setListener(OnPopupItemClicked listener){
        mPopupListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.fragment_download, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Download d = mDownloads.get(position);
        String stateLength = d.getState() + "(" + d.getProgress() + "/" + d.getLength() + ")";

        holder.mTitle.setText(setText(d));
        holder.mLength.setText(stateLength);


        double p = d.getProgress() / (double) d.getLength();
        holder.mBar.setProgress((int) (p*holder.mBar.getMax()));
    }

    private Spanned setText(Download d){
        String title = d.getTitle();
        String number = d.getChapNumber();
        String html = title + "<br>" + number;
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    @Override
    public int getItemCount() {
        return mDownloads.size();
    }

    public List<Download> getItems(){return mDownloads;}

    public void addDownload(Download newDown){
        if(!mDownloads.contains(newDown)){
            mDownloads.add(newDown);
            notifyItemInserted(mDownloads.size()-1);
        }
    }

    public void removeDownload(Download down){
        int index = mDownloads.indexOf(down);
        mDownloads.remove(down);
        notifyItemRemoved(index);
    }

    public void updateDownload(Download down){
        int index = mDownloads.indexOf(down);
        if(index != -1){
            mDownloads.set(index, down);
            notifyItemChanged(index);
        }
    }

    private PopupMenu.OnMenuItemClickListener popListener(int position){
        return item -> {

            Download d = mDownloads.get(position);
            mPopupListener.onPopupClicked(d, item);

            return true;
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public MaterialTextView mTitle;
        public MaterialTextView mLength;
        public ProgressBar mBar;
        public MaterialButton mButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.text_download_title);
            mLength = itemView.findViewById(R.id.text_download_length);
            mBar = itemView.findViewById(R.id.progress_download);
            ImageView img = itemView.findViewById(R.id.button_download_menu);
            img.setOnClickListener(v -> createPopUp(v));

        }

        private void createPopUp(View anchor){
            PopupMenu popupMenu = new PopupMenu(mContext, anchor);
            popupMenu.inflate(R.menu.download_extra_menu);
            popupMenu.setOnMenuItemClickListener(popListener(getAbsoluteAdapterPosition()));

            MenuItem pauseItem = popupMenu.getMenu().getItem(0);

            if(popUpText() == null) pauseItem.setVisible(false);
            else pauseItem.setTitle(popUpText());

            popupMenu.show();
        }


        private String popUpText(){
            //As the text of a popup menu cannot change, we need to change the items text before it is shown
            //and since clicking the menu item changes the state of the download, we can use that
            //to infer the text the item should have
            Download.State state = mDownloads.get(getAbsoluteAdapterPosition()).getState();

            if (state.equals(Download.State.PAUSED) || state.equals(Download.State.ERROR)) return "Resume";
            else if(state.equals(Download.State.DOWNLOADING) || state.equals(Download.State.QUEUED)) return "Pause";
            else return null;
        }
    }

    public interface OnPopupItemClicked{
        void onPopupClicked(Download downloadClicked, MenuItem itemClicked);
    }
}
