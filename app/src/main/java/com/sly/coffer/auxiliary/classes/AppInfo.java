package com.sly.coffer.auxiliary.classes;

import android.graphics.Bitmap;

//软件信息数据类
public class AppInfo {
    private final String appName;       //软件名称
    private final String packageName;   //包名
    private final Bitmap appIcon;       //软件图标资源

    public AppInfo(String appName, String packageName, Bitmap appIcon) {
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Bitmap getAppIcon() {
        return appIcon;
    }
}
