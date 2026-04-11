package com.manager.assistant.ui.pages.bookkeeping.tag;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.databinding.ViewHolderTagGroupBinding;
import com.manager.assistant.ui.others.animators.ExpandFoldAnimator;
import com.manager.assistant.ui.others.animators.RotateAnimator;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagManageRecyclerAdapter extends RecyclerView.Adapter<TagManageRecyclerAdapter.TagGroupViewHolder> {
    private final Map<TagGroup, List<Tag>> tagGroupMap;            //标签分组字典
    private final OnTextViewClickedListener textClickedListener;    //标签文本点击事件监听器

    public interface OnTextViewClickedListener {
        /**
         * 处理标签文本视图点击事件
         *
         * @param tagNo     标签编号
         * @param tagName   标签名称
         * @param tagScope  标签作用域
         * @param groupNo   标签分组编号
         * @param groupName 标签分组名称
         */
        void onTagTextViewClicked(long tagNo, String tagName, int tagScope, long groupNo, String groupName);

        /**
         * 处理分组文本视图点击事件
         *
         * @param groupNo   点击的分组编号
         * @param groupName 点击的分组名称
         */
        void onGroupTextViewClicked(long groupNo, String groupName);
    }

    public static class TagGroupViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagGroupBinding binding;
        boolean isExpanded = true;

        public TagGroupViewHolder(@NonNull ViewHolderTagGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 标签管理额界面RecyclerView的适配器构造方法
     *
     * @param tagGroupMap 标签分组Map
     * @param listener    标签分组名称点击监听
     */
    public TagManageRecyclerAdapter(Map<TagGroup, List<Tag>> tagGroupMap, OnTextViewClickedListener listener) {
        this.tagGroupMap = tagGroupMap;
        this.textClickedListener = listener;
    }

    @NonNull
    @Override
    public TagGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderTagGroupBinding binding = ViewHolderTagGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TagGroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagGroupViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        //获取数据
        List<Map.Entry<TagGroup, List<Tag>>> entryList = new ArrayList<>(tagGroupMap.entrySet());
        Map.Entry<TagGroup, List<Tag>> currentEntry = entryList.get(position);
        TagGroup currentGroup = currentEntry.getKey();
        long groupNo = currentGroup.getGroupNo();
        String groupName = currentGroup.getGroupName();

        //设置分组名称文本
        holder.binding.groupNameText.setText(groupName);

        //设置分组名称文本视图点击监听器
        holder.itemView.setOnClickListener(v ->
                textClickedListener.onGroupTextViewClicked(groupNo, groupName)
        );

        //设置展开和折叠视图的点击方法
        RotateAnimator rotateAnimator = new RotateAnimator(holder.binding.subViewLayout, 0f, 180f);
        holder.binding.subViewLayout.setOnClickListener(v -> {
            //修改标志位
            holder.isExpanded = !holder.isExpanded;

            //旋转分组名称文本右侧的图标
            rotateAnimator.toggle();

            //切换子组件布局的可见性
            if (holder.isExpanded) {
                ExpandFoldAnimator.expand(holder.binding.subViewLayout);
            } else {
                ExpandFoldAnimator.collapse(holder.binding.subViewLayout);
            }
        });

        //添加标签文本视图
        holder.binding.subViewLayout.removeAllViews();    //先删除旧视图
        List<Tag> tagList = currentEntry.getValue();
        for (Tag oneTag : tagList) {
            String tagName = oneTag.getName();
            long tagNo = oneTag.getTno();
            int tagScope = oneTag.getScope();

            //设置文本属性
            MaterialTextView tagTextView = new MaterialTextView(context);
            tagTextView.setMaxLines(1);
            tagTextView.setEllipsize(TextUtils.TruncateAt.END);
            tagTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tagTextView.setPadding(50, 20, 25, 20);

            //添加点击的波纹效果
            try (TypedArray typedArray = context.obtainStyledAttributes(
                    new int[]{android.R.attr.selectableItemBackground})) {
                int backgroundResource = typedArray.getResourceId(0, 0);
                tagTextView.setBackgroundResource(backgroundResource);
            }
            tagTextView.setFocusable(true);
            tagTextView.setClickable(true);

            //添加右侧箭头图标
            Drawable rightArrow = AppCompatResources.getDrawable(context, R.drawable.outline_keyboard_arrow_right_24);
            tagTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, rightArrow, null
            );

            //设置标签文本点击监听器
            tagTextView.setOnClickListener(v ->
                    textClickedListener.onTagTextViewClicked(tagNo, tagName, tagScope, groupNo, groupName)
            );

            tagTextView.setText(tagName);
            holder.binding.subViewLayout.addView(tagTextView);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupMap.size();
    }

    /**
     * 添加新标签
     *
     * @param newTag 新标签对象
     * @param group  新分组对象
     */
    public void addNewTag(Tag newTag, @NonNull TagGroup group) {
        //根据是否包含分组，执行不同的新建操作
        if (tagGroupMap.containsKey(group)) {
            //获取需要更新的视图的下标
            List<TagGroup> keyList = new ArrayList<>(tagGroupMap.keySet());
            TagGroup targetGroup = null;
            int position = 0;
            for (TagGroup tagGroup : keyList) {
                if (tagGroup.getGroupNo() == group.getGroupNo()) {
                    targetGroup = tagGroup;
                    break;
                }
                position++;
            }
            if (targetGroup == null) {
                return;
            }

            //向内存中写入新标签
            List<Tag> tagList = tagGroupMap.get(targetGroup);
            if (tagList == null) {
                return;
            }
            tagList.add(newTag);

            //更新视图
            notifyItemChanged(position);
        } else {
            List<Tag> tagList = new ArrayList<>();
            tagList.add(newTag);
            tagGroupMap.put(group, tagList);

            notifyItemInserted(tagGroupMap.size() - 1);
        }
    }

    /**
     * 编辑标签（更换分组）
     *
     * @param newTagName     新标签名称
     * @param tagNo          标签编号
     * @param tagScope       标签作用域
     * @param newGroupName   新标签分组名称
     * @param oldGroupNo     原标签分组编号
     * @param newGroupNo     新标签分组编号
     * @param currentGroupNo 当前正在显示的分组编号（-1表示显示所有）
     */
    public void modifyTag(
            String newTagName,
            long tagNo,
            int tagScope,
            String newGroupName,
            long oldGroupNo,
            long newGroupNo,
            long currentGroupNo
    ) {
        if (oldGroupNo != newGroupNo) {
            //删除原分组中的标签
            int oldGroupIndex = 0;
            for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
                TagGroup oldGroup = entry.getKey();
                if (oldGroup.getGroupNo() == oldGroupNo) {
                    List<Tag> oldTagList = entry.getValue();
                    for (int i = 0; i < oldTagList.size(); i++) {
                        Tag oldTag = oldTagList.get(i);
                        if (oldTag.getTno() == tagNo) {
                            oldTagList.remove(i);
                            break;
                        }
                    }
                    break;
                }
                oldGroupIndex++;
            }
            notifyItemChanged(oldGroupIndex);

            //如果新分组编号与当前分组编号相同，则添加新分组
            if (newGroupNo == currentGroupNo && currentGroupNo != -1) {
                TagGroup newGroup = new TagGroup(newGroupName, newGroupNo);
                List<Tag> newTagList = new ArrayList<>();
                newTagList.add(new Tag(newTagName, tagNo, tagScope));
                tagGroupMap.put(newGroup, newTagList);
                notifyItemInserted(tagGroupMap.size() - 1);
            } else if (currentGroupNo == -1) {
                //遍历查找
                int newGroupIndex = 0;
                for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
                    TagGroup group = entry.getKey();
                    if (group.getGroupNo() == newGroupNo) {
                        List<Tag> newTagList = entry.getValue();
                        newTagList.add(new Tag(newTagName, tagNo, tagScope));
                        break;
                    }
                    newGroupIndex++;
                }

                //更新UI
                if (newGroupIndex != tagGroupMap.size()) {
                    notifyItemChanged(newGroupIndex);
                } else {
                    TagGroup group = new TagGroup(newGroupName, newGroupNo);
                    List<Tag> tagList = new ArrayList<>();
                    tagList.add(new Tag(newTagName, tagNo, tagScope));
                    tagGroupMap.put(group, tagList);
                    notifyItemInserted(tagGroupMap.size() - 1);
                }
            }
        } else {
            //找到需要更新的视图的下标
            List<TagGroup> keyList = new ArrayList<>(tagGroupMap.keySet());
            TagGroup targetGroup = null;
            int position = 0;
            for (TagGroup group : keyList) {
                if (group.getGroupNo() == oldGroupNo) {
                    targetGroup = group;
                    break;
                }
                position++;
            }
            if (targetGroup == null) {
                return;
            }

            //修改内存中的数据
            List<Tag> tagList = tagGroupMap.get(targetGroup);
            if (tagList == null) {
                return;
            }
            for (Tag tag : tagList) {
                if (tag.getTno() == tagNo) {
                    tag.setName(newTagName);
                    tag.setScope(tagScope);
                }
            }

            //更新视图
            notifyItemChanged(position);
        }
    }

    /**
     * 删除标签
     *
     * @param tagNo   待删除标签的编号
     * @param groupNo 待删除标签所属分组的编号
     */
    public void deleteTag(long tagNo, long groupNo) {
        int groupIndex = 0;        //待删除标签所属分组的下标
        for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
            TagGroup group = entry.getKey();
            if (group.getGroupNo() == groupNo) {
                List<Tag> tagList = entry.getValue();
                for (Tag oldTag : tagList) {
                    if (oldTag.getTno() == tagNo) {
                        tagList.remove(oldTag);
                        break;
                    }
                }
                break;
            }

            groupIndex++;
        }

        notifyItemChanged(groupIndex);
    }

    /**
     * 修改分组
     *
     * @param groupNo      分组编号
     * @param newGroupName 分组名称
     */
    public void modifyGroup(long groupNo, String newGroupName) {
        //修改界面中的分组
        int groupIndex = 0;
        for (TagGroup group : this.tagGroupMap.keySet()) {
            if (group.getGroupNo() == groupNo) {
                group.setGroupName(newGroupName);
                break;
            }
            groupIndex++;
        }

        notifyItemChanged(groupIndex);
    }

    /**
     * 删除分组
     *
     * @param groupNo 分组编号
     */
    public void deleteGroup(long groupNo) {
        //获取需要删除的标签列表
        int groupIndex = 0;
        TagGroup deletedGroup = null;
        for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
            TagGroup group = entry.getKey();
            if (group.getGroupNo() == groupNo) {
                deletedGroup = group;
                break;
            }
            groupIndex++;
        }

        tagGroupMap.remove(deletedGroup);
        notifyItemRemoved(groupIndex);
    }

    /**
     * 刷新UI的方法
     *
     * @param tagGroupMap 刷新时重新获取的数据
     */
    public void refreshUI(Map<TagGroup, List<Tag>> tagGroupMap) {
        int oldCount = this.tagGroupMap.size();
        this.tagGroupMap.clear();
        notifyItemRangeRemoved(0, oldCount);

        this.tagGroupMap.putAll(tagGroupMap);
        notifyItemRangeInserted(0, tagGroupMap.size());
    }

    /**
     * 合并标签分组
     *
     * @param mergedGroupNo 被合并的分组编号
     * @param targetGroupNo 合并到的分组的编号
     */
    public void mergeGroup(long mergedGroupNo, long targetGroupNo) {
        //找出新旧分组中的标签
        List<Tag> tagsInOldGroup = null, tagsInTargetGroup = null;
        TagGroup mergedGroup = null;
        int oldGroupIndex = -1, targetGroupIndex = -1;
        int index = 0;
        for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
            TagGroup group = entry.getKey();
            if (group.getGroupNo() == mergedGroupNo) {
                mergedGroup = group;
                tagsInOldGroup = entry.getValue();
                oldGroupIndex = index;
            } else if (group.getGroupNo() == targetGroupNo) {
                tagsInTargetGroup = entry.getValue();
                targetGroupIndex = index;
            }
            index++;
        }

        //合并标签列表并删除被合并的分组
        if (tagsInOldGroup != null && tagsInTargetGroup != null) {
            tagsInTargetGroup.addAll(tagsInOldGroup);
            tagGroupMap.remove(mergedGroup);

            //刷新UI
            notifyItemRemoved(oldGroupIndex);
            notifyItemChanged(targetGroupIndex);
        }
    }

    /**
     * 合并标签
     *
     * @param mergedTagNo 被合并的标签编号
     * @param groupNo     被合并标签的分组编号
     */
    public void mergeTag(long mergedTagNo, long groupNo) {
        int groupIndex = 0;
        int mergedTagGroupIndex = -1;    //被合并的标签所在分组的下标
        for (Map.Entry<TagGroup, List<Tag>> entry : tagGroupMap.entrySet()) {
            TagGroup group = entry.getKey();
            if (group.getGroupNo() == groupNo) {
                mergedTagGroupIndex = groupIndex;
                int tagIndex = 0;
                List<Tag> tagList = entry.getValue();
                for (Tag tag : tagList) {
                    if (tag.getTno() == mergedTagNo) {
                        tagList.remove(tagIndex);
                        break;
                    }
                    tagIndex++;
                }
                break;
            }
            groupIndex++;
        }
        if (mergedTagGroupIndex != -1) { //更新UI
            notifyItemChanged(mergedTagGroupIndex);
        }
    }
}
