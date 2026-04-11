package com.manager.assistant.ui.pages.picture;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.manager.assistant.databinding.ViewHolderFullScreenPictureBinding;

public class FullScreenPictureAdapter
        extends RecyclerView.Adapter<FullScreenPictureAdapter.FullScreenPictureViewHolder> {
    private final String[] pictureUris; //图片Uri字符串数组

    public static class FullScreenPictureViewHolder extends RecyclerView.ViewHolder {
        ViewHolderFullScreenPictureBinding binding;

        public FullScreenPictureViewHolder(@NonNull ViewHolderFullScreenPictureBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 通过Uri设置图片资源
         *
         * @param context 上下文
         * @param uri     图片Uri
         */
        public void setPicture(Context context, Uri uri) {
            Glide.with(context)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.photoView);
        }
    }

    public FullScreenPictureAdapter(String[] pictureUris) {
        this.pictureUris = pictureUris;
    }

    @NonNull
    @Override
    public FullScreenPictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderFullScreenPictureBinding binding = ViewHolderFullScreenPictureBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FullScreenPictureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenPictureViewHolder holder, int position) {
        Uri pictureUri = Uri.parse(pictureUris[position]);
        holder.setPicture(holder.itemView.getContext(), pictureUri);
    }

    @Override
    public int getItemCount() {
        return pictureUris.length;
    }
}
