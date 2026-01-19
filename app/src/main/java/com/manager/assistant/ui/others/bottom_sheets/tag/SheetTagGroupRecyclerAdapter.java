package com.manager.assistant.ui.others.bottom_sheets.tag;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.util.List;

public class SheetTagGroupRecyclerAdapter extends RecyclerView.Adapter<SheetTagGroupRecyclerAdapter.TagSelectHolder> {
    private List<TagGroup> tagGroupList;        //标签组列表
    private final Context context;              //上下文
    private final long excepted_tag_no;         //被排除的标签编号（不会显示在视图中）
    private SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener;   //标签按钮点击监听器

    public void setOnTagBtnClickedListener(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener) {
        this.tagBtnClickedListener = tagBtnClickedListener;
    }

    public static class TagSelectHolder extends RecyclerView.ViewHolder {
        RecyclerView tagBtnRecycler;                    //标签按钮布局
        MaterialTextView tag_group_name_view;           //标签分组名称

        public TagSelectHolder(@NonNull View itemView) {
            super(itemView);

            tagBtnRecycler = itemView.findViewById(R.id.tag_btn_recycler_view);
            tag_group_name_view = itemView.findViewById(R.id.tag_group_name_view);
        }
    }

    public SheetTagGroupRecyclerAdapter(long excepted_tag_no, Context context) {
        this.excepted_tag_no = excepted_tag_no;
        this.context = context;
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

        //去除需要排除的标签按钮
        int index = 0, delete_tag_index = -1;
        for (Tag tag : tags) {
            if (tag.getTno() == excepted_tag_no) {
                delete_tag_index = index;
            }
            index++;
        }
        if (delete_tag_index != -1) {
            tags.remove(delete_tag_index);
        }

        //根据当前的标签列表是否为空设置视图内容
        if (tags.isEmpty()) {
            holder.tag_group_name_view.setVisibility(View.GONE);
            holder.tagBtnRecycler.setVisibility(View.GONE);
        } else {
            holder.tag_group_name_view.setText(group_name);
            SheetTagBtnRecyclerAdapter btn_layout_adapter = new SheetTagBtnRecyclerAdapter(tags, context, tagBtnClickedListener);
            holder.tagBtnRecycler.setAdapter(btn_layout_adapter);

            //设置布局器
            int spanCount = 3;
            GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
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
    @SuppressLint("NotifyDataSetChanged")
    public void setTagGroupList(List<TagGroup> tagGroupList) {
        this.tagGroupList = tagGroupList;
        notifyDataSetChanged();
    }
}
