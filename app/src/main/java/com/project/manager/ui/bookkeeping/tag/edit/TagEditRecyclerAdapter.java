package com.project.manager.ui.bookkeeping.tag.edit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.ManagerAssistant;
import com.project.manager.R;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.view_model.AccountTagModifyID;
import com.project.manager.ui.view_model.AccountTagViewModel;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.TagGroup;
import com.project.manager.ui.view_model.TagWithModifyID;

import java.util.ArrayList;
import java.util.List;

public class TagEditRecyclerAdapter extends RecyclerView.Adapter<TagEditRecyclerAdapter.TagEditViewHolder> {
    List<TagGroup> tagGroupList;                        //标签组列表
    Context context;                                    //上下文
    OnTextViewClickedListener textClickedListener;    //标签文本点击事件监听器

    public interface OnTextViewClickedListener {
        /**
         * 处理标签文本视图点击事件
         *
         * @param tag_no     标签编号
         * @param tag_name   标签名称
         * @param group_no   标签分组编号
         * @param group_name 标签分组名称
         */
        void onTagTextViewClicked(long tag_no, String tag_name, long group_no, String group_name);

        /**
         * 处理分组文本视图点击事件
         *
         * @param group_no   点击的分组编号
         * @param group_name 点击的分组名称
         */
        void onGroupTextViewClicked(long group_no, String group_name);
    }

    public List<TagGroup> getTagGroupList() {
        return tagGroupList;
    }

    public class TagEditViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView group_name_text;       //分组名称文本视图
        ImageView expand_fold_view;             //控制卡片展开和折叠的按钮
        LinearLayout sub_view_layout;           //子组件的线性布局管理器
        int expandedHeight;                     //子布局展开时的高度
        boolean isAnimating = false;            //标记是否在动画进行中

