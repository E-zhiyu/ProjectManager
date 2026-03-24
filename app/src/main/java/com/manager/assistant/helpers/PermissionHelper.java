package com.manager.assistant.helpers;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.generic_enums.LogTags;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 在打开Activity时申请权限的工具类
 */
public class PermissionHelper {
    private final ComponentActivity activity;   //需要申请权限的Activity
    private final List<String> runtimePermissions = new ArrayList<>();      //运行时权限列表
    private final Queue<SpecialRequest> specialQueue = new LinkedList<>();  //特殊权限队列
    private ActivityResultLauncher<String[]> runtimeLauncher;   //申请运行时权限的启动器
    private boolean isSpecialProcessing = false;                //是否正在处理特殊权限，防止在处理权限时重复调用权限申请方法

    public enum SpecialType {
        //精确闹钟权限
        ALARM(
                ctx -> Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ctx.getSystemService(android.app.AlarmManager.class).canScheduleExactAlarms(),
                ctx -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + ctx.getPackageName()));
                    }
                    return null;
                }
        ),
        //电池优化
        @SuppressLint("BatteryLife") BATTERY(
                ctx -> ((android.os.PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(ctx.getPackageName()),
                ctx -> new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        ),
        //通知监听权限
        NOTIFICATION_LISTENER(
                PermissionHelper::isNotificationServiceEnabled,
                c -> {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    return intent;
                }
        ),
        //自启动权限
        AUTO_START(context -> false, context -> null);
        private final Checker checker;
        private final IntentBuilder intentBuilder;

        SpecialType(Checker c, IntentBuilder i) {
            this.checker = c;
            this.intentBuilder = i;
        }

        boolean isGranted(Context c) {
            return checker.check(c);
        }

        Intent getIntent(Context c) {
            return intentBuilder.build(c);
        }

        interface Checker {
            boolean check(Context c);
        }

        interface IntentBuilder {
            Intent build(Context c);
        }
    }

    private static class SpecialRequest {
        Object permission; // 可以是 String (运行时) 或 SpecialType (特殊)
        String customTitle;
        String customMessage;

        SpecialRequest(Object permission, String title, String message) {
            this.permission = permission;
            this.customTitle = title;
            this.customMessage = message;
        }
    }

    /**
     * 默认构造方法：内部自动注册 Launcher
     */
    public PermissionHelper(@NonNull ComponentActivity activity) {
        this(activity, null);
    }

    /**
     * 支持自定义 Launcher 的构造方法
     *
     * @param customLauncher 如果传入 null，则使用默认的注册逻辑
     */
    public PermissionHelper(@NonNull ComponentActivity activity,
                            @Nullable ActivityResultLauncher<String[]> customLauncher) {
        this.activity = activity;

        if (customLauncher != null) {
            this.runtimeLauncher = customLauncher;
        } else {
            // 只有在没有提供自定义 Launcher 时才在内部 register
            initDefaultLauncher();
        }
    }

    /**
     * 初始化默认运行时权限申请启动器
     */
    private void initDefaultLauncher() {
        this.runtimeLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    //如果运行时权限被拒绝了，那就不要继续申请
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            runtimePermissions.remove(entry.getKey());
                        }
                    }
                }
        );
    }

    /**
     * 添加权限请求（使用自定义文案）
     *
     * @param permission 需要申请的权限（类型为{@link String}表示运行时权限，{@link SpecialType}表示特殊应用权限
     * @param title      对话框标题
     * @param message    提示文本
     */
    public void addPermission(SpecialType permission, String title, String message) {
        specialQueue.add(new SpecialRequest(permission, title, message));
    }

    /**
     * 添加权限申请请求
     *
     * @param permission 运行时权限
     */
    public void addPermission(String permission) {
        runtimePermissions.add(permission);
    }

    /**
     * 开始申请权限
     */
    public void start() {
        //检查是否正在处理权限，如果是则直接结束
        if (isSpecialProcessing) {
            return;
        }

        if (runtimeLauncher == null) {
            throw new IllegalStateException("RuntimeLauncher has not been initialized. " +
                    "Ensure it was passed in constructor or registered before Activity started.");
        }

        //筛选没有授权的运行时权限
        List<String> deniedPermissions = new ArrayList<>();
        for (String p : runtimePermissions) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(p);
            }
        }

        //先处理运行时权限，再处理特殊应用权限
        if (!deniedPermissions.isEmpty()) {
            runtimeLauncher.launch(deniedPermissions.toArray(new String[0]));
        } else {
            processNextSpecial();
        }
    }

    /**
     * 处理队下一个特殊应用权限
     */
    private void processNextSpecial() {
        if (specialQueue.isEmpty()) {
            Log.i(LogTags.PERMISSION_HELPER.getV(), "特殊应用权限申请完毕");
            return;
        }

        //从队列中取出一个特殊应用权限请求
        Log.d(LogTags.PERMISSION_HELPER.getV(), "正在处理下个特殊应用权限");
        isSpecialProcessing = true;         //标记为正在处理
        SpecialRequest request = specialQueue.poll();
        if (request == null) {
            isSpecialProcessing = false;    //如果没有特殊权限了，那就标记为未处理
            return;
        }

        //处理该权限
        handleSpecialPermission(request);
    }

    /**
     * 处理特殊应用权限
     *
     * @param request 权限请求
     */
    private void handleSpecialPermission(@NonNull SpecialRequest request) {
        SpecialType type = (SpecialType) request.permission;
        Log.d(LogTags.PERMISSION_HELPER.getV(), request.customTitle);
        if (type == SpecialType.AUTO_START) {
            //单独处理自启动权限
            if (!AutoBookKeepingPreference.getHintAutoStart(activity)) {
                AutoBookKeepingPreference.setHintAutoStart(true, activity);

                //弹出提示框
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(request.customTitle)
                        .setMessage(request.customMessage)
                        .setNegativeButton(
                                "取消",
                                (dialog, which) -> processNextSpecial()
                        )
                        .setPositiveButton(
                                "前往设置",
                                (dialog, which) -> {
                                    isSpecialProcessing = false;    //未直接调用processNextSpecial()，需要标记为未处理
                                    PermissionHelper.requestAutoStartPermission(activity);
                                }
                        )
                        .setCancelable(false)
                        .show();
            } else {
                processNextSpecial();
            }
        } else if (type.isGranted(activity)) {
            processNextSpecial();
        } else {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(request.customTitle)
                    .setMessage(request.customMessage)
                    .setPositiveButton("去设置", (d, w) -> {
                        isSpecialProcessing = false;    //未直接调用processNextSpecial()，需要标记为未处理
                        activity.startActivity(type.getIntent(activity));
                    })
                    .setNegativeButton("取消", (d, w) -> processNextSpecial())
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * 检查通知使用权的授予情况
     *
     * @param context 上下文
     * @return 是否授予通知使用权
     */
    public static boolean isNotificationServiceEnabled(@NonNull Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查并引导用户开启自启动权限
     *
     * @param context 上下文
     */
    public static void requestAutoStartPermission(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // 根据设备厂商跳转到不同的设置页面
            if (manufacturer.contains("xiaomi")) {
                // 小米设备
                intent.setComponent(new ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else {
                //其他设备跳转到设置界面
                intent.setAction(Settings.ACTION_SETTINGS);
                Toast.makeText(context, "请前往自启动管理页进行授权", Toast.LENGTH_SHORT).show();
            }
            context.startActivity(intent);
        } catch (Exception e) {
            //如果出现异常，跳转到设置
            intent.setAction(Settings.ACTION_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
    }
}