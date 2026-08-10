package com.sly.coffer.data.save.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "accessibilityRules",
        indices = {
                @Index(value = "enabled")
        }
)
public class AccessibilityRuleEntity {
    @PrimaryKey(autoGenerate = true)
    private long ruleId;        //主键
    private String name;        //规则名称
    private int type;           //流水种类
    @ColumnInfo(defaultValue = "true")
    private boolean enabled;    //是否启用
    private String packageName; //应用包名
    private String targetActivity;  //目标 Activity 的名称（可选）
    private String viewId;      //金额视图的 ID
    private float posX;         //X轴上点击的位置
    private float posY;         //Y轴上点击的位置

    public AccessibilityRuleEntity(String name, int type, String packageName, String targetActivity, String viewId, float posX, float posY) {
        this.name = name;
        this.type = type;
        this.enabled = true;
        this.packageName = packageName;
        this.targetActivity = targetActivity;
        this.viewId = viewId;
        this.posX = posX;
        this.posY = posY;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(String targetActivity) {
        this.targetActivity = targetActivity;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }
}
