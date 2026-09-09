package com.sly.coffer.ui.pages.main.settings.sub.media;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sly.coffer.R;
import com.sly.coffer.auxiliary.classes.MediaFileInfo;
import com.sly.coffer.auxiliary.interfaces.adapter.ViewHolderListener;
import com.sly.coffer.databinding.ViewHolderMediaListBinding;
import com.sly.coffer.helpers.TextHelper;
import com.sly.coffer.helpers.appearence.AppearanceHelper;

import java.util.List;
import java.util.stream.Collectors;

public class MediaListAdapter extends ListAdapter<MediaFileInfo, MediaListAdapter.ItemViewHolder> {
    private static final DiffUtil.ItemCallback<MediaFileInfo> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaFileInfo oldItem, @NonNull MediaFileInfo newItem) {
            return oldItem.getUri().hashCode() == newItem.getUri().hashCode();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaFileInfo oldItem, @NonNull MediaFileInfo newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getSize() == newItem.getSize();
        }
    };
    private final OnClickListener clickListener;
    private final RequestOptions glideOptions;          //图片显示设置

    public interface OnClickListener {
        /**
         * 点击回调
         *
         * @param pos     被点击的元素在列表中的位置
         * @param uriList 当前的媒体文件 Uri 列表
         * @param view    被点击的视图
         */
        void onClick(int pos, List<Uri> uriList, View view);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMediaListBinding binding;

        public ItemViewHolder(@NonNull ViewHolderMediaListBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //绑定点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));
        }
    }

    public MediaListAdapter(Context context, OnClickListener clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;

        //初始化Glide设置
        glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.outline_error_24)             //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //缓存策略
                .override(
                        AppearanceHelper.dpToPx(context, 100),
                        AppearanceHelper.dpToPx(context, 100)
                );                                              //图片尺寸
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderMediaListBinding binding = ViewHolderMediaListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ItemViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int pos, View anchor) {
                        List<Uri> uriList = getCurrentList().stream()
                                .map(MediaFileInfo::getUri)
                                .collect(Collectors.toList());
                        clickListener.onClick(pos, uriList, anchor);
                    }

                    @Override
                    public void onLongClick(int pos, View anchor) {
                    }

                    @Override
                    public void onCheckedChange(int pos, boolean finalStat, View anchor) {

                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        MediaFileInfo info = getItem(position);

        //通过 Glide 显示图片
        Glide.with(holder.itemView.getContext())
                .load(info.getUri())
                .apply(glideOptions)
                .into(holder.binding.imageView);

        //文件大小
        holder.binding.sizeText.setText(TextHelper.shortenFileSize(info.getSize()));
    }
}
