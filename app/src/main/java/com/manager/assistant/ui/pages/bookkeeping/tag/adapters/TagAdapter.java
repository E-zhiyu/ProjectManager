package com.manager.assistant.ui.pages.bookkeeping.tag.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.databinding.ViewHolderTagBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private List<Tag> tagList = new ArrayList<>();              //数据源列表
    private List<Tag> backTagList;                              //用于隐藏ViewHolder的标签列表
    private final OnClickedListener listener;                   //点击监听器
    private TagGroup group;                                     //标签分组实例

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagBinding binding;

        public TagViewHolder(@NonNull ViewHolderTagBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置监听器
            binding.getRoot().setOnClickListener(view -> listener.onClicked(getBindingAdapterPosition()));
        }
    }

    /**
     * 标签适配器构造方法
     *
     * @param group    分组实例
     * @param tagList  标签数据源列表
     * @param listener 标签点击监听器
     */
    public TagAdapter(TagGroup group, List<Tag> tagList, OnClickedListener listener) {
        this.group = group;
        this.tagList.addAll(tagList);
        this.listener = listener;
    }

    public interface OnClickedListener {
        /**
         * 处理标签文本视图点击事件
         *
         * @param tag   标签实例
         * @param group 标签分组
         */
        void onTagClicked(Tag tag, TagGroup group);
    }

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         */
        void onClicked(int position);
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderTagBinding binding = ViewHolderTagBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TagViewHolder(binding, position -> {
            Tag tag = tagList.get(position);
            listener.onTagClicked(
                    tag,
                    group
            );
        });
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tagList.get(position);
        holder.binding.tagNameText.setText(tag.getName());

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.itemView, tagList.size() + 1, position + 1);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    /**
     * 添加标签
     *
     * @param tag 新标签实例
     */
    public void addTag(Tag tag) {
        tagList.add(tag);
        notifyItemInserted(tagList.size() - 1);
        notifyItemChanged(tagList.size() - 2);  //通知之前的尾部改变圆角
    }

    /**
     * 批量添加标签
     *
     * @param tagList 标签列表
     */
    public void addTag(List<Tag> tagList) {
        int oldCount = this.tagList.size();
        this.tagList.addAll(tagList);
        notifyItemRangeInserted(oldCount, tagList.size());
        notifyItemChanged(oldCount - 1);        //通知之前的尾部改变圆角
    }

    /**
     * 修改标签
     *
     * @param tag 修改后的标签实例
     */
    public void modifyTag(Tag tag) {
        int index = 0;
        for (Tag t : tagList) {
            if (t.getTno() == tag.getTno()) {
                tagList.set(index, tag);
                notifyItemChanged(index);
                break;
            }
            index++;
        }
    }

    /**
     * 删除标签
     *
     * @param tagNo 需要被删除的标签编号
     */
    public void deleteTag(long tagNo) {
        int index = 0;
        for (Tag t : tagList) {
            if (t.getTno() == tagNo) {
                tagList.remove(index);
                notifyItemRemoved(index);
                notifyItemChanged(tagList.size() - 1);  //通知尾部改变圆角
                break;
            }
            index++;
        }
    }

    /**
     * 分组修改回调
     *
     * @param group 修改后的标签分组
     */
    public void onGroupModified(@NonNull TagGroup group) {
        this.group = group;
    }

    /**
     * 切换展开状态
     *
     * @param isExpanded 是否展开
     */
    public void changeExpandStatue(boolean isExpanded) {
        if (isExpanded && backTagList != null) {
            tagList = backTagList;
            backTagList = null;
            notifyItemRangeInserted(0, tagList.size());
        } else if (!isExpanded && tagList != null) {
            backTagList = tagList;
            tagList = null;
            notifyItemRangeRemoved(0, backTagList.size());
        }
    }
}
