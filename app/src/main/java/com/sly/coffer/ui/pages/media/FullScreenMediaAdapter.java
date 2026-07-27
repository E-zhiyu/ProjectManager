package com.sly.coffer.ui.pages.media;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sly.coffer.databinding.ViewHolderFullScreenMediaBinding;

public class FullScreenMediaAdapter
        extends ListAdapter<Uri, FullScreenMediaAdapter.FullScreenPictureViewHolder> {
    private final String[] pictureUris; //图片Uri字符串数组
    private static final DiffUtil.ItemCallback<Uri> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Uri oldItem, @NonNull Uri newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Uri oldItem, @NonNull Uri newItem) {
            return true;
        }
    };

    public static class FullScreenPictureViewHolder extends RecyclerView.ViewHolder {
        ViewHolderFullScreenMediaBinding binding;

        public FullScreenPictureViewHolder(@NonNull ViewHolderFullScreenMediaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 全屏媒体适配器构造方法
     *
     * @param mediaUris 媒体 Uri 字符串数组
     */
    public FullScreenMediaAdapter(String[] mediaUris) {
        super(ITEM_CALLBACK);
        this.pictureUris = mediaUris;
    }

    @NonNull
    @Override
    public FullScreenPictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderFullScreenMediaBinding binding = ViewHolderFullScreenMediaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FullScreenPictureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenPictureViewHolder holder, int position) {
        Uri mediaUri = getItem(position);
        Context context = holder.itemView.getContext();
        Glide.with(context)
                .load(mediaUri)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.binding.photoView);
    }

    @Override
    public int getItemCount() {
        return pictureUris.length;
    }
}
