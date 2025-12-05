package com.project.manager.helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.project.manager.ui.RequestResultCode;

/**
 * 与应用权限有关的帮助器
 */
public class PermissionHelper {
    /**
     * 动态申请应用列表权限（此方法只能在主线程调用）
     *
     * @param activity 申请权限的Activity，申请结果通过onRequestPermissionsResult()方法获取
     */
    public static void getAppListPermission(@NonNull Activity activity) {
        try {
            if (ContextCompat.checkSelfPermission(activity, "com.android.permission.GET_INSTALLED_APPS") == PackageManager.PERMISSION_GRANTED) {
                return;
            }

            PermissionInfo permissionInfo = activity.getPackageManager().getPermissionInfo("com.android.permission.GET_INSTALLED_APPS", 0);
            String permissionInfoPackageName = permissionInfo.packageName;
            if (permissionInfoPackageName.equals("com.lbe.security.miui") || permissionInfoPackageName.equals("oplus")) {
                //MIUI 系统支持动态申请该权限
                ActivityCompat.requestPermissions(activity,
                        new String[]{"com.android.permission.GET_INSTALLED_APPS"},
                        RequestResultCode.REQUEST_GET_PERMISSION.ordinal()
                );
            } else {
                //其他系统的动态申请逻辑
                if (isRuntimeAppListPermissionEnable(activity)) {
                    //没有权限，需要申请
                    ActivityCompat.requestPermissions(activity,
                            new String[]{"com.android.permission.GET_INSTALLED_APPS"},
                            RequestResultCode.REQUEST_GET_PERMISSION.ordinal()
                    );
                } else {
                    //不能动态申请则需要手动授权
                    Toast.makeText(activity, "您的系统不支持动态申请应用列表权限，请手动授权并重启应用", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHelper.showExceptionDialog(activity, e);
            Toast.makeText(activity, "应用列表权限申请失败，请手动授权并重启应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断是否支持动态应用列表权限申请
     *
     * @param activity 活动类
     * @return 是否支持动态应用列表权限申请
     */
    private static boolean isRuntimeAppListPermissionEnable(@NonNull Activity activity) {
        return Settings.Secure.getInt(activity.getContentResolver(),
                "oem_installed_apps_runtime_permission_enable", 0) > 0;
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
     * 申请通知使用权
     *
     * @param context 上下文
     */
    public static void requestNotificationPermission(@NonNull Context context) {
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
    public static void openBatteryOptimizations(@NonNull Context context) {
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
