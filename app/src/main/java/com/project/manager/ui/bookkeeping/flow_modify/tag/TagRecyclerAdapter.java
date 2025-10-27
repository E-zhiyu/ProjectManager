package com.project.manager.ui.bookkeeping.flow_modify.tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.project.manager.R;

import java.util.List;

public class TagRecyclerAdapter extends RecyclerView.Adapter<TagGroupHolder> {
    private List<TagGroup> tagGroupList;   //标签组列表
    Context context;

    public TagRecyclerAdapter(List<TagGroup> tagGroupList, Context context) {
        this.tagGroupList = tagGroupList;
        this.context = context;
    }

    //获取现存的标签组列表
    public List<TagGroup> getTagGroupList() {
        return tagGroupList;
    }

    @NonNull
    @Override
    public TagGroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tag_group = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_group_in_bottom_sheet, parent, false);

        return new TagGroupHolder(tag_group);
    }

    @Override
    public void onBindViewHolder(@NonNull TagGroupHolder holder, int position) {
        TagGroup currentTagGroup = this.tagGroupList.get(position);
        String group_name = currentTagGroup.group_name;
        List<String> tags = currentTagGroup.tags;

        holder.tag_group_name_view.setText(group_name);
        for (String tag_name : tags) {
            MaterialButton tag_btn = new MaterialButton(context);   //实例化标签按钮

            tag_btn.setText(tag_name);    //设置按钮文本

            holder.tag_btn_layout.addView(tag_btn);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }

    /**
     * 标签数据更改时重新加载界面
     *
     * @param tagGroupList 更改后的标签分组列表
     */
    public void reloadAfterChanged(List<TagGroup> tagGroupList) {
        this.tagGroupList = tagGroupList;
        notifyDataSetChanged();
    }
}
