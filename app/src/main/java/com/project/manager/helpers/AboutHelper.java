package com.project.manager.helpers;

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

import io.noties.markwon.Markwon;

public class AboutHelper {
    private static final String about_md = "这是一款极简记账工具，秉持“简洁无广、隐私保护”的核心理念，为用户提供清爽、安全的记账体验  \n\n" +
            "### 下载链接：  \n" +
            "- **[小飞机网盘](https://share.feijipan.com/s/kTVc2PiI)**(免登录)  \n" +
            "- **[123云盘](https://www.123865.com/s/C5xcVv-kRYT3)**(需要登录)  \n\n" +
            "### 联系作者：  \n" +
            "- **[酷安@E_zhiyu](http://www.coolapk.com/u/36112159)**  \n" +
            "- **[GitHub@E-zhiyu](https://github.com/E-zhiyu)**  \n\n";

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
    public static int getVersionCode(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        return packageInfo.versionCode;
    }

    /**
     * 获取应用名称
     *
     * @param context 上下文
     * @return 应用名称
     * @throws PackageManager.NameNotFoundException 包名未找到引发的异常
     */
    @NonNull
    public static String getAppName(@NonNull Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                context.getPackageName(),
                PackageManager.GET_META_DATA
        );
        return packageManager.getApplicationLabel(applicationInfo).toString();
    }

    /**
     * 显示关于软件对话框
     *
     * @param context 上下文
     */
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
                .inflate(R.layout.view_markdown_text, null);
        MaterialTextView about_text = about_dialog.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(about_text, about_md);

        new MaterialAlertDialogBuilder(context)
                .setTitle(app_name + " " + version_name)
                .setView(about_dialog)
                .setPositiveButton("关闭", ((dialog, which) -> dialog.dismiss()))
                .show();
    }
}
