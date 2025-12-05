package com.project.manager.data.data_class;

import android.graphics.Bitmap;

//软件信息数据类
public class AppInfo {
    private final String app_name;        //软件名称
    private final String package_name;    //包名
    private final Bitmap app_icon;      //软件图标资源

    public AppInfo(String app_name, String package_name, Bitmap app_icon) {
        this.app_icon = app_icon;
        this.app_name = app_name;
        this.package_name = package_name;
    }

    public String getApp_name() {
        return app_name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public Bitmap getApp_icon() {
        return app_icon;
    }
}
