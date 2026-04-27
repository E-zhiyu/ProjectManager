package com.manager.assistant.helpers;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.LifecycleManager;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.generic_enums.LogTags;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * 在打开Activity时申请权限的工具类
 */
public class PermissionHelper {
    private final ComponentActivity activity;   //需要申请权限的Activity
    private final List<String> runtimePermissions = new ArrayList<>();          //运行时权限列表
    private final List<String> rationaleRuntimePermissions = new ArrayList<>(); //可以再次申请的运行时权限
    private final Queue<PermissionRequest> specialQueue = new LinkedList<>();   //特殊权限队列
    private ActivityResultLauncher<String[]> runtimeLauncher;   //申请运行时权限的启动器
    private boolean isProcessing = false;                       //是否正在处理权限，防止在处理权限时重复调用权限申请方法

    public enum SpecialPermissionType {
        //精确闹钟权限
        ALARM(
                PermissionHelper::isExactAlarmEnabled,
                PermissionHelper::buildExactAlarmIntent
        ),
        //电池优化
        @SuppressLint("BatteryLife") BATTERY(
                PermissionHelper::isIgnoringBatteryOptimizations,
                PermissionHelper::buildIgnoringBatteryOptimizationsIntent
        ),
        //通知监听权限
        NOTIFICATION_LISTENER(
                PermissionHelper::isNotificationServiceEnabled,
                c -> buildNotificationListenerIntent()
        ),
        //自启动权限
        AUTO_START(
                context -> false,
                PermissionHelper::buildAutoStartPermissionIntent
        );
        private final Checker checker;              //如何检查权限是否授予
        private final IntentBuilder intentBuilder;  //跳转权限界面所需的Intent构建器

        SpecialPermissionType(Checker c, IntentBuilder i) {
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

    private static class PermissionRequest {
        Object permission;      //权限（可以是运行时权限字符串，也可以是SpecialPermissionType
        String customTitle;     //对话框标题
        String customMessage;   //自定义对话框消息

        PermissionRequest(Object permission, String title, String message) {
            this.permission = permission;
            this.customTitle = title;
            this.customMessage = message;
        }

        PermissionRequest(Object permission, String message) {
            this(permission, "", message);
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
                        String permission = entry.getKey();

                        runtimePermissions.remove(permission);
                        if (!entry.getValue() && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                            //如果用户没有授权，但是又可以再次申请，说明没有被永久拒绝，转移到再次申请列表中
                            rationaleRuntimePermissions.add(permission);
                        } else {
                            //如果用户没有授权，但是又不能再次申请，则说明权限被永久拒绝，不要再申请，完全删除这个权限
                            rationaleRuntimePermissions.remove(permission);
                        }
                    }
                }
        );
    }

    /**
     * 添加权限请求（使用自定义文案）
     *
     * @param permission 需要申请的权限（类型为{@link String}表示运行时权限，{@link SpecialPermissionType}表示特殊应用权限
     * @param title      对话框标题
     * @param message    提示文本
     */
    public void addPermission(@NonNull SpecialPermissionType permission, String title, String message) {
        if (!permission.isGranted(activity)) {
            specialQueue.add(new PermissionRequest(permission, title, message));
        }
    }

    /**
     * 添加权限申请请求
     *
     * @param permission 运行时权限字符串
     * @param message    用户拒绝过一次后，再次申请前显示的解释对话框的提示信息
     */
    public void addPermission(String permission, String message) {
        if (!isRuntimePermissionGranted(permission, activity)) {
            runtimePermissions.add(permission);
        }
    }

    /**
     * 开始申请权限
     */
    public void start() {
        //检查是否正在处理权限，如果是则直接结束
        if (isProcessing) {
            return;
        }

        if (runtimeLauncher == null) {
            throw new IllegalStateException("RuntimeLauncher has not been initialized. " +
                    "Ensure it was passed in constructor or registered before Activity started.");
        }

        //筛选没有授权的运行时权限
        List<String> deniedPermissions = runtimePermissions.stream()
                .filter(permission -> !isRuntimePermissionGranted(permission, activity))
                .collect(Collectors.toList());

        //先处理运行时权限，再处理需要解释一次的运行时权限，最后处理特殊应用权限
        if (!deniedPermissions.isEmpty()) {
            runtimeLauncher.launch(deniedPermissions.toArray(new String[0]));
        } else if (!rationaleRuntimePermissions.isEmpty()) {
            showRationaleDialog(rationaleRuntimePermissions.toArray(new String[0]));
        } else {
            processNextSpecial();
        }
    }

