package com.sly.coffer.ui.others.selections.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.ui.pages.main.bookkeeping.AccountMediaAdapter;

public class MediaIdKeyProvider extends ItemKeyProvider<Long> {
    private final AccountMediaAdapter adapter;

    public MediaIdKeyProvider(AccountMediaAdapter adapter) {
        super(SCOPE_MAPPED);
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        if (position >= 0 && position < adapter.getItemCount()) {
            return adapter.getItemId(position);
        }
        return null;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        for (int i = 0; i < adapter.getCurrentList().size(); i++) {
            if (adapter.getCurrentList().get(i).getItemId() == key) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
}
