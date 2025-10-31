package com.project.manager.ui.bookkeeping.tag.modify;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.project.manager.R;
import com.project.manager.ui.bookkeeping.tag.TagGroup;

import java.util.List;

public class TagModifyActivity extends AppCompatActivity implements TagModifyRecyclerAdapter.OnTagGroupClickedListener {
    TagModifyRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_modify);

        List<TagGroup> tagGroupList = TagGroup.loadTagGroups(this); //获取标签分组数据

        RecyclerView tagGroupRecycler = findViewById(R.id.tag_group_recycler);
        adapter = new TagModifyRecyclerAdapter(tagGroupList, this, this);
        tagGroupRecycler.setAdapter(adapter);
    }

    @Override
    public void onTagGroupClicked(int position) {
        //TODO: 设计标签分组展开和折叠方法

        adapter.notifyItemChanged(position);
    }
}