package com.sly.coffer.data.save.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(
        tableName = "pickedPages",
        indices = {
                @Index(value = {"packageName", "activityName"}, unique = true)
        }
)
public class PickedPageEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;                //主键
    private String remark;          //备注
    @ColumnInfo(defaultValue = "")
    private String packageName;     //应用包名
    @ColumnInfo(defaultValue = "")
    private String activityName;    //活动名称
    private LocalDateTime dateTime; //拾取的时间

    public PickedPageEntity(String remark, String packageName, String activityName, LocalDateTime dateTime) {
        this.remark = remark;
        this.packageName = packageName;
        this.activityName = activityName;
        this.dateTime = dateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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
