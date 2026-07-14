package com.manager.assistant.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.manager.assistant.auxiliary.classes.AppInfo;
import com.manager.assistant.helpers.appearence.IconHelper;

import java.util.ArrayList;
import java.util.List;

public class AppListHelper {
    /**
     * 加载应用列表
     *
     * @param isSysAppIncluded 是否包含系统应用
     * @param context          上下文
     * @return 读取到的应用信息列表
     */
    @NonNull
    public static List<AppInfo> getInstalledApps(boolean isSysAppIncluded, @NonNull Context context) {
        List<AppInfo> appInfoList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        @SuppressLint("QueryPermissionsNeeded")
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            if (!isSysAppIncluded && ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
                continue;   //动态排除系统应用

            String appName = app.loadLabel(pm).toString();              //获取应用名称
            String packageName = app.packageName;                       //获取包名
            Drawable originDrawable = app.loadIcon(pm);                 //获取应用图标

            //转换为Bitmap并缩放
            Bitmap scaledBitmap = IconHelper.getRoundedCornerIcon(context, originDrawable);
            AppInfo appInfo = new AppInfo(appName, packageName, scaledBitmap);
            appInfoList.add(appInfo);
        }

        return appInfoList;
    }
}
