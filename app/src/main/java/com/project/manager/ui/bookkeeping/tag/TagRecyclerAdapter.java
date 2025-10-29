package com.project.manager.ui.bookkeeping.tag;

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
    private final List<TagGroup> tagGroupList;              //标签组列表
    Context context;
    private OnTagBtnClickedListener tagBtnClickedListener;  //标签按钮点击监听器

    //标签按钮点击监听接口
    public interface OnTagBtnClickedListener {
        void onTagBtnClicked(long tag_no, String tag_name); //传递标签编号和名称
    }

    public void setOnTagBtnClickedListener(OnTagBtnClickedListener listener) {
        this.tagBtnClickedListener = listener;
    }

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
            tag_btn.setOnClickListener(v -> {   //设置回调接口
                if (tagBtnClickedListener != null) {
                    tagBtnClickedListener.onTagBtnClicked(oneTag.tno, oneTag.name);
                }
            });

            holder.tag_btn_layout.addView(tag_btn);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }

    /**
     * 添加新标签（不添加新分组）
     *
     * @param new_tag         新标签对象
     * @param target_group_no 已存在的分组编号
     */
    public void addNewTag(Tag new_tag, long target_group_no) {
        int position = 0;   //待刷新的分组下标
        for (TagGroup group : this.tagGroupList) {
            if (group.group_no == target_group_no) {
                group.addTag(new_tag);
                break;
            }
            position++;
        }

        notifyItemChanged(position);
    }

    /**
     * 添加新标签（同时添加新分组）
     *
     * @param new_tag   新标签对象
     * @param new_group 新分组对象
     */
    public void addNewTag(Tag new_tag, TagGroup new_group) {
        int list_size = this.tagGroupList.size();
        this.tagGroupList.add(new_group);

        notifyItemInserted(list_size);

        new_group.addTag(new_tag);
        notifyItemChanged(list_size);
    }
}
