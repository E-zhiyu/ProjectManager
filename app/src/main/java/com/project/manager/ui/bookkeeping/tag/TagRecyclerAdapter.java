package com.project.manager.ui.bookkeeping.tag;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.project.manager.R;
import com.project.manager.database.FlowColumns;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.database.FlowTables;

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
        return this.tagGroupList;
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
        List<Tag> tags = currentTagGroup.tags;

        holder.tag_group_name_view.setText(group_name);
        for (Tag oneTag : tags) {
            MaterialButton tag_btn = new MaterialButton(context);   //实例化标签按钮

            tag_btn.setText(oneTag.name);    //设置按钮文本

            holder.tag_btn_layout.addView(tag_btn);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }

    /**
     * 添加新标签
     *
     * @param tagGroupList 修改后的标签分组列表
     */
    public void addNewTag(List<TagGroup> tagGroupList) {
        this.tagGroupList = tagGroupList;

        //TODO: 换成性能开销更少的界面刷新方法
        notifyDataSetChanged();

    }
}
