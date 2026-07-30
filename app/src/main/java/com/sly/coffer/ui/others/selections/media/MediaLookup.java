package com.sly.coffer.ui.others.selections.media;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.ui.pages.main.bookkeeping.AccountMediaAdapter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MediaLookup extends ItemDetailsLookup<Long> {
    private final RecyclerView recyclerView;

    public MediaLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public @Nullable ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof AccountMediaAdapter.MediaViewHolder) {
                return ((AccountMediaAdapter.MediaViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
