package com.manager.assistant.ui.pages.picture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.manager.assistant.enums.LogTags;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Picture;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.helpers.AnimationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureViewHolder> {
    private final Context context;                      //上下文
    private final List<Picture> pictureList;            //数据源列表
    private final List<Boolean> pictureSelectList;      //记录图片选择状态的列表
    private boolean isDeleteMode = false;               //标记是否为删除图片模式
    private final RequestOptions glideOptions;
    private final DeleteModeSwitchListener listener;    //删除模式切换监听器

    public interface DeleteModeSwitchListener {
        /**
         * 图片删除模式切换的回调
         *
         * @param isDeleteMode 切换后是否为图片删除模式
         */
        void onDeleteModeSwitched(boolean isDeleteMode);
    }

    public static class PictureViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;                    //图像容器视图
        CheckedTextView checkedTextView;        //右上角复选框

        public PictureViewHolder(@NonNull View view) {
            super(view);
            this.imageView = view.findViewById(R.id.image_view);
            this.checkedTextView = view.findViewById(R.id.checked_text);
        }

        /**
         * 通过Uri设置图片资源
         *
         * @param context      上下文
         * @param uri          图片Uri
         * @param glideOptions glide的设置项
         */
        public void setPictureRes(Context context, @NonNull Uri uri, RequestOptions glideOptions) {
            Glide.with(context)
                    .load(uri)
                    .apply(glideOptions)
                    .into(imageView);
        }
    }

    /**
     * 图片适配器构造方法
     *
     * @param context  上下文
     * @param listener 图片删除状态切换监听器
     */
    public PictureAdapter(Context context, DeleteModeSwitchListener listener) {
        this.context = context;
        this.pictureList = new ArrayList<>();
        this.pictureSelectList = new ArrayList<>();
        this.listener = listener;

        //动态计算图片宽度
        int screen_width = getScreenWidth(context);
        int picture_size = screen_width * 5 / 18;               //图片宽高占屏幕宽度的5/18
        glideOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.baseline_error_outline_24)    //错误图
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) //缓存策略
                .override(picture_size, picture_size);          //图片尺寸
    }

    /**
     * 获取是否为图片删除模式的方法
     *
     * @return 是否为图片删除模式
     */
    public boolean isDeleteMode() {
        return isDeleteMode;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.view_holder_picture, parent, false);
        return new PictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        Picture picture = pictureList.get(position);
        Uri pictureUri = picture.getPictureUri();
        if (pictureUri != null) {
            holder.setPictureRes(context, pictureUri, glideOptions);

            //设置复选框属性
            if (isDeleteMode) {
                holder.checkedTextView.setVisibility(View.VISIBLE);
            } else {
                holder.checkedTextView.setVisibility(View.GONE);
            }
            boolean isChecked = pictureSelectList.get(position);
            holder.checkedTextView.setChecked(isChecked);

            //视图点击监听器
            holder.imageView.setOnClickListener(v -> {
                if (!isDeleteMode) {
                    openPictureCheckActivity(holder.getBindingAdapterPosition());
                } else {
                    holder.checkedTextView.setVisibility(View.VISIBLE);
                    holder.checkedTextView.toggle();

                    //同步图片选择状态数据
                    pictureSelectList.set(holder.getBindingAdapterPosition(), holder.checkedTextView.isChecked());
                }
            });

            //视图长按监听器
            holder.imageView.setOnLongClickListener(v -> {
                if (!isDeleteMode) {
                    pictureSelectList.set(holder.getBindingAdapterPosition(), true);
                    switchDeleteMode(true);

                    Toast.makeText(context, "返回即可退出图片删除模式", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    /**
     * 切换图片删除模式
     *
     * @param isDeleteMode 切换后是否为删除模式
     */
    @SuppressLint("NotifyDataSetChanged")
    public void switchDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
        listener.onDeleteModeSwitched(isDeleteMode);

        if (!isDeleteMode) {
            Collections.fill(pictureSelectList, false);
        }

        for (int index = 0; index < pictureList.size(); index++) {
            notifyItemChanged(index);
        }
    }

    /**
     * 刷新图片
     *
     * @param pictureList 刷新后的图片列表
     */
    public void refreshPicture(List<Picture> pictureList) {
        this.pictureList.clear();
        this.pictureList.addAll(pictureList);
        pictureSelectList.clear();
        pictureSelectList.addAll(new ArrayList<>(Collections.nCopies(pictureList.size(), false)));    //默认未选择

        //刷新UI
        if (pictureList.size() == 1) {
            notifyItemInserted(0);
        } else if (pictureList.size() > 1) {
            notifyItemRangeInserted(0, pictureList.size() - 1);
        }
    }

    /**
     * 打开图片查看Activity
     *
     * @param position 点击的图片的下标
     */
    private void openPictureCheckActivity(int position) {
        //判断文件是否存在
        Uri pictureUri = pictureList.get(position).getPictureUri();
        File pictureFile = new File(Objects.requireNonNull(pictureUri.getPath()));
        if (pictureFile.exists()) {
            //获取所有图片的Uri(仅当图片存在时)
            String[] pictureUris = pictureList.stream()
                    .map(Picture::getPictureUri)
                    .filter(this::isPictureExists)
                    .map(Uri::toString)
                    .toArray(String[]::new);

            Intent skip2ImageActivity = new Intent(context, FullScreenImageActivity.class);
            skip2ImageActivity.putExtra(KeyValueStrings.FILE_URI.getValue(), pictureUris);
            skip2ImageActivity.putExtra(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), getLegalStartPosition(position));
            context.startActivity(skip2ImageActivity);
        }
    }

    /**
     * 判断图片是否存在
     *
     * @param uri 图片Uri
     * @return 图片是否存在
     */
    private boolean isPictureExists(@NonNull Uri uri) {
        File pictureFile = new File(Objects.requireNonNull(uri.getPath()));
        return pictureFile.exists();
    }

    /**
     * 获取合法的起始图片下标
     *
     * @param origin_position 原起始下标
     * @return 除去无效图片后的起始下标
     */
    private int getLegalStartPosition(int origin_position) {
        int lost_picture_num = 0;   //在原起始下标之前丢失的图片的数量
        int index = 0;
        for (Picture picture : pictureList) {
            if (index >= origin_position) break;

            Uri uri = picture.getPictureUri();
            File pictureFile = new File(Objects.requireNonNull(uri.getPath()));
            if (!pictureFile.exists()) {
                lost_picture_num++;
            }

            index++;
        }

        return origin_position - lost_picture_num;
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * 将单个图片添加至界面中
     *
     * @param picture 新相片数据实例
     */
    public void addPicture(Picture picture) {
        pictureSelectList.add(false);
        pictureList.add(picture);
        notifyItemInserted(pictureList.size() - 1);
    }

    /**
     * 添加多个图片到界面中
     *
     * @param pictureList 包含图片数据的列表
     */
    public void addPicture(@NonNull List<Picture> pictureList) {
        int start = this.pictureList.size();    //添加后的起始下标
        int step = pictureList.size();          //步长
        this.pictureList.addAll(pictureList);
        pictureSelectList.addAll(new ArrayList<>(Collections.nCopies(pictureList.size(), false)));

        if (step > 1) {
            notifyItemRangeInserted(start, start + step - 1);
        } else if (step == 1) {
            notifyItemInserted(start);
        }
    }

    /**
     * 删除被选中的图片
     */
    public void deleteSelectedPicture() {
        //从尾部开始删除，避免影响下标值
        int delete_num = 0;
        for (int index = pictureSelectList.size() - 1; index >= 0; index--) {
            boolean isSelected = pictureSelectList.get(index);
            if (isSelected) {
                Picture picture = pictureList.get(index);
                Uri pictureUri = picture.getPictureUri();
                long pno = picture.getPno();

                //删除文件
                File pictureFile = new File(Objects.requireNonNull(pictureUri.getPath()));
                if (!pictureFile.exists() || !pictureFile.delete()) {
                    Log.w(LogTags.PICTURE_ADAPTER.getV(), String.format(
                            Locale.getDefault(),
                            "“%s”不存在或删除失败",
                            pictureFile.getName()
                    ));
                }

                //删除数据库内容（如果该图片本来就在数据库中）
                if (pno != 0) {
                    Picture.deletePicture(context, pno);
                }

                pictureList.remove(index);
                pictureSelectList.remove(index);
                notifyItemRemoved(index);
                delete_num++;
            }
        }

        if (delete_num > 0) {
            Toast.makeText(context, String.format(Locale.getDefault(), "已删除%d张图片", delete_num), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "没有图片被删除", Toast.LENGTH_SHORT).show();
        }

        //关闭图片删除模式
        switchDeleteMode(false);
    }
}
