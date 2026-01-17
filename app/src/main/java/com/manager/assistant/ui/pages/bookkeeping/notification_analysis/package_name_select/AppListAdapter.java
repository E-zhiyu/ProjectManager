package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.package_name_select;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppInfoViewHolder> {
    private final Context context;
    private final AppClickedListener listener;
    private List<AppInfo> appInfoList;    //应用列表

    public static class AppInfoViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView app_icon_view;                   //应用图标视图
        MaterialTextView app_name_text, package_name_text;  //应用名称和包名文本视图

        public AppInfoViewHolder(@NonNull View itemView) {
            super(itemView);

            app_icon_view = itemView.findViewById(R.id.app_icon_view);
            app_name_text = itemView.findViewById(R.id.app_name_text);
            package_name_text = itemView.findViewById(R.id.package_name_text);
        }
    }

    public interface AppClickedListener {
        /**
         * 应用视图被点击的回调方法
         *
         * @param package_name 被点击的应用的包名
         */
        void onAppClicked(String package_name);
    }

    public AppListAdapter(AppClickedListener listener, Context context) {
        this.appInfoList = new ArrayList<>();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public AppInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.view_holder_app_info, parent, false);
        return new AppInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppInfoViewHolder holder, int position) {
        AppInfo appInfo = appInfoList.get(position);
        String app_name = appInfo.getApp_name();
        String package_name = appInfo.getPackage_name();
        Bitmap app_icon = appInfo.getApp_icon();

        holder.app_name_text.setText(app_name);
        holder.package_name_text.setText(package_name);
        holder.app_icon_view.setImageBitmap(app_icon);

        holder.itemView.setOnClickListener(v -> listener.onAppClicked(package_name));   //绑定点击动作
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
    @SuppressLint("NotifyDataSetChanged")
    public void setAppInfoList(List<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
        notifyDataSetChanged();
    }
}
