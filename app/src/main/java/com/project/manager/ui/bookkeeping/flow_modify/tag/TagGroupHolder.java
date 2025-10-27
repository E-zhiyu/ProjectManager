package com.project.manager.ui.bookkeeping.flow_modify.tag;

import android.view.View;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

public class TagGroupHolder extends RecyclerView.ViewHolder {
    GridLayout tag_btn_layout;              //标签按钮布局
    MaterialTextView tag_group_name_view;   //标签分组名称
    public TagGroupHolder(@NonNull View itemView) {
        super(itemView);

        tag_btn_layout = itemView.findViewById(R.id.tag_btn_layout);
        tag_group_name_view = itemView.findViewById(R.id.tag_group_name_view);
    }
}
