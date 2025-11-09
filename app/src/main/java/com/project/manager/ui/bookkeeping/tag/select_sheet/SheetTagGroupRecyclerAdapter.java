package com.project.manager.ui.bookkeeping.tag.select_sheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.TagGroup;

import java.util.List;

public class SheetTagGroupRecyclerAdapter extends RecyclerView.Adapter<SheetTagGroupRecyclerAdapter.TagSelectHolder> {
    private final List<TagGroup> tagGroupList;              //标签组列表
    Context context;
    SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener;   //标签按钮点击监听器

    public void setOnTagBtnClickedListener(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener) {
        this.tagBtnClickedListener = tagBtnClickedListener;
    }

    public static class TagSelectHolder extends RecyclerView.ViewHolder {
        RecyclerView tag_btn_layout;                    //标签按钮布局
        SheetTagBtnRecyclerAdapter btn_layout_adapter;  //标签按钮布局适配器
        MaterialTextView tag_group_name_view;           //标签分组名称

        public TagSelectHolder(@NonNull View itemView) {
            super(itemView);

            tag_btn_layout = itemView.findViewById(R.id.tag_btn_recycler_view);
            tag_group_name_view = itemView.findViewById(R.id.tag_group_name_view);
        }
    }

    public SheetTagGroupRecyclerAdapter(List<TagGroup> tagGroupList, Context context) {
        this.tagGroupList = tagGroupList;
        this.context = context;
    }

    @NonNull
    @Override
    public TagSelectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tag_group = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_group_in_bottom_sheet, parent, false);

        return new TagSelectHolder(tag_group);
    }

    @Override
    public void onBindViewHolder(@NonNull TagSelectHolder holder, int position) {
        TagGroup currentTagGroup = this.tagGroupList.get(position);
        String group_name = currentTagGroup.getGroupName();
        List<Tag> tags = currentTagGroup.getTags();

        holder.tag_group_name_view.setText(group_name);
        holder.btn_layout_adapter = new SheetTagBtnRecyclerAdapter(tags, context);
        holder.tag_btn_layout.setAdapter(holder.btn_layout_adapter);
        holder.btn_layout_adapter.setOnTagBtnClickedListener(tagBtnClickedListener);    //设置标签按钮点击监听器

        //设置布局器
        int spanCount = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        holder.tag_btn_layout.setLayoutManager(layoutManager);

        //设置按钮间隔
        int spacing = 16; // 单位：像素
        holder.tag_btn_layout.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }
}
