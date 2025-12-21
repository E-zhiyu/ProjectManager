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
import com.google.android.material.button.MaterialButton;
import com.project.manager.R;
import com.project.manager.databinding.BottomSheetTagSelectBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.data.data_class.TagGroup;
import com.project.manager.ui.bookkeeping.tag.edit.TagManageActivity;

import java.util.ArrayList;
import java.util.List;

public class TagSelectBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private BottomSheetTagSelectBinding binding;    //绑定的XML视图
    private long excepted_tag_no = 0;               //被排除的标签编号（不会显示）
    private boolean isTagExcepted = false;          //是否存在被排除的标签
    private final SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener; //标签按钮点击事件的监听器
    private String edit_btn_str = "标签管理";       //左侧编辑按钮文本
    private String clear_btn_str = "清除输入";      //右侧清除按钮文本

    public interface TagDataObserver {
        void startObserveTag();
    }

    /**
     * 标签选择菜单构造方法
     *
     * @param listener 标签按钮点击监听器
     * @param observer 标签数据更改观察者，用于观察标签是否修改/删除
     */
    public TagSelectBottomSheet(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener, TagDataObserver observer) {
        this.tagBtnClickedListener = listener;
        //标签数据观察者（观察标签是否更改或者删除）
        if (observer != null) {
            observer.startObserveTag();
        }
    }

    /**
     * 标签选择菜单构造方法
     *
     * @param listener      标签按钮点击监听器
     * @param edit_btn_str  标签编辑按钮的文本
     * @param clear_btn_str 清除输入按钮的文本
     */
    public TagSelectBottomSheet(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener, String edit_btn_str, String clear_btn_str) {
        this.tagBtnClickedListener = listener;
        this.edit_btn_str = edit_btn_str;
        this.clear_btn_str = clear_btn_str;
    }

    /**
     * 指定排除的标签的构造方法
     *
     * @param listener        标签按钮点击的监听器
     * @param excepted_tag_no 被排除的标签编号
     */
    public TagSelectBottomSheet(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener, long excepted_tag_no) {
        this.tagBtnClickedListener = listener;
        this.excepted_tag_no = excepted_tag_no;
        isTagExcepted = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetTagSelectBinding.inflate(inflater, container, false);

        initViews();

        RecyclerView tag_group_recycler_view = binding.tagGroupRecycler;
        List<TagGroup> tagGroupList;
        try {
            tagGroupList = TagGroup.loadTagGroups(requireContext());
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            Toast.makeText(requireContext(), "标签数据读取失败", Toast.LENGTH_SHORT).show();
            tagGroupList = new ArrayList<>();
        }
        //标签列表视图适配器
        SheetTagGroupRecyclerAdapter tagAdapter = new SheetTagGroupRecyclerAdapter(tagGroupList, excepted_tag_no, requireContext());
        tag_group_recycler_view.setAdapter(tagAdapter);

        //设置标签按钮点击事件监听器
        tagAdapter.setOnTagBtnClickedListener(this.tagBtnClickedListener);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //初始化视图
    private void initViews() {
        MaterialButton edit_tag_btn = binding.editTagBtn;
        MaterialButton clear_input_btn = binding.clearInputBtn;

        //设置按钮文本
        if (edit_btn_str != null) {
            edit_tag_btn.setText(edit_btn_str);
        } else {
            edit_tag_btn.setVisibility(View.GONE);
        }
        if (clear_btn_str != null) {
            clear_input_btn.setText(clear_btn_str);
        } else {
            clear_input_btn.setVisibility(View.GONE);
        }

        if (!isTagExcepted) {
            edit_tag_btn.setOnClickListener(this);
            clear_input_btn.setOnClickListener(this);
        } else {
            edit_tag_btn.setVisibility(View.GONE);
            clear_input_btn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.edit_tag_btn) {
            Intent skip2TagEdit = new Intent(requireContext(), TagManageActivity.class);
            startActivity(skip2TagEdit);
            dismiss();
        } else if (v.getId() == R.id.clear_input_btn) {
            tagBtnClickedListener.onTagBtnClicked(0, "");
        }
    }
}
