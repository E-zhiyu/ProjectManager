package com.sly.coffer.ui.others.selections.account;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.ui.pages.report.AccountSelectListAdapter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class AccountLookup extends ItemDetailsLookup<Long> {
    private final RecyclerView recyclerView;

    public AccountLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public @Nullable ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof AccountSelectListAdapter.ItemViewHolder) {
                return ((AccountSelectListAdapter.ItemViewHolder) holder).getItemDetails();
            } else if (holder instanceof AccountSelectListAdapter.SeparatorViewHolder) {
                return ((AccountSelectListAdapter.SeparatorViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
