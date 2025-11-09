package com.project.manager.ui.bookkeeping.tag.select_sheet;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.manager.R;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.tag.TagGroup;
import com.project.manager.ui.bookkeeping.tag.edit.TagEditActivity;

import java.util.ArrayList;
import java.util.List;

public class TagSelectBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private View binding;                                                           //绑定的XML视图
    private SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener; //标签按钮点击事件的监听器

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflater.inflate(R.layout.bottom_sheet_tag_select, container, false);

        initViews();

        RecyclerView tag_group_recycler_view = binding.findViewById(R.id.tag_group_recycler);
        List<TagGroup> tagGroupList;
        try {
            tagGroupList = TagGroup.loadTagGroups(requireContext());
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            Toast.makeText(requireContext(), "标签数据读取失败", Toast.LENGTH_SHORT).show();
            tagGroupList = new ArrayList<>();
        }
        //标签列表视图适配器
        SheetTagGroupRecyclerAdapter tagAdapter = new SheetTagGroupRecyclerAdapter(tagGroupList, requireContext());
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
            Intent skip2TagEdit = new Intent(requireContext(), TagEditActivity.class);
            startActivity(skip2TagEdit);
            dismiss();
        }
    }

    //设置标签按钮点击监听器
    public void setOnTagBtnClickedListener(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener) {
        tagBtnClickedListener = listener;
    }
}
