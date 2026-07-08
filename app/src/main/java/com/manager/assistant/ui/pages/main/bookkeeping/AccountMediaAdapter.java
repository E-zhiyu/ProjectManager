package com.manager.assistant.ui.pages.main.bookkeeping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.manager.assistant.R;
import com.manager.assistant.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.manager.assistant.auxiliary.interfaces.adapter.ViewHolderListener;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.databinding.ViewHolderMediaBinding;

public class AccountMediaAdapter extends ListAdapter<MediaEntity, AccountMediaAdapter.MediaViewHolder> {
    private final static DiffUtil.ItemCallback<MediaEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull MediaEntity oldItem, @NonNull MediaEntity newItem) {
            return oldItem.getMediaId() == newItem.getMediaId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaEntity oldItem, @NonNull MediaEntity newItem) {
            return oldItem.getFileUri().equals(newItem.getFileUri());
        }
    };
    private SelectionTracker<Long> selectionTracker;    // ViewHolder 选择追踪器
    private final RequestOptions glideOptions;          //初始化Glide设置
    private boolean isSelectMode = false;               //是否是选择模式
    private final AdapterOnClickListener<MediaEntity> clickListener;

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMediaBinding binding;
        private final SpringAnimation scaleXAnim;           //X轴缩放动画
        private final SpringAnimation scaleYAnim;           //Y轴缩放动画
        private static final float PRESSED_SCALE = 0.9f;    //按下时缩放程度
        private MediaEntity media;                          //媒体实例

        public MediaViewHolder(@NonNull ViewHolderMediaBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置缩放动画
            scaleXAnim = new SpringAnimation(binding.imageCard, SpringAnimation.SCALE_X);
            scaleYAnim = new SpringAnimation(binding.imageCard, SpringAnimation.SCALE_Y);
            initScaleAnimation();

            //点击监听
            binding.getRoot().setOnClickListener(view ->
                    listener.onClick(getBindingAdapterPosition(), binding.getRoot())
            );
        }

        /**
         * 绑定媒体实例
         *
         * @param media 媒体实例
         */
        public void bindMedia(MediaEntity media) {
            this.media = media;
        }

        /**
         * 初始化缩放动画
         */
        private void initScaleAnimation() {
            SpringForce forceX = new SpringForce(1f);
            SpringForce forceY = new SpringForce(1f);

            forceX.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
            forceY.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

            forceX.setStiffness(SpringForce.STIFFNESS_LOW);
            forceY.setStiffness(SpringForce.STIFFNESS_LOW);

            scaleXAnim.setSpring(forceX);
            scaleYAnim.setSpring(forceY);
        }

        /**
         * 切换选择
         *
         * @param isChecked 是否选中
         */
        public void setChecked(boolean isChecked) {
            if (isChecked) {
                scaleXAnim.animateToFinalPosition(PRESSED_SCALE);
                scaleYAnim.animateToFinalPosition(PRESSED_SCALE);
            } else {
                scaleXAnim.animateToFinalPosition(1f);
                scaleYAnim.animateToFinalPosition(1f);
            }
            binding.checkedText.setChecked(isChecked);
        }

        /**
         * 为 Selection 库提供信息
         *
         * @return Selection 库的 Item 信息
         */
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<>() {
                @Override
                public int getPosition() {
                    return getBindingAdapterPosition();
                }

                @Nullable
                @Override
                public Long getSelectionKey() {
                    int pos = getBindingAdapterPosition();

                    if (getBindingAdapter() instanceof AccountMediaAdapter) {
                        // 必须严格过滤 NO_POSITION
                        return (pos != RecyclerView.NO_POSITION && pos < getBindingAdapter().getItemCount()) ?
                                media.getItemId() :
                                null;
                    } else {
                        return null;
                    }
                }
            };
        }
    }

    /**
     * @param imageSize     预览图大小
     * @param clickListener 预览图点击监听
     */
    public AccountMediaAdapter(int imageSize, AdapterOnClickListener<MediaEntity> clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;

        glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.outline_error_24)             //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //缓存策略
                .override(imageSize, imageSize);                          //图片尺寸
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    /**
     * 更新选择模式
     *
     * @param selectMode 是否为选择模式
     */
    public void setSelectMode(boolean selectMode) {
        if (isSelectMode == selectMode) return;

        isSelectMode = selectMode;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getItemId();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderMediaBinding binding = ViewHolderMediaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MediaViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int pos, View anchor) {
                        MediaEntity mediaEntity = getItem(pos);
                        clickListener.onClick(mediaEntity, anchor);
                    }

                    @Override
                    public void onLongClick(int pos, View anchor) {
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaEntity media = getItem(position);

        //设置选择状态
        boolean isChecked = selectionTracker.isSelected(media.getItemId());
        holder.setChecked(isChecked);

        //通过 Glide 显示图片
        holder.bindMedia(media);
        Glide.with(holder.itemView.getContext())
                .load(media.getFileUri())
                .apply(glideOptions)
                .into(holder.binding.imageView);

        //设置复选框是否显示
        if (isSelectMode) {
            holder.binding.checkedText.setVisibility(View.VISIBLE);
        } else {
            holder.binding.checkedText.setVisibility(View.GONE);
        }
    }

    /**
     * 通过{@link Long}类型的 ID 查找 Item
     *
     * @param id 通过{@link #getItemId(int)}返回的 ID
     * @return 若找到则返回 Item 实例，否则返回 null
     */
    @Nullable
    public MediaEntity getItemById(long id) {
        for (MediaEntity media : getCurrentList()) {
            if (media.getItemId() == id) {
                return media;
            }
        }
        return null;
    }
}
