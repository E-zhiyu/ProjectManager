package com.manager.assistant.helpers.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * 静态权限帮助器
 */
public class PermissionHelper {
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
     * 申请通知使用权
     *
     * @param context 上下文
     */
    public static void requestNotificationListenerPermission(@NonNull Context context) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
                Toast.makeText(context, "您的设备不支持直接跳转，请前往自启动管理页面为本应用授权", Toast.LENGTH_SHORT).show();
            }
            context.startActivity(intent);
        } catch (Exception e) {
            //如果出现异常，跳转到设置
            intent.setAction(Settings.ACTION_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
    }

    /**
     * 判断是否已经忽略电池优化
     *
     * @param context 上下文
     * @return 是否忽略电池优化
     */
    public static boolean isIgnoringBatteryOptimizations(@NonNull Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return false;
    }

    /**
     * 打开电池优化界面
     *
     * @param context 上下文
     */
    public static void requestIgnoringBatteryOptimizations(@NonNull Context context) {
        boolean hasIgnored = isIgnoringBatteryOptimizations(context);
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
        if (!hasIgnored) {
            Toast.makeText(context, "请将本应用的优化策略改为“无限制”\n(提示：开关按钮左侧是可点击的)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "电池优化策略已为“无限制”，无需更改", Toast.LENGTH_SHORT).show();
        }
    }
}
