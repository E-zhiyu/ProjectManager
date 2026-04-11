package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.databinding.ViewHolderTagSelectBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SheetTagGroupRecyclerAdapter extends RecyclerView.Adapter<SheetTagGroupRecyclerAdapter.TagSelectHolder> {
    private Map<TagGroup, List<Tag>> tagGroupMap;                               //标签分组字典
    private final SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener;  //标签按钮点击监听器

    public static class TagSelectHolder extends RecyclerView.ViewHolder {
        ViewHolderTagSelectBinding binding;

        public TagSelectHolder(@NonNull ViewHolderTagSelectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public SheetTagGroupRecyclerAdapter(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagSelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderTagSelectBinding binding = ViewHolderTagSelectBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TagSelectHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagSelectHolder holder, int position) {
        List<Map.Entry<TagGroup, List<Tag>>> entryList = new ArrayList<>(tagGroupMap.entrySet());
        Map.Entry<TagGroup, List<Tag>> currentEntry = entryList.get(position);
        TagGroup currentGroup = currentEntry.getKey();
        String groupName = currentGroup.getGroupName();
        List<Tag> tagList = currentEntry.getValue();

        //根据当前的标签列表是否为空设置视图内容
        if (tagList.isEmpty()) {
            holder.binding.groupNameText.setVisibility(View.GONE);
            holder.binding.tagBtnRecyclerView.setVisibility(View.GONE);
        } else {
            holder.binding.groupNameText.setText(groupName);

            SheetTagBtnRecyclerAdapter btnLayoutAdapter = new SheetTagBtnRecyclerAdapter(tagList, listener);
            holder.binding.tagBtnRecyclerView.setAdapter(btnLayoutAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return tagGroupMap != null ? tagGroupMap.size() : 0;
    }

    /**
     * 设置标签分组数据并刷新UI
     *
     * @param tagGroupMap 标签分组列表
     */
    public void setTagGroupMap(@NonNull Map<TagGroup, List<Tag>> tagGroupMap) {
        this.tagGroupMap = tagGroupMap;
        notifyItemRangeChanged(0, tagGroupMap.size());
    }
}
