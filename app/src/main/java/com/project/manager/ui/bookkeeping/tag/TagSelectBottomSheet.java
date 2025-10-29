package com.project.manager.ui.bookkeeping.tag;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.manager.R;
import com.project.manager.RequestResultCode;
import com.project.manager.database.FlowColumns;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.database.FlowTables;

import java.util.ArrayList;
import java.util.List;

public class TagSelectBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    View binding;                   //绑定的XML视图
    TagRecyclerAdapter tagAdapter;  //标签列表视图适配器

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflater.inflate(R.layout.bottom_sheet_tag_select, container, false);

        initViews();

        RecyclerView tag_group_recycler_view = binding.findViewById(R.id.tag_group_recycler);
        List<TagGroup> tagGroupList = loadTagData();
        tagAdapter = new TagRecyclerAdapter(tagGroupList, requireContext());
        tag_group_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));
        tag_group_recycler_view.setAdapter(tagAdapter);

        return binding;
    }

    //初始化视图
    private void initViews() {
        binding.findViewById(R.id.add_tag_btn).setOnClickListener(this);
    }

    //加载标签数据
    @NonNull
    private List<TagGroup> loadTagData() {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(getActivity())) {
            SQLiteDatabase db = db_helper.openReadLink();

            //查询标签并分组
            Cursor tag_cursor = db.query(
                    FlowTables.TAG.toString(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<TagGroup> tagGroupList = new ArrayList<>();    //标签组实例列表
            while (tag_cursor.moveToNext()) {
                String tag_name = tag_cursor.getString(tag_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NAME.toString()));  //标签名称
                long tag_no = tag_cursor.getInt(tag_cursor.getColumnIndexOrThrow(FlowColumns.TAG_NO.toString()));           //标签编号
                long group_no = tag_cursor.getInt(tag_cursor.getColumnIndexOrThrow(FlowColumns.GROUP_NO.toString()));       //分组编号
                String group_name = TagGroup.groupNoTransToName(group_no, requireContext());                                //分组名称

                boolean isGroupFound = false;   //判断是否找到同号分组
                for (TagGroup group : tagGroupList) {
                    if (group.group_no == group_no) {
                        group.addTag(new Tag(tag_name, tag_no));    //找到同号分组：将标签添加至该分组
                        isGroupFound = true;
                        break;
                    }
                }
                if (!isGroupFound) {
                    TagGroup newGroup = new TagGroup(group_name, group_no);
                    newGroup.addTag(new Tag(tag_name, tag_no));     //找不到同号分组：新建分组并添加标签至该分组
                    tagGroupList.add(newGroup);
                }
            }

            tag_cursor.close();
            db.close();
            return tagGroupList;
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("无法读取标签信息：数据库被其他进程占用");
        } catch (SQLiteException e) {
            throw new RuntimeException("数据库读取失败");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_tag_btn) {
            addNewTag();
        } else {
            //TODO:标签按钮点击事件

        }
    }

    private void addNewTag() {
        Intent skip2NewTag = new Intent(getActivity(), NewTagActivity.class);
        startActivityForResult(skip2NewTag, RequestResultCode.NEW_TAG_REQUEST.ordinal());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);

        if (resultCode != RequestResultCode.RESULT_OK.ordinal())
            return;
        else {
            if (requestCode == RequestResultCode.NEW_TAG_REQUEST.ordinal()) {
                Bundle dataBundle = resultIntent.getExtras();
                String tag_name = null;         //标签名称
                String group_name = null;       //分组名称
                if (dataBundle != null) {
                    tag_name = dataBundle.getString(TagAttributions.NAME.value);
                    group_name = dataBundle.getString(TagAttributions.GROUP_NAME.value);
                }
                long group_no = 0;   //分组编号

                //判断是否需要新的分组
                List<TagGroup> tagGroupList = tagAdapter.getTagGroupList();
                boolean needNewGroup = true;
                for (TagGroup oneGroup : tagGroupList) {
                    if (oneGroup.group_name.equals(group_name)) {
                        needNewGroup = false;
                        group_no = TagGroup.nameTransToGno(group_name, requireContext());
                        break;
                    }
                }
                if (needNewGroup) {
                    group_no = TagGroup.saveNewGroup(group_name, requireContext());
                }

                //将新标签的数据写入数据库
                long tag_no = Tag.saveNewTag(tag_name, group_no, requireContext()); //获取标签编号

                //将变化保存至列表中并传递给适配器
                if (needNewGroup) {
                    Tag new_tag = new Tag(tag_name, tag_no);
                    TagGroup new_group = new TagGroup(group_name, group_no);
                    tagAdapter.addNewTag(new_tag, new_group);
                } else {
                    Tag new_tag = new Tag(tag_name, tag_no);
                    tagAdapter.addNewTag(new_tag, group_no);
                }
            }
        }
    }
}
