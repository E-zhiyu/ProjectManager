package com.project.manager.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.project.manager.R;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class PackageNameHelper {
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

    /**
     * 动态申请应用列表权限（此方法只能在主线程调用）
     *
     * @param activity 申请权限的Activity，申请结果通过onRequestPermissionsResult()方法获取
     */
    public static void getPermission(@NonNull Activity activity) {
        try {
            PermissionInfo permissionInfo = activity.getPackageManager().getPermissionInfo("com.android.permission.GET_INSTALLED_APPS", 0);
            if (permissionInfo != null && permissionInfo.packageName.equals("com.lbe.security.miui")) {
                //MIUI 系统支持动态申请该权限
                if (ContextCompat.checkSelfPermission(activity, "com.android.permission.GET_INSTALLED_APPS") != PackageManager.PERMISSION_GRANTED) {
                    //没有权限，需要申请
                    ActivityCompat.requestPermissions(activity,
                            new String[]{"com.android.permission.GET_INSTALLED_APPS"},
                            RequestResultCode.REQUEST_GET_PERMISSION.ordinal()
                    );
                }
            } else {
                //其他系统的动态申请逻辑
                if (isRuntimePermissionEnable(activity)) {
                    if (ContextCompat.checkSelfPermission(activity, "com.android.permission.GET_INSTALLED_APPS") != PackageManager.PERMISSION_GRANTED) {
                        //没有权限，需要申请
                        ActivityCompat.requestPermissions(activity,
                                new String[]{"com.android.permission.GET_INSTALLED_APPS"},
                                RequestResultCode.REQUEST_GET_PERMISSION.ordinal()
                        );
                    }
                } else {
                    //不能动态申请则需要手动授权
                    Toast.makeText(activity, "您的系统不支持动态申请应用列表权限，请手动授权并重启应用", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHelper.showExceptionDialog(activity, e);
            Toast.makeText(activity, "应用列表权限申请失败，请手动授权并重启应用", Toast.LENGTH_SHORT).show();
        }
    }

    //判断是否支持动态权限申请
    private static boolean isRuntimePermissionEnable(@NonNull Activity activity) {
        return Settings.Secure.getInt(activity.getContentResolver(),
                "oem_installed_apps_runtime_permission_enable", 0) > 0;
    }
}
