package com.manager.assistant.helpers.about;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.manager.assistant.helpers.ExceptionHelper;

public class AboutHelper {
    /**
     * 获取版本名称
     *
     * @param context 上下文
     * @return 版本名称字符串
     * @throws PackageManager.NameNotFoundException 包名未找到引发的异常
     */
    public static String getVersionName(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionName;
    }

    /**
     * 获取当前版本代码
     *
     * @param context 上下文
     * @return 版本代码整数值
     * @throws PackageManager.NameNotFoundException 包名未找到引发的异常
     */
    public static long getVersionCode(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        return packageInfo.getLongVersionCode();
    }

    /**
     * 获取应用名称
     *
     * @param context 上下文
     * @return 应用名称
     */
    @NonNull
    public static String getAppName(@NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return "ManagerAssistant";
        }
    }
}
