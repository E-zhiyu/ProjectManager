package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.ui.others.bottom_sheets.tag.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SheetTagGroupRecyclerAdapter extends RecyclerView.Adapter<SheetTagGroupRecyclerAdapter.TagSelectHolder> {
    private Map<TagGroup, List<Tag>> tagGroupMap;                               //标签分组字典
    private final SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener;  //标签按钮点击监听器

    public static class TagSelectHolder extends RecyclerView.ViewHolder {
        RecyclerView tagBtnRecycler;                    //标签按钮布局
        MaterialTextView tagGroupNameText;              //标签分组名称

        public TagSelectHolder(@NonNull View itemView) {
            super(itemView);

            tagBtnRecycler = itemView.findViewById(R.id.tag_btn_recycler_view);
            tagGroupNameText = itemView.findViewById(R.id.tag_group_name_view);
        }
    }

    public SheetTagGroupRecyclerAdapter(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagSelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tagGroup = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_tag_select, parent, false);

        return new TagSelectHolder(tagGroup);
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
            holder.tagGroupNameText.setVisibility(View.GONE);
            holder.tagBtnRecycler.setVisibility(View.GONE);
        } else {
            holder.tagGroupNameText.setText(groupName);
            SheetTagBtnRecyclerAdapter btnLayoutAdapter = new SheetTagBtnRecyclerAdapter(tagList, listener);
            holder.tagBtnRecycler.setAdapter(btnLayoutAdapter);

            //设置布局器
            int spanCount = 3;
            GridLayoutManager layoutManager = new GridLayoutManager(holder.itemView.getContext(), spanCount);
            holder.tagBtnRecycler.setLayoutManager(layoutManager);

            //设置按钮间隔
            int spacing = 16; //单位：像素
            holder.tagBtnRecycler.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
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
