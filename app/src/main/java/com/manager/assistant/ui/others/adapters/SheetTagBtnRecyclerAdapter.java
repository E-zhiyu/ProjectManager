package com.manager.assistant.ui.others.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.Shapeable;
import com.manager.assistant.R;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;

import java.util.List;

public class SheetTagBtnRecyclerAdapter extends RecyclerView.Adapter<SheetTagBtnRecyclerAdapter.BtnViewHolder> {
    private final List<Tag> tagList;                                //标签数据源列表
    private final OnTagBtnClickedListener tagBtnClickedListener;    //标签按钮点击监听器

    //标签按钮点击监听接口
    public interface OnTagBtnClickedListener {
        void onTagBtnClicked(long tag_no, String tagName); //传递标签编号和名称
    }

    public static class BtnViewHolder extends RecyclerView.ViewHolder {
        MaterialButton tagBtn;     //标签按钮
        SpringAnimationOnTouchListener onTouchListener; //带有点击动画的监听器

        public BtnViewHolder(@NonNull View view) {
            super(view);
            this.tagBtn = view.findViewById(R.id.tag_btn);

            //设置触摸监听器
            Vibrator vibrator = (Vibrator) view.getContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            onTouchListener = new SpringAnimationOnTouchListener((Shapeable) view, vibrator);
            view.setOnTouchListener(onTouchListener);
        }
    }

    public SheetTagBtnRecyclerAdapter(List<Tag> tagList, OnTagBtnClickedListener tagBtnClickedListener) {
        this.tagList = tagList;
        this.tagBtnClickedListener = tagBtnClickedListener;
    }

    @NonNull
    @Override
    public BtnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_tag_btn, parent, false);
        return new BtnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnViewHolder holder, int position) {
        Tag oneTag = tagList.get(position);
        String tagName = oneTag.getName();
        long tag_no = oneTag.getTno();

        holder.tagBtn.setText(tagName);

        holder.tagBtn.setOnClickListener(v -> {
            try {
                tagBtnClickedListener.onTagBtnClicked(tag_no, tagName);
            } catch (NullPointerException e) {
                ExceptionHelper.showExceptionDialog(holder.itemView.getContext(), e);
                Toast.makeText(holder.itemView.getContext(), "标签按钮点击监听器初始化异常", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }
}
