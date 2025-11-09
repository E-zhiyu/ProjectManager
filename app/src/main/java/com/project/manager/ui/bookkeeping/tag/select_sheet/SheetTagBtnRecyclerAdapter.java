package com.project.manager.ui.bookkeeping.tag.select_sheet;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.tag.Tag;

import java.util.List;

public class SheetTagBtnRecyclerAdapter extends RecyclerView.Adapter<SheetTagBtnRecyclerAdapter.BtnViewHolder> {
    List<Tag> tagList;  //标签数据源列表
    Context context;    //上下文
    private OnTagBtnClickedListener tagBtnClickedListener;  //标签按钮点击监听器

    public SheetTagBtnRecyclerAdapter(List<Tag> tagList, Context context) {
        this.tagList = tagList;
        this.context = context;
    }

    @NonNull
    @Override
    public BtnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialButton tag_btn = new MaterialButton(context);

        //设置按钮的属性
        tag_btn.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tag_btn.setPadding(16, 16, 16, 16);

        return new BtnViewHolder(tag_btn);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnViewHolder holder, int position) {
        Tag oneTag = tagList.get(position);
        String tag_name = oneTag.getName();
        long tag_no = oneTag.getTno();

        holder.tag_btn.setText(tag_name);

        holder.tag_btn.setOnClickListener(v -> {
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
        void onTagBtnClicked(long tag_no, String tag_name); //传递标签编号和名称
    }

    public static class BtnViewHolder extends RecyclerView.ViewHolder {
        MaterialButton tag_btn;     //标签按钮

        public BtnViewHolder(@NonNull MaterialButton tag_btn) {
            super(tag_btn);
            this.tag_btn = tag_btn;
        }
    }

    public void setOnTagBtnClickedListener(OnTagBtnClickedListener listener) {
        this.tagBtnClickedListener = listener;
    }
}