        public TagEditViewHolder(@NonNull View itemView) {
            super(itemView);
            group_name_text = itemView.findViewById(R.id.tag_group_name_view);
            sub_view_layout = itemView.findViewById(R.id.sub_view_layout);
            expand_fold_view = itemView.findViewById(R.id.expand_fold_btn);

            //设置展开和折叠视图的点击方法
            expand_fold_view.setOnClickListener(v -> {
                //旋转分组名称文本右侧的图标
                rotateIcon(expand_fold_view, sub_view_layout.getVisibility() == View.VISIBLE);

                //切换子组件布局的可见性
                if (sub_view_layout.getVisibility() == View.VISIBLE) {
                    if (!isAnimating) {
                        expandedHeight = sub_view_layout.getMeasuredHeight();   //折叠之前保存展开时的高度
                    }

                    isAnimating = true;
                    animateHeight(sub_view_layout, expandedHeight, 0, () -> {
                        isAnimating = false;
                        sub_view_layout.setVisibility(View.GONE);
                    });
                } else {
                    sub_view_layout.setVisibility(View.VISIBLE);
                    isAnimating = true;
                    animateHeight(sub_view_layout, 0, expandedHeight, () -> isAnimating = false);
                }
            });
        }
    }

    private void animateHeight(final View view, int start, int end, Runnable onEnd) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener((animation) -> {
            int height = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        });

        //设置动画结束后执行的代码
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onEnd != null) onEnd.run();
            }
        });

        animator.setDuration(200);
        animator.start();
    }

    public TagEditRecyclerAdapter(List<TagGroup> tagGroupList, Context context, OnTextViewClickedListener listener) {
        this.tagGroupList = tagGroupList;
        this.context = context;
        this.textClickedListener = listener;
    }

    @NonNull
    @Override
    public TagEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_edit_group_in_edit_activity, parent, false);
        return new TagEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagEditViewHolder holder, int position) {
        //设置分组名称文本
        TagGroup currentGroup = this.tagGroupList.get(position);
        long group_no = currentGroup.getGroup_no();
        String group_name = currentGroup.getGroup_name();
        holder.group_name_text.setText(group_name);

        //设置分组名称文本视图点击监听器
        holder.group_name_text.setOnClickListener(v ->
                textClickedListener.onGroupTextViewClicked(group_no, group_name)
        );

        //添加标签文本视图
        holder.sub_view_layout.removeAllViews();    //先删除旧视图
        for (Tag oneTag : currentGroup.getTags()) {
            String tag_name = oneTag.getName();
            long tag_no = oneTag.getTno();

            //设置文本属性
            MaterialTextView tag_text_view = new MaterialTextView(context);
            tag_text_view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tag_text_view.setPadding(50, 20, 25, 20);
            tag_text_view.setTextAppearance(R.style.CommonTextAppearance);

            //添加点击的波纹效果
            try (TypedArray typedArray = context.obtainStyledAttributes(
                    new int[]{android.R.attr.selectableItemBackground})) {
                int backgroundResource = typedArray.getResourceId(0, 0);
                tag_text_view.setBackgroundResource(backgroundResource);
            }
            tag_text_view.setFocusable(true);
            tag_text_view.setClickable(true);

            //添加右侧箭头图标
            Drawable right_arrow = AppCompatResources.getDrawable(context, R.drawable.baseline_keyboard_arrow_right_24);
            tag_text_view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, right_arrow, null
            );

            //设置标签文本点击监听器
            tag_text_view.setOnClickListener(v ->
                    textClickedListener.onTagTextViewClicked(tag_no, tag_name, group_no, group_name)
            );

            tag_text_view.setText(tag_name);
            holder.sub_view_layout.addView(tag_text_view);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }

    /**
     * 旋转视图的图标
     *
     * @param expand_fold_view 需要旋转图标的视图
     * @param isExpanded       原先是否为展开状态
     */
    private void rotateIcon(ImageView expand_fold_view, boolean isExpanded) {
        //使用 ObjectAnimator 动画旋转
        ObjectAnimator animator;
        if (!isExpanded) {
            //不是展开状态，则将旋转了180°的图标旋转至360°
            animator = ObjectAnimator.ofFloat(
                    expand_fold_view,
                    "rotation",
                    180f,
                    360f
            );
        } else {
            animator = ObjectAnimator.ofFloat(
                    expand_fold_view,
                    "rotation",
                    0f,
                    180f
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

    /**
     * 编辑标签（未更换分组）
     *
     * @param new_tag_name 新标签名称
     * @param tag_no       标签编号
     * @param group_no     该标签所属的分组编号
     */
    public void modifyTag(String new_tag_name, long tag_no, long group_no) {
        //将数据保存至数据库
        try {
            Tag.modifyTag(new_tag_name, tag_no, context);
            Toast.makeText(context, "标签修改成功", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //找到视图中对应的分组并更改
        int group_index = 0;
        for (TagGroup group : this.tagGroupList) {
            if (group.getGroup_no() == group_no) {
                for (Tag tag : group.getTags()) {
                    if (tag.getTno() == tag_no) {
                        tag.setName(new_tag_name);
                        break;
                    }
                }
                break;
            }
            group_index++;
        }
        notifyItemChanged(group_index);
    }

    /**
     * 编辑标签（更换分组）
     *
     * @param new_tag_name    新标签名称
     * @param tag_no          标签编号
     * @param new_group_name  新标签分组名称
     * @param origin_group_no 原标签分组编号
     * @param new_group_no    新标签分组编号
     */
    public void modifyTag(String new_tag_name, long tag_no, String new_group_name, long origin_group_no, long new_group_no) {
        //将数据保存至数据库
        try {
            Tag.modifyTag(new_tag_name, tag_no, new_group_no, context);
            Toast.makeText(context, "标签修改成功", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //判断是否需要新建标签分组
        if (new_group_no == 0) {
            try {
                new_group_no = TagGroup.saveNewGroup(new_group_name, context);
            } catch (SQLiteException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                Toast.makeText(context, "标签修改失败", Toast.LENGTH_SHORT).show();
                return;
            }

            TagGroup newGroup = new TagGroup(new_group_name, new_group_no);
            newGroup.addTag(new Tag(new_tag_name, tag_no));
            this.tagGroupList.add(newGroup);

            notifyItemInserted(this.tagGroupList.size() - 1);   //更新列表视图
        } else {
            int new_group_index = 0;    //待新增标签的分组下标
            for (TagGroup group : this.tagGroupList) {
                if (group.getGroup_no() == new_group_no) {
                    Tag new_tag = new Tag(new_tag_name, tag_no);
                    group.addTag(new_tag);
                    break;
                }
                new_group_index++;
            }

            notifyItemChanged(new_group_index);
        }

        //删除原分组中对应的标签
        int origin_group_index = 0;
        for (TagGroup group : this.tagGroupList) {
            if (group.getGroup_no() == origin_group_no) {
                int old_tag_index = 0;
                for (Tag old_tag : group.getTags()) {
                    if (old_tag.getTno() == tag_no) {
                        group.removeTag(old_tag_index);
                        break;
                    }
                    old_tag_index++;
                }
                break;
            }
            origin_group_index++;
        }

        notifyItemChanged(origin_group_index);
    }

    /**
     * 删除标签
     *
     * @param tag_no   待删除标签的编号
     * @param group_no 待删除标签所属分组的编号
     */
    public void deleteTag(long tag_no, long group_no) {
        int group_index = 0;        //待删除标签所属分组的下标
        for (TagGroup group : this.tagGroupList) {
            if (group.getGroup_no() == group_no) {
                int tag_index = 0;  //待删除标签的编号
                for (Tag tag : group.getTags()) {
                    if (tag.getTno() == tag_no) {
                        group.removeTag(tag_index);
                        break;
                    }
                    tag_index++;
                }
                break;
            }
            group_index++;
        }

        //将数据保存至数据库
        try {
            Tag.deleteTag(tag_no, context);
            Toast.makeText(context, "标签删除成功", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        notifyItemChanged(group_index);
    }

    /**
     * 修改分组
     *
     * @param group_no       分组编号
     * @param new_group_name 分组名称
     */
    public void modifyGroup(long group_no, String new_group_name) {
        //将数据保存至数据库
        try {
            TagGroup.modifyGroupName(group_no, new_group_name, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //修改界面中的分组
        int group_index = 0;
        for (TagGroup group : this.tagGroupList) {
            if (group.getGroup_no() == group_no) {
                group.setGroup_name(new_group_name);
                break;
            }
            group_index++;
        }

        notifyItemChanged(group_index);
    }

    /**
     * 删除分组
     *
     * @param group_no 分组编号
     */
    public void deleteGroup(long group_no) {
        //删除界面中的分组
        int group_index = 0;
        List<Tag> tagsToBeDeleted = new ArrayList<>();  //待删除的标签的列表
        for (TagGroup group : this.tagGroupList) {
            if (group.getGroup_no() == group_no) {
                tagsToBeDeleted = group.getTags();
                break;
            }
            group_index++;
        }

        //从数据库中删除标签和分组
        try {
            Tag.deleteTag(tagsToBeDeleted, context);    //删除标签
            TagGroup.deleteGroup(group_no, context);    //删除分组

            Toast.makeText(context, "标签分组已删除", Toast.LENGTH_SHORT).show();

            //获取ViewModel通知流水账数据输入界面更新UI
            ManagerAssistant app = (ManagerAssistant) ((EditActivity) context).getApplication();
            AccountTagViewModel viewModel = app.getAccountTagViewModel();

            List<TagWithModifyID> tagWithModifyIDList = new ArrayList<>();
            for (Tag tag : tagsToBeDeleted) {
                String tag_name = tag.getName();
                long tag_no = tag.getTno();
                TagWithModifyID oneTagWithModifyID = new TagWithModifyID(tag_name, tag_no, AccountTagModifyID.DELETE);
                tagWithModifyIDList.add(oneTagWithModifyID);
            }
            viewModel.updateTag(tagWithModifyIDList);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        tagGroupList.remove(group_index);
        notifyItemRemoved(group_index);
    }

    /**
     * 合并标签分组
     *
     * @param old_group_no    被合并的分组编号
     * @param merge_target_no 合并到的分组的编号
     */
    public void mergeGroup(long old_group_no, long merge_target_no) {
        try {
            TagGroup.mergeGroup(old_group_no, merge_target_no, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        List<Tag> tags_in_old_group = null, tags_in_target_group = null;
        int old_group_index = -1, target_group_index = -1;
        int index = 0;
        for (TagGroup group : tagGroupList) {
            if (group.getGroup_no() == old_group_no) {
                tags_in_old_group = group.getTags();
                old_group_index = index;
            } else if (group.getGroup_no() == merge_target_no) {
                tags_in_target_group = group.getTags();
                target_group_index = index;
            }
            index++;
        }

        if (tags_in_old_group != null && tags_in_target_group != null) {
            tags_in_target_group.addAll(tags_in_old_group);
            tagGroupList.remove(old_group_index);

            //刷新UI
            notifyItemRemoved(old_group_index);
            notifyItemChanged(target_group_index);
        }
    }

    /**
     * 合并标签
     *
     * @param merged_tag_no       被合并的标签编号
     * @param merge_target_tag_no 合并到的目标标签编号
     * @param group_no            被合并标签的分组编号
     */
    public void mergeTag(long merged_tag_no, long merge_target_tag_no, long group_no) {
        try {
            Tag.mergeTag(merged_tag_no, merge_target_tag_no, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        int group_index = 0;
        int merged_tag_group_index = -1;    //被合并的标签所在分组的下标
        for (TagGroup group : tagGroupList) {
            if (group.getGroup_no() == group_no) {
                merged_tag_group_index = group_index;
                int tag_index = 0;
                List<Tag> tagList = group.getTags();
                for (Tag tag : tagList) {
                    if (tag.getTno() == merged_tag_no) {
                        tagList.remove(tag_index);
                        break;
                    }
                    tag_index++;
                }
                break;
            }
            group_index++;
        }
        if (merged_tag_group_index != -1) { //更新UI
            notifyItemChanged(merged_tag_group_index);
        }
    }
}