    /**
     * 处理队下一个特殊应用权限
     */
    private void processNextSpecial() {
        //判断特殊权限是否申请完毕
        if (specialQueue.isEmpty()) {
            Log.i(LogTags.PERMISSION_HELPER.getV(), "特殊应用权限申请完毕");
            return;
        }

        //从队列中取出一个特殊应用权限请求
        Log.d(LogTags.PERMISSION_HELPER.getV(), "正在处理下个特殊应用权限");
        isProcessing = true;         //标记为正在处理
        PermissionRequest request = specialQueue.poll();
        if (request == null) {
            isProcessing = false;    //如果没有特殊权限了，那就标记为未处理
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
    private void handleSpecialPermission(@NonNull PermissionRequest request) {
        SpecialPermissionType type = (SpecialPermissionType) request.permission;
        Log.d(LogTags.PERMISSION_HELPER.getV(), request.customTitle);
        if (type.isGranted(activity)) {
            processNextSpecial();
        } else {
            //以后不再提示申请自启动权限
            if (type == SpecialPermissionType.AUTO_START) {
                AutoBookKeepingPreference.setHintAutoStart(true, activity);
            }

            new MaterialAlertDialogBuilder(activity)
                    .setTitle(request.customTitle)
                    .setMessage(request.customMessage)
                    .setPositiveButton("去设置", (d, w) -> {
                        isProcessing = false;    //未直接调用processNextSpecial()，需要标记为未处理

                        LifecycleManager.startExternalActivity(activity, type.getIntent(activity));
                    })
                    .setNegativeButton("取消", (d, w) -> processNextSpecial())
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * 显示用户拒绝过一次的运行时权限解释对话框
     *
     * @param permissions 需要一次性申请的运行时权限
     */
    private void showRationaleDialog(String[] permissions) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle("需要权限")
                .setMessage("为了提供完整的功能，我们需要以下权限，请您授权。")
                .setPositiveButton("确定", (dialog, which) -> runtimeLauncher.launch(permissions))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 判断运行时权限是否已经授予
     *
     * @param permission 运行时权限字符串
     * @param context    上下文
     * @return 是否授予运行时权限
     */
    public static boolean isRuntimePermissionGranted(String permission, Context context) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查是否有精确闹钟权限
     *
     * @param context 上下文
     * @return 是否拥有精确闹钟权限
     */
    public static boolean isExactAlarmEnabled(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                context.getSystemService(android.app.AlarmManager.class).canScheduleExactAlarms();
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
     * 判断是否在电池优化白名单中
     *
     * @param context 上下文
     * @return 是否在电池优化白名单
     */
    public static boolean isIgnoringBatteryOptimizations(@NonNull Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    /**
     * 构建精确闹钟权限Intent
     *
     * @param context 上下文
     * @return 用于申请精确闹钟权限的Intent
     */
    @Nullable
    public static Intent buildExactAlarmIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + context.getPackageName())
            );
        }
        return null;
    }

    /**
     * 构建申请通知监听权限的Intent
     *
     * @return 申请通知监听权限的Intent
     */
    @NonNull
    public static Intent buildNotificationListenerIntent() {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 构建用于申请电池优化白名单的Intent
     *
     * @param context 上下文
     * @return 用于申请电池优化白名单的Intent
     */
    @NonNull
    public static Intent buildIgnoringBatteryOptimizationsIntent(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.contains("oppo") || manufacturer.contains("oneplus")) {
            @SuppressLint("BatteryLife") Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
            return intent;
        } else {
            return new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        }
    }

    /**
     * 构建跳转自启动界面的Intent
     *
     * @param context 上下文
     * @return 跳转自启动界面的Intent
     */
    @NonNull
    public static Intent buildAutoStartPermissionIntent(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.contains("xiaomi")) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            return intent;
        } else {
            Toast.makeText(context, "请手动前往自启动界面授权", Toast.LENGTH_SHORT).show();
            return new Intent(Settings.ACTION_SETTINGS);
        }
    }
}