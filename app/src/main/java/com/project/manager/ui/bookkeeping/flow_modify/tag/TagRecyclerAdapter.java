package com.project.manager.ui.bookkeeping.flow_modify.tag;

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
import com.project.manager.database.FlowDatabaseHelper;

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
     * 添加新标签
     *
     * @param tag_name   标签名
     * @param group_name 标签分组名
     */
    public void addNewTag(String tag_name, String group_name) {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(context)) {
            SQLiteDatabase db = db_helper.openWriteLink();

            boolean needNewGroup = true;
            for (TagGroup group : tagGroupList) {
                if (group.group_name.equals(group_name)) {
                    group.addTag(tag_name);
                    needNewGroup = false;
                    break;
                }
            }
            if (needNewGroup) {
                TagGroup new_group = new TagGroup(group_name);
                new_group.addTag(tag_name);
                tagGroupList.add(new_group);

                //将新分组插入数据库
                ContentValues group_values = new ContentValues();
                group_values.put(FlowDatabaseHelper.COLUMN_TAG_GROUP, group_name);
                db.insert(FlowDatabaseHelper.TABLE_TAG_GROUP, null, group_values);
            }

            //将新标签插入数据库
            ContentValues tag_values = new ContentValues();
            tag_values.put(FlowDatabaseHelper.COLUMN_TAG, tag_name);
            tag_values.put(FlowDatabaseHelper.COLUMN_TAG_GROUP, group_name);
            db.insert(FlowDatabaseHelper.TABLE_TAG, null, tag_values);

            db.close();

            //TODO: 换成性能开销更少的界面刷新方法
            notifyDataSetChanged();
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("打开数据库失败：数据库被其他进程占用");
        }
    }
}
