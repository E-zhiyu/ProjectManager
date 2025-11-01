package com.project.manager.ui.bookkeeping.tag.edit;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.TagGroup;

import java.util.List;

public class TagEditRecyclerAdapter extends RecyclerView.Adapter<TagEditRecyclerAdapter.TagEditViewHolder> {
    List<TagGroup> tagGroupList;                        //标签组列表
    Context context;                                    //上下文
    OnTagTextViewClickedListener tagClickedListener;    //标签文本点击事件监听器

    public interface OnTagTextViewClickedListener {
        /**
         * 处理标签文本视图点击事件
         *
         * @param tag_no     标签编号
         * @param tag_name   标签名称
         * @param group_no   标签分组编号
         * @param group_name 标签分组名称
         */
        void onTagTextViewClicked(long tag_no, String tag_name, long group_no, String group_name);
    }

    public List<TagGroup> getTagGroupList() {
        return tagGroupList;
    }

    public class TagEditViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView group_name_text;       //分组名称文本视图
        LinearLayout sub_view_layout;           //子组件的线性布局管理器

        public TagEditViewHolder(@NonNull View itemView) {
            super(itemView);
            group_name_text = itemView.findViewById(R.id.tag_group_name_view);
            sub_view_layout = itemView.findViewById(R.id.sub_view_layout);

            //设置点击方法
            group_name_text.setOnClickListener(v -> {
                //旋转分组名称文本右侧的图标
                rotateDrawableIcon(group_name_text, sub_view_layout.getVisibility() == View.VISIBLE);

                //切换子组件布局的可见性
                if (sub_view_layout.getVisibility() == View.VISIBLE) {
                    //TODO:解决折叠动画不生效的BUG
                    sub_view_layout.setVisibility(View.GONE);
                    animateHeight(sub_view_layout, sub_view_layout.getMeasuredHeight(), 0);
                } else {
                    sub_view_layout.setVisibility(View.VISIBLE);
                    animateHeight(sub_view_layout, 0, sub_view_layout.getMeasuredHeight());
                }
            });
        }
    }

    private void animateHeight(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener((animation) -> {
            int height = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        });
        animator.setDuration(200);
        animator.start();
    }

    public TagEditRecyclerAdapter(List<TagGroup> tagGroupList, Context context, OnTagTextViewClickedListener listener) {
        this.tagGroupList = tagGroupList;
        this.context = context;
        this.tagClickedListener = listener;
    }

    @NonNull
    @Override
    public TagEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_modify_group_in_modify_activity, parent, false);
        return new TagEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagEditViewHolder holder, int position) {
        //设置分组名称文本
        TagGroup currentGroup = this.tagGroupList.get(position);
        long group_no = currentGroup.getGroup_no();
        String group_name = currentGroup.getGroupName();
        holder.group_name_text.setText(group_name);

        holder.sub_view_layout.removeAllViews();
        for (Tag oneTag : currentGroup.getTags()) {
            String tag_name = oneTag.getName();
            long tag_no = oneTag.getTno();

            MaterialTextView tag_text_view = new MaterialTextView(context);
            tag_text_view.setTextAppearance(R.style.CommonTextAppearance);

            //添加右侧箭头图标
            Drawable right_arrow = AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_arrow_right_24);
            tag_text_view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, right_arrow, null
            );

            //TODO:添加子组件的点击监听器
            tag_text_view.setOnClickListener((v) -> {
                tagClickedListener.onTagTextViewClicked(tag_no, tag_name, group_no, group_name);
            });

            tag_text_view.setText(tag_name);
            holder.sub_view_layout.addView(tag_text_view);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }

    /**
     * 旋转右侧图标
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

    /**
     * 添加新标签（不添加新分组）
     *
     * @param new_tag         新标签对象
     * @param target_group_no 已存在的分组编号
     */
    public void addNewTag(Tag new_tag, long target_group_no) {
        int position = 0;   //待刷新的分组下标
        for (TagGroup group : this.tagGroupList) {
            if (group.getGroup_no() == target_group_no) {
                group.addTag(new_tag);
                break;
            }
            position++;
        }

        notifyItemChanged(position);
    }

    /**
     * 添加新标签（同时添加新分组）
     *
     * @param new_tag   新标签对象
     * @param new_group 新分组对象
     */
    public void addNewTag(Tag new_tag, TagGroup new_group) {
        int list_size = this.tagGroupList.size();
        new_group.addTag(new_tag);
        this.tagGroupList.add(new_group);

        notifyItemInserted(list_size);
    }
}
