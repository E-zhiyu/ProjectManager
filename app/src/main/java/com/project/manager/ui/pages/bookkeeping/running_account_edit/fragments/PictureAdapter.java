package com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.project.manager.R;
import com.project.manager.data.data_class.Picture;
import com.project.manager.ui.pages.bookkeeping.KeyValueStrings;
import com.project.manager.ui.picture.FullScreenImageActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureViewHolder> {
    private final Context context;                  //上下文
    private final List<Picture> pictureList;        //数据源列表
    private final List<Boolean> pictureSelectList;  //记录图片选择状态的列表
    private boolean isDeleteMode = false;           //标记是否为删除图片模式
    private final RequestOptions glideOptions = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.baseline_photo_24)      //占位图
            .error(R.drawable.baseline_error_outline_24)    //错误图
            .diskCacheStrategy(DiskCacheStrategy.NONE)      //缓存策略(不缓存)
            .override(300, 300);               //图片尺寸

    public static class PictureViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;                //图像容器视图
        CheckedTextView checkedTextView;    //右上角复选框

        public PictureViewHolder(@NonNull View view) {
            super(view);
            this.imageView = view.findViewById(R.id.image_view);
            this.checkedTextView = view.findViewById(R.id.checked_text);
        }

        /**
         * 通过Uri设置图片资源
         *
         * @param context      上下文
         * @param uri          图片Uri
         * @param glideOptions glide的设置项
         */
        public void setPictureRes(Context context, @NonNull Uri uri, RequestOptions glideOptions) {
            Glide.with(context)
                    .load(uri)
                    .apply(glideOptions)
                    .into(imageView);
        }
    }

    /**
     * 图片适配器构造方法
     *
     * @param context     上下文
     * @param pictureList 图片列表
     */
    public PictureAdapter(Context context, @NonNull List<Picture> pictureList) {
        this.context = context;
        this.pictureList = pictureList;
        pictureSelectList = new ArrayList<>(Collections.nCopies(pictureList.size(), false));    //默认未选择
    }

    /**
     * 获取是否为图片删除模式的方法
     *
     * @return 是否为图片删除模式
     */
    public boolean isDeleteMode() {
        return isDeleteMode;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.view_holder_picture, parent, false);
        return new PictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        Uri pictureUri = picture.getPictureUri();
        if (pictureUri != null) {
            holder.setPictureRes(context, pictureUri, glideOptions);

            //设置复选框属性
            if (isDeleteMode) {
                holder.checkedTextView.setVisibility(View.VISIBLE);
            } else {
                holder.checkedTextView.setVisibility(View.GONE);
            }
            boolean isChecked = pictureSelectList.get(holder.getBindingAdapterPosition());
            holder.checkedTextView.setChecked(isChecked);

            //视图点击监听器
            holder.imageView.setOnClickListener(v -> {
                if (!isDeleteMode) {
                    Intent skip2ImageActivity = new Intent(context, FullScreenImageActivity.class);
                    skip2ImageActivity.putExtra(KeyValueStrings.FILE_URI.getValue(), pictureUri.toString());
                    context.startActivity(skip2ImageActivity);
                } else {
                    holder.checkedTextView.setVisibility(View.VISIBLE);
                    holder.checkedTextView.toggle();

                    //同步图片选择状态数据
                    pictureSelectList.set(holder.getBindingAdapterPosition(), holder.checkedTextView.isChecked());
                }
            });

            //视图长按监听器
            holder.imageView.setOnLongClickListener(v -> {
                if (!isDeleteMode) {
                    pictureSelectList.set(holder.getBindingAdapterPosition(), true);
                    switchDeleteMode(true);

                    Toast.makeText(context, "返回以退出图片删除模式", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    /**
     * 切换图片删除模式
     *
     * @param isDeleteMode 切换后是否为删除模式
     */
    @SuppressLint("NotifyDataSetChanged")
    public void switchDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
        notifyDataSetChanged();
    }

    /**
     * 将新相片添加至界面中
     *
     * @param picture 新相片数据实例
     */
    public void addPicture(Picture picture) {
        pictureList.add(picture);
        notifyItemInserted(pictureList.size() - 1);
    }
}
