package com.manager.assistant.ui.pages.main.bookkeeping.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.databinding.ViewHolderDateHeaderBinding;

public class DateHeaderAdapter extends RecyclerView.Adapter<DateHeaderAdapter.DateHeaderViewHolder> {
    private final String date;  //日期

    public DateHeaderAdapter(String date) {
        this.date = date;
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        ViewHolderDateHeaderBinding binding;

        public DateHeaderViewHolder(@NonNull ViewHolderDateHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public DateHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderDateHeaderBinding binding = ViewHolderDateHeaderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DateHeaderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateHeaderViewHolder holder, int position) {
        holder.binding.dateText.setText(date);
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
