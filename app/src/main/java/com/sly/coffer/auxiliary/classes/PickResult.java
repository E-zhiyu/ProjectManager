package com.sly.coffer.auxiliary.classes;

import android.graphics.Rect;

import androidx.annotation.NonNull;

public class PickResult {
    public float x;
    public float y;
    public String packageName;
    public String activityName;
    public String viewId;
    public String className;
    public String text;
    public String contentDescription;
    public boolean clickable;
    public boolean enabled;
    public boolean visible;
    public Rect bounds;

    @NonNull
    @Override
    public String toString() {
        return "PickResult{" +
                "x=" + x +
                ", y=" + y +
                ", packageName='" + packageName + '\'' +
                ", activityName='" + activityName + '\'' +
                ", viewId='" + viewId + '\'' +
                ", className='" + className + '\'' +
                ", text='" + text + '\'' +
                ", contentDescription='" +
                contentDescription + '\'' +
                ", bounds=" + bounds +
                '}';
    }
}
