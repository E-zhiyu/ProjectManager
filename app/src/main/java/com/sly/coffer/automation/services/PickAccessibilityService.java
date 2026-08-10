package com.sly.coffer.automation.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.sly.coffer.auxiliary.classes.PickResult;
import com.sly.coffer.auxiliary.enums.LogTags;
import com.sly.coffer.helpers.accessibility.AccessibilityNodePicker;
import com.sly.coffer.ui.others.overlay.PickerOverlay;

@SuppressLint("AccessibilityPolicy")
public class PickAccessibilityService extends AccessibilityService {
    private PickerOverlay pickerOverlay;
    private String currentActivityName;
    private String currentPackageName;

    public String getCurrentActivityName() {
        return currentActivityName;
    }

    public String getCurrentPackageName() {
        return currentPackageName;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = getServiceInfo();
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        //获取包名
        CharSequence packageName = event.getPackageName();
        if (packageName == null) return;
        String pkgName = packageName.toString();
        Log.d(LogTags.AB_ACCESSIBILITY_SERVICE.n(), "包名：" + pkgName);

        //获取活动名
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence className = event.getClassName();
            if (className != null) {
                currentActivityName = className.toString();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * 开始拾取模式
     */
    public void startPicker() {
        if (pickerOverlay != null) {
            return;
        }

        pickerOverlay = new PickerOverlay(
                this,
                (x, y) -> {
                    PickResult result = AccessibilityNodePicker.pick(
                            PickAccessibilityService.this,
                            x,
                            y
                    );

                    if (result != null) {
                        result.packageName = currentPackageName;
                        result.activityName = currentActivityName;
                        onNodePicked(result);
                    }

                    stopPicker();
                });

        pickerOverlay.show();
    }

    /**
     * 结束拾取模式
     */
    public void stopPicker() {
        if (pickerOverlay != null) {
            pickerOverlay.dismiss();
            pickerOverlay = null;
        }
    }

    /**
     * 用户成功拾取一个节点
     */
    private void onNodePicked(@NonNull PickResult result) {
        Log.d(
                "AccountingPicker",
                "package = " + result.packageName
                        + "\nactivity = " + result.activityName
                        + "\nviewId = " + result.viewId
        );

        // TODO:
        // 把 result 返回给你的记账规则编辑页面
        //
        // 例如保存：
        //
        // packageName
        // activityName
        // viewId
        // className
        //
        // 后续自动记账时：
        // root.findAccessibilityNodeInfosByViewId(viewId)
        //
        // 然后读取 text。
    }
}
