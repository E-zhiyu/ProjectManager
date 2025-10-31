package com.project.manager.ui.bookkeeping.tag;

import android.content.Intent;
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
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.tag.modify.NewTagActivity;
import com.project.manager.ui.bookkeeping.tag.modify.TagModifyActivity;

import java.util.ArrayList;
import java.util.List;

//TODO:增加标签编辑界面
public class TagSelectBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    View binding;                   //绑定的XML视图
    TagRecyclerAdapter tagAdapter;  //标签列表视图适配器

    TagRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener;   //标签按钮点击事件的监听器

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflater.inflate(R.layout.bottom_sheet_tag_select, container, false);

        initViews();

        RecyclerView tag_group_recycler_view = binding.findViewById(R.id.tag_group_recycler);
        List<TagGroup> tagGroupList = TagGroup.loadTagGroups(requireContext());
        tagAdapter = new TagRecyclerAdapter(tagGroupList, requireContext());
        tag_group_recycler_view.setLayoutManager(new LinearLayoutManager(requireActivity()));
        tag_group_recycler_view.setAdapter(tagAdapter);

        //设置标签按钮点击事件监听器
        tagAdapter.setOnTagBtnClickedListener(this.tagBtnClickedListener);

        return binding;
    }

    //初始化视图
    private void initViews() {
        binding.findViewById(R.id.add_tag_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_tag_btn) {
            addNewTag();
        }
    }

    private void addNewTag() {
//        Intent skip2NewTag = new Intent(getActivity(), NewTagActivity.class);
//
//        //获取已保存的标签分组信息
//        List<TagGroup> tagGroupList = tagAdapter.getTagGroupList();
//        ArrayList<String> groupNameList = new ArrayList<>();
//        for (TagGroup group : tagGroupList) {
//            groupNameList.add(group.group_name);
//        }
//        skip2NewTag.putStringArrayListExtra(KeyValueStrings.TAG_GROUP_NAME_LIST.getValue(), groupNameList);
//
//        startActivityForResult(skip2NewTag, RequestResultCode.NEW_TAG_REQUEST.ordinal());

        Intent skip2TagModify = new Intent(getActivity(), TagModifyActivity.class);
        startActivity(skip2TagModify);
        dismiss();
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
                    tag_name = dataBundle.getString(TagAttributions.NAME.getValue());
                    group_name = dataBundle.getString(TagAttributions.GROUP_NAME.getValue());
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

    public void setOnTagBtnClickedListener(TagRecyclerAdapter.OnTagBtnClickedListener listener) {
        this.tagBtnClickedListener = listener;
    }
}
