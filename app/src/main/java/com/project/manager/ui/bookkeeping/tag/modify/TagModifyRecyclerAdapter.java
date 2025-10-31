package com.project.manager.ui.bookkeeping.tag.modify;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.TagGroup;

import java.util.List;

public class TagModifyRecyclerAdapter extends RecyclerView.Adapter<TagModifyRecyclerAdapter.TagModifyViewHolder> {
    OnTagGroupClickedListener tagGroupClickedListener;
    List<TagGroup> tagGroupList;
    Context context;

    public interface OnTagGroupClickedListener {
        /**
         * 标签分组点击方法
         *
         * @param position 点击的标签分组的下标
         */
        void onTagGroupClicked(int position);
    }

    public class TagModifyViewHolder extends RecyclerView.ViewHolder {
        boolean isExpanded = true;              //标记是否展开
        MaterialTextView group_name_text;       //分组名称文本视图
        LinearLayout sub_view_layout;           //子组件的线性布局管理器

        public TagModifyViewHolder(@NonNull View itemView) {
            super(itemView);
            group_name_text = itemView.findViewById(R.id.tag_group_name_view);
            sub_view_layout = itemView.findViewById(R.id.sub_view_layout);

            // 启用过渡动画
            //TODO:解决动画不生效的BUG
            TransitionManager.beginDelayedTransition((ViewGroup) sub_view_layout.getParent(),
                    TransitionInflater.from(context).inflateTransition(R.transition.fade_slide));

            //设置点击方法
            group_name_text.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (tagGroupClickedListener != null && currentPosition != RecyclerView.NO_POSITION) {

                    //切换子组件布局的可见性
                    if (isExpanded) {
                        sub_view_layout.setVisibility(View.GONE);
                    } else {
                        sub_view_layout.setVisibility(View.VISIBLE);
                    }

                    rotateDrawableIcon(group_name_text, isExpanded);    //旋转分组名称文本右侧的图标
                    this.isExpanded = !isExpanded;
                }
            });
        }
    }

    public TagModifyRecyclerAdapter(List<TagGroup> tagGroupList, OnTagGroupClickedListener listener, Context context) {
        this.tagGroupList = tagGroupList;
        this.tagGroupClickedListener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public TagModifyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_modify_group_in_modify_activity, parent, false);
        return new TagModifyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagModifyViewHolder holder, int position) {
        //设置分组名称文本
        TagGroup currentGroup = this.tagGroupList.get(position);
        String group_name = currentGroup.getGroupName();
        holder.group_name_text.setText(group_name);

        holder.sub_view_layout.removeAllViews();
        for (Tag oneTag : currentGroup.getTags()) {
            String tag_name = oneTag.getName();

            MaterialTextView tag_text_view = new MaterialTextView(context);

            tag_text_view.setText(tag_name);
            holder.sub_view_layout.addView(tag_text_view);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }

    /**
     * 旋转分组右侧图标
     *
     * @param textView   需要旋转图标的文本视图
     * @param isExpanded 原先是否为展开状态
     */
    private void rotateDrawableIcon(MaterialTextView textView, boolean isExpanded) {
        RotateDrawable rotateDrawable = new RotateDrawable();
        Drawable originalDrawable = ContextCompat.getDrawable(context, R.drawable.baseline_keyboard_arrow_up_24);
        rotateDrawable.setDrawable(originalDrawable);
        rotateDrawable.setBounds(0, 0,
                rotateDrawable.getIntrinsicWidth(),
                rotateDrawable.getIntrinsicHeight());

        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null, rotateDrawable, null
        );

        //使用 ObjectAnimator 动画旋转
        ObjectAnimator animator;
        if (!isExpanded) {
            //不是展开状态，则将旋转了180°的图标旋转至360°
            animator = ObjectAnimator.ofInt(
                    rotateDrawable,
                    "level",
                    5000,
                    10000
            );
        } else {
            animator = ObjectAnimator.ofInt(
                    rotateDrawable,
                    "level",
                    0,
                    5000
            );
        }

        animator.setDuration(250);
        animator.setInterpolator(new LinearInterpolator()); //匀速
        animator.start();
    }
}
