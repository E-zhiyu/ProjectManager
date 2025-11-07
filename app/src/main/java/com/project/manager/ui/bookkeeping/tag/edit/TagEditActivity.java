package com.project.manager.ui.bookkeeping.tag.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.project.manager.R;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.TagGroup;

import java.util.ArrayList;
import java.util.List;

public class TagEditActivity extends AppCompatActivity implements View.OnClickListener, TagEditRecyclerAdapter.OnTagTextViewClickedListener {
    private TagEditRecyclerAdapter adapter;
    private ActivityResultLauncher<Intent> newTagLauncher, modifyTagLauncher;   //活动启动器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_edit);

        initActivityLauncher();
        initViews();

        List<TagGroup> tagGroupList = TagGroup.loadTagGroups(this); //获取标签分组数据

        RecyclerView tagGroupRecycler = findViewById(R.id.tag_group_recycler);
        adapter = new TagEditRecyclerAdapter(tagGroupList, this, this);
        tagGroupRecycler.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tag_add_btn) {
            Intent skip2NewTag = new Intent(this, NewTagActivity.class);

            //获取已保存的标签分组信息
            List<TagGroup> tagGroupList = adapter.getTagGroupList();
            ArrayList<String> groupNameList = new ArrayList<>();
            for (TagGroup group : tagGroupList) {
                groupNameList.add(group.getGroupName());
            }
            skip2NewTag.putStringArrayListExtra(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue(), groupNameList);

            newTagLauncher.launch(skip2NewTag);
        }
    }

    @Override
    public void onTagTextViewClicked(long tag_no, String tag_name, long group_no, String group_name) {
        Intent skip2ModifyTag = new Intent(this, TagModifyActivity.class);
        Bundle clickedTagData = new Bundle();

        clickedTagData.putString(KeyValueStrings.TAG_NAME.getValue(), tag_name);
        clickedTagData.putString(KeyValueStrings.TAG_GROUP_NAME.getValue(), group_name);
        clickedTagData.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        clickedTagData.putLong(KeyValueStrings.TAG_GROUP_NO.getValue(), group_no);

        //获取已保存的标签分组信息
        List<TagGroup> tagGroupList = adapter.getTagGroupList();
        ArrayList<String> groupNameList = new ArrayList<>();
        for (TagGroup group : tagGroupList) {
            groupNameList.add(group.getGroupName());
        }
        clickedTagData.putStringArrayList(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue(), groupNameList);

        skip2ModifyTag.putExtras(clickedTagData);
        modifyTagLauncher.launch(skip2ModifyTag);
    }

    //初始化视图
    private void initViews() {
        MaterialButton tag_add_btn = findViewById(R.id.tag_add_btn);
        tag_add_btn.setOnClickListener(this);
    }

    //初始化活动启动器
    private void initActivityLauncher() {
        newTagLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (data == null && resultCode != Activity.RESULT_CANCELED) {
                        throw new NullPointerException("无法获取新建标签数据");
                    } else {
                        addNewTag(resultCode, data);
                    }
                }
        );

        modifyTagLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (data == null && resultCode != Activity.RESULT_CANCELED) {
                        throw new NullPointerException("无法获取修改后的标签数据");
                    } else {
                        modifyTag(resultCode, data);
                    }
                }
        );
    }

    private void addNewTag(int resultCode, Intent resultIntent) {
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            Bundle dataBundle = resultIntent.getExtras();
            String tag_name = null;         //标签名称
            String group_name = null;       //分组名称
            if (dataBundle != null) {
                tag_name = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());
                group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            }
            long group_no = 0;   //分组编号

            //判断是否需要新的分组
            List<TagGroup> tagGroupList = adapter.getTagGroupList();
            boolean needNewGroup = true;
            for (TagGroup oneGroup : tagGroupList) {
                if (oneGroup.getGroupName().equals(group_name)) {
                    needNewGroup = false;
                    group_no = TagGroup.nameTransToGno(group_name, this);
                    break;
                }
            }
            if (needNewGroup) {
                group_no = TagGroup.saveNewGroup(group_name, this);
            }

            //将新标签的数据写入数据库
            long tag_no = Tag.saveNewTag(tag_name, group_no, this); //获取标签编号
            if (tag_no != 0) {
                //将变化保存至列表中并传递给适配器
                Tag new_tag = new Tag(tag_name, tag_no);
                if (needNewGroup) {
                    TagGroup new_group = new TagGroup(group_name, group_no);
                    adapter.addNewTag(new_tag, new_group);
                } else {
                    adapter.addNewTag(new_tag, group_no);
                }
            }
        }
    }

    private void modifyTag(int resultCode, Intent resultIntent) {
        Bundle dataBundle;
        try {
            dataBundle = resultIntent.getExtras();
        } catch (NullPointerException e) {
            return; //若引发空指针异常，则说明在标签修改界面没有进行任何操作就退出
        }
        if (dataBundle == null) {
            throw new RuntimeException("无法获取修改后的标签信息");
        }

        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());                    //标签编号
        long origin_group_no = dataBundle.getLong(KeyValueStrings.TAG_GROUP_NO.getValue());     //原分组编号
        if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            String tag_name = dataBundle.getString(KeyValueStrings.TAG_NAME.getValue());
            String group_name = dataBundle.getString(KeyValueStrings.TAG_GROUP_NAME.getValue());
            long new_group_no = TagGroup.nameTransToGno(group_name, this);  //获取修改后的分组编号

            if (origin_group_no == new_group_no) {
                adapter.editTag(tag_name, tag_no, origin_group_no);
            } else {
                adapter.editTag(tag_name, tag_no, group_name, origin_group_no, new_group_no);
            }
        } else if (resultCode == RequestResultCode.RESULT_REJECT.ordinal()) {
            return;
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            adapter.deleteTag(tag_no, origin_group_no);
        }
    }
}