package com.sly.coffer.automation.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sly.coffer.automation.broadcast.BroadcastActions;
import com.sly.coffer.auxiliary.classes.CustomDateTimeFormatter;
import com.sly.coffer.auxiliary.classes.PickResult;
import com.sly.coffer.auxiliary.enums.LogTags;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;
import com.sly.coffer.data.save.db.services.AccessibilityRuleService;
import com.sly.coffer.helpers.accessibility.AccessibilityNodePicker;
import com.sly.coffer.ui.others.overlay.PickerOverlay;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            // 2. 从事件中获取包名和类名
            CharSequence className = event.getClassName();

            if (className != null) {
                // 3. 组合成完整的组件名
                ComponentName componentName = new ComponentName(
                        packageName.toString(),
                        className.toString()
                );

                // 这就是当前活动的完整名称 (例如: com.example.app/.MainActivity)
                if (isActivity(componentName)) {
                    currentActivityName = componentName.flattenToShortString();
                }
            }
        }
    }

    /**
     * 判断是否为活动名称
     *
     * @param componentName 待判断的字符串
     * @return 是否为活动名称
     */
    private boolean isActivity(ComponentName componentName) {
        try {
            // 尝试通过PackageManager获取Activity信息
            getPackageManager().getActivityInfo(componentName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false; // 说明这个组件不是Activity
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

                    //判断是否点击到有内容的视图
                    if (!isResultUsableAndNotify(result)) {
                        return;
                    }

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
     * 判断拾取结果是否可用，不可用则发送提醒
     *
     * @param result 拾取结果
     * @return 该结果是否可用
     */
    private boolean isResultUsableAndNotify(@Nullable PickResult result) {
        if (result == null) {
            Toast.makeText(this, "无法拾取视图，请重试", Toast.LENGTH_SHORT).show();
            return false;
        } else if (result.viewId == null || result.viewId.isEmpty()) {
            Toast.makeText(this, "该视图没有编号，请重新选择", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Pattern numPattern = Pattern.compile("\\d");
            Matcher matcher = numPattern.matcher(result.content);
            if (matcher.find()) {
                return true;
            } else {
                Toast.makeText(this, "视图文本中不含数字，请重新选择", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
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
        String viewId = result.viewId;
        String content = result.content;

        Log.d(
                LogTags.PICK_ACCESSIBILITY_SERVICE.n(),
                "package = " + packageName
                        + "\nactivity = " + activityName
                        + "\nviewId = " + viewId
                        + "\ncontent = " + content
        );

        //实例化数据实体
        LocalDateTime time = LocalDateTime.now();
        PickedViewEntity pickedView = new PickedViewEntity(
                "视图 · " + time.format(CustomDateTimeFormatter.DATE_TIME),
                viewId,
                content,
                packageName,
                activityName,
                time
        );

        //保存视图信息
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(AccessibilityRuleService.addPickedView(pickedView, db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        id -> Toast.makeText(this, "已保存拾取的视图", Toast.LENGTH_SHORT).show(),
                        e -> Log.e(LogTags.PICK_ACCESSIBILITY_SERVICE.n(), "拾取视图保存失败")
                )
        );
    }
}
