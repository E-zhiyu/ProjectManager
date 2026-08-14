package com.sly.coffer.data.save.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(
        tableName = "pickedViews",
        indices = {
                @Index(value = {"viewId", "packageName", "activityName"}, unique = true)
        }
)
public class PickedViewEntity {
    @PrimaryKey(autoGenerate = true)
    private long Id;                //主键
    private String remark;          //备注
    @ColumnInfo(defaultValue = "")
    private String viewId;          //视图ID
    private String describeContent; //描述文本
    @ColumnInfo(defaultValue = "")
    private String packageName;     //应用包名
    private String appName;         //应用名称
    @ColumnInfo(defaultValue = "")
    private String activityName;    //活动名称
    private LocalDateTime dateTime; //拾取的时间

    public PickedViewEntity(String remark, String viewId, String describeContent, String packageName, String appName, String activityName, LocalDateTime dateTime) {
        this.remark = remark;
        this.viewId = viewId;
        this.describeContent = describeContent;
        this.packageName = packageName;
        this.appName = appName;
        this.activityName = activityName;
        this.dateTime = dateTime;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getDescribeContent() {
        return describeContent;
    }

    public void setDescribeContent(String describeContent) {
        this.describeContent = describeContent;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
