package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.data.data_class.TagGroup;
import com.manager.assistant.ui.others.bottom_sheets.tag.GridSpacingItemDecoration;

import java.util.List;

public class SheetTagGroupRecyclerAdapter extends RecyclerView.Adapter<SheetTagGroupRecyclerAdapter.TagSelectHolder> {
    private List<TagGroup> tagGroupList;                                        //标签组列表
    private final SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener;  //标签按钮点击监听器

    public static class TagSelectHolder extends RecyclerView.ViewHolder {
        RecyclerView tagBtnRecycler;                    //标签按钮布局
        MaterialTextView tag_group_name_view;           //标签分组名称

        public TagSelectHolder(@NonNull View itemView) {
            super(itemView);

            tagBtnRecycler = itemView.findViewById(R.id.tag_btn_recycler_view);
            tag_group_name_view = itemView.findViewById(R.id.tag_group_name_view);
        }
    }

    public SheetTagGroupRecyclerAdapter(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TagSelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tag_group = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_tag_select, parent, false);

        return new TagSelectHolder(tag_group);
    }

    @Override
    public void onBindViewHolder(@NonNull TagSelectHolder holder, int position) {
        TagGroup currentTagGroup = this.tagGroupList.get(position);
        String group_name = currentTagGroup.getGroup_name();
        List<Tag> tags = currentTagGroup.getTags();

        //根据当前的标签列表是否为空设置视图内容
        if (tags.isEmpty()) {
            holder.tag_group_name_view.setVisibility(View.GONE);
            holder.tagBtnRecycler.setVisibility(View.GONE);
        } else {
            holder.tag_group_name_view.setText(group_name);
            SheetTagBtnRecyclerAdapter btn_layout_adapter = new SheetTagBtnRecyclerAdapter(tags, listener);
            holder.tagBtnRecycler.setAdapter(btn_layout_adapter);

            //设置布局器
            int spanCount = 3;
            GridLayoutManager layoutManager = new GridLayoutManager(holder.itemView.getContext(), spanCount);
            holder.tagBtnRecycler.setLayoutManager(layoutManager);

            //设置按钮间隔
            int spacing = 16; // 单位：像素
            holder.tagBtnRecycler.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        }
    }

    @Override
    public int getItemCount() {
        return tagGroupList != null ? tagGroupList.size() : 0;
    }

    /**
     * 设置标签分组数据并刷新UI
     *
     * @param tagGroupList 标签分组列表
     */
    public void setTagGroupList(@NonNull List<TagGroup> tagGroupList) {
        this.tagGroupList = tagGroupList;
        notifyItemRangeChanged(0, tagGroupList.size());
    }
}
