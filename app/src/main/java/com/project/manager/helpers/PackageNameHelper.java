package com.project.manager.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.project.manager.R;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class PackageNameHelper {
    /**
     * 加载应用列表
     *
     * @return 包含应用信息的列表
     */
    @NonNull
    public static List<AppInfo> getInstalledApps(boolean isSysAppIncluded, @NonNull Context context) {
        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            if (!isSysAppIncluded && ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
                continue;   //动态排除系统应用

            String appName = pm.getApplicationLabel(app).toString();    //获取应用名称
            String packageName = app.packageName;                       //获取包名
            Drawable originIcon;                                        //获取应用图标（Drawable）
            try {
                originIcon = pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                Toast.makeText(context, "获取应用图标时出错", Toast.LENGTH_SHORT).show();
                originIcon = AppCompatResources.getDrawable(context, R.mipmap.unknown_app_ic_channel);
            }

            Drawable scaledIcon = ImageHelper.resizeIcon(originIcon, 48, context);
            AppInfo appInfo = new AppInfo(appName, packageName, scaledIcon);
            appInfoList.add(appInfo);
        }

        return appInfoList;
    }

    /**
     * 在完整应用列表中搜索匹配项
     *
     * @param query           搜索内容
     * @param fullAppInfoList 完整的应用列表
     * @return 匹配的应用组成的应用列表
     */
    @NonNull
    public static List<AppInfo> searchInFullAppList(String query, @NonNull List<AppInfo> fullAppInfoList) {
        List<AppInfo> searchResult = new ArrayList<>();

        for (AppInfo app : fullAppInfoList) {
            String app_name = app.getApp_name();
            if (app_name.toLowerCase().contains(query.toLowerCase()))
                searchResult.add(app);
        }

        return searchResult;
    }
}
