package com.sly.coffer.automation.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.sly.coffer.automation.broadcast.BroadcastActions;
import com.sly.coffer.auxiliary.classes.PickResult;
import com.sly.coffer.auxiliary.enums.LogTags;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;
import com.sly.coffer.data.save.db.services.AccessibilityRuleService;
import com.sly.coffer.helpers.AppListHelper;
import com.sly.coffer.helpers.accessibility.AccessibilityNodePicker;
import com.sly.coffer.ui.others.overlay.PickerOverlay;

import java.time.LocalDateTime;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressLint("AccessibilityPolicy")
public class PickAccessibilityService extends AccessibilityService {
    private final CompositeDisposable disposable = new CompositeDisposable();
    private PickerOverlay pickerOverlay;
    private String currentActivityName;
    private String currentPackageName;
    private BroadcastReceiver startReceiver = null;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = getServiceInfo();
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);

        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter(BroadcastActions.START_PICK.toString());
        startReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                if (BroadcastActions.START_PICK.toString().equals(intent.getAction())) {
                    Log.d(LogTags.PICK_ACCESSIBILITY_SERVICE.n(), "开启视图拾取模式");
                    startPicker();
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(startReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(this, startReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
        Log.d(LogTags.PICK_ACCESSIBILITY_SERVICE.n(), "注册广播接收器");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        //获取包名
        CharSequence packageName = event.getPackageName();
        if (packageName == null) return;
        currentPackageName = packageName.toString();
        Log.d(LogTags.AB_ACCESSIBILITY_SERVICE.n(), "包名：" + currentPackageName);

        //获取活动名
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence className = event.getClassName();
            if (className != null) {
                currentActivityName = className.toString();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();

        //注销广播接收器
        if (startReceiver != null) {
            unregisterReceiver(startReceiver);
            startReceiver = null;
        }
        Log.d(LogTags.PICK_ACCESSIBILITY_SERVICE.n(), "注销广播接收器");
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
                            this,
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
        //解析拾取数据
        String packageName = result.packageName;
        String activityName = result.activityName;
        String appName = AppListHelper.getAppNameByPackageName(packageName, this);
        String viewId = result.viewId;
        String content = result.content;

        Log.d(
                LogTags.PICK_ACCESSIBILITY_SERVICE.n(),
                "package = " + packageName
                        + "\nactivity = " + activityName
                        + "\nviewId = " + viewId
                        + "\ncontent = " + content
        );

        //保存视图信息
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(db.accessibilityRuleDao().getPickedViewCountSingle()
                .flatMap(count -> {
                    String remark = "视图" + (count + 1);
                    PickedViewEntity pickedView = new PickedViewEntity(
                            remark,
                            viewId,
                            content,
                            packageName,
                            appName,
                            activityName,
                            LocalDateTime.now()
                    );
                    return AccessibilityRuleService.addPickedView(pickedView, db);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        id -> {
                            //TODO:成功回调
                        },
                        e -> Log.e(LogTags.PICK_ACCESSIBILITY_SERVICE.n(), "拾取视图保存失败")
                )
        );
    }
}
