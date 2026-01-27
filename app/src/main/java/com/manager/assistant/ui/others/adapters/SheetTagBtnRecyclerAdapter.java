package com.manager.assistant.ui.others.adapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.data.data_class.Tag;

import java.util.List;

public class SheetTagBtnRecyclerAdapter extends RecyclerView.Adapter<SheetTagBtnRecyclerAdapter.BtnViewHolder> {
    private final List<Tag> tagList;  //标签数据源列表
    private final Context context;    //上下文
    private final OnTagBtnClickedListener tagBtnClickedListener;  //标签按钮点击监听器

    public SheetTagBtnRecyclerAdapter(List<Tag> tagList, Context context, OnTagBtnClickedListener tagBtnClickedListener) {
        this.tagList = tagList;
        this.context = context;
        this.tagBtnClickedListener = tagBtnClickedListener;
    }

    @NonNull
    @Override
    public BtnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialButton tagBtn = new MaterialButton(context);

        //设置按钮的属性
        tagBtn.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tagBtn.setPadding(16, 16, 16, 16);
        AnimationHelper.attachMorphAnimation(tagBtn);

        return new BtnViewHolder(tagBtn);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnViewHolder holder, int position) {
        Tag oneTag = tagList.get(position);
        String tag_name = oneTag.getName();
        long tag_no = oneTag.getTno();

        holder.tagBtn.setText(tag_name);

        holder.tagBtn.setOnClickListener(v -> {
            try {
                tagBtnClickedListener.onTagBtnClicked(tag_no, tag_name);
            } catch (NullPointerException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                Toast.makeText(context, "标签按钮点击监听器初始化异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    //标签按钮点击监听接口
    public interface OnTagBtnClickedListener {
        void onTagBtnClicked(long tag_no, String tagName); //传递标签编号和名称
    }

    public static class BtnViewHolder extends RecyclerView.ViewHolder {
        MaterialButton tagBtn;     //标签按钮

        public BtnViewHolder(@NonNull MaterialButton tagBtn) {
            super(tagBtn);
            this.tagBtn = tagBtn;
        }
    }
}
