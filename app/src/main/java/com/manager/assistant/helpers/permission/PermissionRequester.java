package com.manager.assistant.helpers.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 在打开Activity时申请权限的工具类
 */
public class PermissionRequester {
    private final ComponentActivity activity;   //需要申请权限的Activity
    private final List<String> runtimePermissions = new ArrayList<>();      //运行时权限列表
    private final Queue<SpecialRequest> specialQueue = new LinkedList<>();  //特殊权限队列
    private ActivityResultLauncher<String[]> runtimeLauncher;   //申请运行时权限的启动器

    public enum SpecialType {
        ALARM(
                ctx -> Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ctx.getSystemService(android.app.AlarmManager.class).canScheduleExactAlarms(),
                ctx -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + ctx.getPackageName()));
                    }
                    return null;
                }
        ),
        @SuppressLint("BatteryLife") BATTERY(
                ctx -> ((android.os.PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(ctx.getPackageName()),
                ctx -> new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        );
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
    public PermissionRequester(@NonNull ComponentActivity activity) {
        this(activity, null);
    }

    /**
     * 支持自定义 Launcher 的构造方法
     *
     * @param customLauncher 如果传入 null，则使用默认的注册逻辑
     */
    public PermissionRequester(@NonNull ComponentActivity activity,
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
                    //批量运行时权限回调后，自动进入特殊权限流程
                    processNextSpecial();
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
        if (runtimeLauncher == null) {
            throw new IllegalStateException("RuntimeLauncher has not been initialized. " +
                    "Ensure it was passed in constructor or registered before Activity started.");
        }

        List<String> deniedPermissions = new ArrayList<>();
        for (String p : runtimePermissions) {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(p);
            }
        }

        if (!deniedPermissions.isEmpty()) {
            runtimeLauncher.launch(deniedPermissions.toArray(new String[0]));
        } else {
            processNextSpecial();
        }
    }

    /**
     * 处理队下一个特殊应用权限
     */
    public void processNextSpecial() {
        if (specialQueue.isEmpty()) return;

        SpecialRequest request = specialQueue.poll();
        if (request == null) {
            return;
        }

        handleSpecialPermission(request);
    }

    /**
     * 处理特殊应用权限
     *
     * @param request 权限请求
     */
    private void handleSpecialPermission(@NonNull SpecialRequest request) {
        SpecialType type = (SpecialType) request.permission;
        if (type.isGranted(activity)) {
            processNextSpecial();
            return;
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle(request.customTitle)
                .setMessage(request.customMessage)
                .setPositiveButton("去设置", (d, w) -> activity.startActivity(type.getIntent(activity)))
                .setNegativeButton("取消", (d, w) -> processNextSpecial())
                .setCancelable(false)
                .show();
    }
}