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
import com.project.manager.ui.bookkeeping.tag.edit.TagEditActivity;

import java.util.List;

public class TagSelectBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    View binding;                   //绑定的XML视图
    TagSelectRecyclerAdapter tagAdapter;  //标签列表视图适配器

    TagSelectRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener;   //标签按钮点击事件的监听器

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflater.inflate(R.layout.bottom_sheet_tag_select, container, false);

        initViews();

        RecyclerView tag_group_recycler_view = binding.findViewById(R.id.tag_group_recycler);
        List<TagGroup> tagGroupList = TagGroup.loadTagGroups(requireContext());
        tagAdapter = new TagSelectRecyclerAdapter(tagGroupList, requireContext());
        tag_group_recycler_view.setAdapter(tagAdapter);

        //设置标签按钮点击事件监听器
        tagAdapter.setOnTagBtnClickedListener(this.tagBtnClickedListener);

        return binding;
    }

    //初始化视图
    private void initViews() {
        binding.findViewById(R.id.edit_tag_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.edit_tag_btn) {
            Intent skip2TagEdit = new Intent(getActivity(), TagEditActivity.class);
            startActivity(skip2TagEdit);
            dismiss();
        }
    }

    public void setOnTagBtnClickedListener(TagSelectRecyclerAdapter.OnTagBtnClickedListener listener) {
        this.tagBtnClickedListener = listener;
    }
}
