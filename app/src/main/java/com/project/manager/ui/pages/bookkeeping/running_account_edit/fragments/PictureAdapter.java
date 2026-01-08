package com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.project.manager.R;
import com.project.manager.data.data_class.Picture;
import com.project.manager.helpers.ExceptionHelper;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureViewHolder> {
    private final Context context;            //上下文
    private final List<Picture> pictureList;  //数据源列表
    private final RequestOptions glideOptions = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.baseline_photo_24)      //占位图
            .error(R.drawable.baseline_error_outline_24)    //错误图
            .diskCacheStrategy(DiskCacheStrategy.ALL)       //缓存策略
            .override(300, 300);               //图片尺寸

    public static class PictureViewHolder extends RecyclerView.ViewHolder {
        String fileUriStr;
        ImageView imageView;

        public PictureViewHolder(@NonNull ImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }

        /**
         * 通过Uri设置图片资源
         *
         * @param context      上下文
         * @param uri          图片Uri
         * @param glideOptions glide的设置项
         */
        public void setPictureRes(Context context, @NonNull Uri uri, RequestOptions glideOptions) {
            fileUriStr = uri.toString();
            Glide.with(context)
                    .load(uri)
                    .apply(glideOptions)
                    .into(imageView);
        }
    }

    public PictureAdapter(Context context, List<Picture> pictureList) {
        this.context = context;
        this.pictureList = pictureList;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);

        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return new PictureViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        Uri pictureUri = picture.getPictureUri();
        if (pictureUri != null) {
            holder.setPictureRes(context, pictureUri, glideOptions);
        }

        //TODO:设置点击监听和长按监听
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public void addPicture(Picture picture) {
        //保存至数据库
        try {
            long pno = Picture.addPicture(context, picture);
            picture.setPno(pno);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //刷新UI
        pictureList.add(picture);
        notifyItemInserted(pictureList.size() - 1);
    }
}
