package com.project.manager.ui.bookkeeping.tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

import java.util.List;

public class TagSelectRecyclerAdapter extends RecyclerView.Adapter<TagSelectRecyclerAdapter.TagSelectHolder> {
    private final List<TagGroup> tagGroupList;              //标签组列表
    Context context;
    private OnTagBtnClickedListener tagBtnClickedListener;  //标签按钮点击监听器

    public static class TagSelectHolder extends RecyclerView.ViewHolder {
        GridLayout tag_btn_layout;              //标签按钮布局
        MaterialTextView tag_group_name_view;   //标签分组名称
        public TagSelectHolder(@NonNull View itemView) {
            super(itemView);

            tag_btn_layout = itemView.findViewById(R.id.tag_btn_layout);
            tag_group_name_view = itemView.findViewById(R.id.tag_group_name_view);
        }
    }

    //标签按钮点击监听接口
    public interface OnTagBtnClickedListener {
        void onTagBtnClicked(long tag_no, String tag_name); //传递标签编号和名称
    }

    public void setOnTagBtnClickedListener(OnTagBtnClickedListener listener) {
        this.tagBtnClickedListener = listener;
    }

    public TagSelectRecyclerAdapter(List<TagGroup> tagGroupList, Context context) {
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
        for (Tag oneTag : tags) {
            MaterialButton tag_btn = new MaterialButton(context);   //实例化标签按钮

            tag_btn.setText(oneTag.getName());    //设置按钮文本
            tag_btn.setOnClickListener(v -> {   //设置回调接口
                if (tagBtnClickedListener != null) {
                    tagBtnClickedListener.onTagBtnClicked(oneTag.getTno(), oneTag.getName());
                }
            });

            holder.tag_btn_layout.addView(tag_btn);
        }
    }

    @Override
    public int getItemCount() {
        return this.tagGroupList.size();
    }
}
