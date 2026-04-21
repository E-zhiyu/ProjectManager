package com.manager.assistant.ui.pages.tag.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.databinding.ViewHolderTagGroupBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;
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

        /**
         * 标签分组展开状态变化回调
         *
         * @param groupNo    分组编号
         * @param isExpanded 是否展开
         */
        void onExpandStatueChanged(long groupNo, boolean isExpanded);
    }

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         */
        void onGroupClicked();

        /**
         * 标签分组展开状态变化回调
         */
        void onExpandStatueChanged(boolean isExpanded);
    }

    public static class TagGroupViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagGroupBinding binding;
        boolean isExpanded = true;
        private static final int KEY_RADIUS_ANIMATOR = 0x7F0B0002;  //动画执行器的Tag
        private static final int ANIMATION_DURATION = 200;          //圆角动画持续时间（ms）

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

                //修改圆角
                updateRadius(isExpanded);

                //触发监听器
                listener.onExpandStatueChanged(isExpanded);
            });

            //设置根视图点击方法
            binding.getRoot().setOnClickListener(v -> listener.onGroupClicked());
        }

        /**
         * 更新圆角（带动画）
         *
         * @param isExpanded 是否展开
         */
        private void updateRadius(boolean isExpanded) {
            ValueAnimator radiusAnimator = (ValueAnimator) binding.getRoot().getTag(KEY_RADIUS_ANIMATOR);
            if (radiusAnimator == null) {
                radiusAnimator = ValueAnimator.ofFloat(0f, 1f);
                radiusAnimator.setDuration(ANIMATION_DURATION);
                radiusAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                //通过Tag保存引用
                binding.getRoot().setTag(KEY_RADIUS_ANIMATOR, radiusAnimator);

                //设置取消和结束监听器
                radiusAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.getRoot().setTag(KEY_RADIUS_ANIMATOR, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        binding.getRoot().setTag(KEY_RADIUS_ANIMATOR, null);
                    }
                });

                //设置过程监听器
                radiusAnimator.addUpdateListener(animation -> {
                    //获取动画进度
                    float process = animation.getAnimatedFraction();

                    //计算当前的圆角值
                    int small = AppearanceAnimationHelper.SMALL_CARD_RADIUS;
                    int medium = AppearanceAnimationHelper.MEDIUM_CARD_RADIUS;
                    float currentDp = isExpanded ?
                            medium - (medium - small) * process :
                            small + (medium - small) * process;

                    //转换为像素值
                    float currentPx = ViewEdgeHelper.dpToPx(binding.getRoot().getContext(), currentDp);

                    //应用计算得到的圆角值
                    binding.getRoot().setShapeAppearanceModel(
                            binding.getRoot().getShapeAppearanceModel()
                                    .toBuilder()
                                    .setBottomLeftCornerSize(currentPx)
                                    .setBottomRightCornerSize(currentPx)
                                    .build()
                    );
                });

                radiusAnimator.start();
            } else {
                if (radiusAnimator.isRunning()) {
                    radiusAnimator.reverse();
                }
            }
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
                new ViewHolderListener() {
                    @Override
                    public void onGroupClicked() {
                        listener.onGroupClicked(tagGroup);
                    }

                    @Override
                    public void onExpandStatueChanged(boolean isExpanded) {
                        listener.onExpandStatueChanged(tagGroup.getGroupNo(), isExpanded);
                    }
                }
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
