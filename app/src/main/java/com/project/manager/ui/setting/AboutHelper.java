package com.project.manager.ui.setting;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.exception.ExceptionHelper;

import io.noties.markwon.Markwon;

public class AboutHelper {
    private static final String about_message = "这是一个项目管理工具，为用户提供记账功能。  \n" +
            "查看源码：[GitHub](https://github.com/E-zhiyu/ManagerAssitant/releases)  \n" +
            "本软件基于GPL3.0协议开源";

    //获取版本名称
    public static String getVersionName(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionName;
    }

    //获取应用名称
    @NonNull
    public static String getAppName(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                context.getPackageName(),
                PackageManager.GET_META_DATA
        );
        return packageManager.getApplicationLabel(applicationInfo).toString();
    }

    //显示关于软件对话框
    public static void showAboutDialog(Context context) {
        String version_name = "UnknowVersion", app_name = "UnknowApp";
        try {
            version_name = "v" + getVersionName(context);
            app_name = getAppName(context);
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "无法获取版本名称或应用名称", Toast.LENGTH_SHORT).show();
        }

        //获取自定义弹窗视图
        View about_dialog = LayoutInflater.from(context)
                .inflate(R.layout.dialog_about, null);
        MaterialTextView about_text = about_dialog.findViewById(R.id.about_text);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(about_text, about_message);

        new MaterialAlertDialogBuilder(context)
                .setTitle(app_name + " " + version_name)
                .setView(about_dialog)
                .setPositiveButton("关闭", ((dialog, which) -> dialog.dismiss()))
                .show();
    }
}
