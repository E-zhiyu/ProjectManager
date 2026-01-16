package com.project.manager.ui.pages.picture;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.project.manager.R;

public class FullScreenPictureAdapter extends RecyclerView.Adapter<FullScreenPictureAdapter.FullScreenPictureViewHolder> {
    private final String[] pictureUris; //图片Uri字符串数组
    private final Context context;      //上下文

    public static class FullScreenPictureViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        public FullScreenPictureViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
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
                    .into(photoView);
        }
    }

    public FullScreenPictureAdapter(Context context, String[] pictureUris) {
        this.context = context;
        this.pictureUris = pictureUris;
    }

    @NonNull
    @Override
    public FullScreenPictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.view_holder_full_screen_picture, parent, false);
        return new FullScreenPictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenPictureViewHolder holder, int position) {
        Uri pictureUri = Uri.parse(pictureUris[position]);
        holder.setPicture(context, pictureUri);
    }

    @Override
    public int getItemCount() {
        return pictureUris.length;
    }
}
