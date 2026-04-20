package com.manager.assistant.ui.pages.bookkeeping.tag;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.databinding.ViewHolderTagGroupBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.ui.others.animators.RotateAnimator;
import com.manager.assistant.data.classes.TagGroup;

public class TagGroupAdapter extends RecyclerView.Adapter<TagGroupAdapter.TagGroupViewHolder> {
    private TagGroup tagGroup;                              //分组数据实例
    private final OnClickedListener listener;               //点击事件监听器

    public interface OnClickedListener {
        /**
         * 处理分组文本视图点击事件
         *
         * @param group 标签分组实例
         */
        void onGroupClicked(TagGroup group);
    }

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         */
        void onGroupClicked(int position);
    }

    public static class TagGroupViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagGroupBinding binding;
        boolean isExpanded = true;

        public TagGroupViewHolder(@NonNull ViewHolderTagGroupBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置展开和折叠视图的点击方法
            RotateAnimator rotateAnimator = new RotateAnimator(binding.expandFoldBtn, 0f, 180f);
            binding.expandFoldBtn.setOnClickListener(v -> {
                //修改标志位
                isExpanded = !isExpanded;

                //旋转分组名称文本右侧的图标
                rotateAnimator.toggle();

                //TODO:完成可见性切换功能
            });

            //设置根视图点击方法
            binding.getRoot().setOnClickListener(v -> listener.onGroupClicked(getBindingAdapterPosition()));
        }
    }

    /**
     * 标签管理额界面RecyclerView的适配器构造方法
     *
     * @param tagGroup 标签分组实例
     * @param listener 标签分组名称点击监听
     */
    public TagGroupAdapter(TagGroup tagGroup, OnClickedListener listener) {
        this.tagGroup = tagGroup;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderTagGroupBinding binding = ViewHolderTagGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new TagGroupViewHolder(
                binding,
                position -> listener.onGroupClicked(tagGroup)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TagGroupViewHolder holder, int position) {
        String groupName = tagGroup.getGroupName();
        holder.binding.groupNameText.setText(groupName);

        if (holder.isExpanded) {
            AppearanceAnimationHelper.setRadius(
                    holder.itemView.getContext(),
                    holder.itemView,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                    AppearanceAnimationHelper.SMALL_CARD_RADIUS,
                    AppearanceAnimationHelper.SMALL_CARD_RADIUS
            );
        } else {
            AppearanceAnimationHelper.setRadius(
                    holder.itemView.getContext(),
                    holder.itemView,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS,
                    AppearanceAnimationHelper.MEDIUM_CARD_RADIUS
            );
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    /**
     * 分组修改回调
     *
     * @param group 修改后的标签分组实例
     */
    public void onGroupModified(TagGroup group) {
        this.tagGroup = group;
        notifyItemChanged(0);
    }
}
