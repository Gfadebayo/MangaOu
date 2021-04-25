package com.exzell.mangaplayground.selection;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public abstract class DetailsViewHolder extends RecyclerView.ViewHolder {

    public DetailsViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract ItemDetailsLookup.ItemDetails<Long> getDetails();
}
