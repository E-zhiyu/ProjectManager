package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.package_name_select;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.AppInfo;
import com.manager.assistant.databinding.ViewHolderAppInfoBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppInfoViewHolder> {
    private final AppClickedListener listener;  //应用条目点击监听器
    private final List<AppInfo> appInfoList;    //应用列表

    public static class AppInfoViewHolder extends RecyclerView.ViewHolder {
        ViewHolderAppInfoBinding binding;

        public AppInfoViewHolder(@NonNull ViewHolderAppInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface AppClickedListener {
        /**
         * 应用视图被点击的回调方法
         *
         * @param packageName 被点击的应用的包名
         */
        void onAppClicked(String packageName);
    }

    public AppListAdapter(AppClickedListener listener) {
        this.appInfoList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderAppInfoBinding binding = ViewHolderAppInfoBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AppInfoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppInfoViewHolder holder, int position) {
        //获取应用信息数据
        AppInfo appInfo = appInfoList.get(position);
        String appName = appInfo.getAppName();
        String packageName = appInfo.getPackageName();
        Bitmap appIcon = appInfo.getAppIcon();

        //初始化视图
        holder.binding.appNameText.setText(appName);
        holder.binding.packageNameText.setText(packageName);
        holder.binding.appIconView.setImageBitmap(appIcon);

        //设置视图圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.itemView, appInfoList.size(), position);

        //设置点击监听
        AppearanceAnimationHelper.attachMorphAnimation(holder.itemView);
        holder.itemView.setOnClickListener(v -> listener.onAppClicked(packageName));
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    /**
     * 刷新应用信息
     *
     * @param appInfoList 刷新后的应用信息列表
     */
    public void setAppInfoList(List<AppInfo> appInfoList) {
        int oldCount = this.appInfoList.size();
        this.appInfoList.clear();
        notifyItemRangeRemoved(0, oldCount);

        this.appInfoList.addAll(appInfoList);
        notifyItemRangeInserted(0, appInfoList.size());
    }
}
