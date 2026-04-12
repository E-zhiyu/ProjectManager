package com.manager.assistant.ui.pages.setting;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.databinding.ActivityPermissionManageBinding;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingClickableTextView;

import io.noties.markwon.Markwon;

public class PermissionManageActivity extends AppCompatActivity {
    private ActivityPermissionManageBinding binding;                //绑定的XML视图
    private final ActivityResultLauncher<String> runtimeLauncher =  //申请运行时权限的启动器
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    o -> {
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPermissionManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.scrollView.setPadding(
                    0,
                    0,
                    0,
                    systemBars.bottom + ViewEdgeHelper.dpToPx(this, 15)
            );
            return insets;
        });

        initViews();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //相机权限
        SettingClickableTextView camera = new SettingClickableTextView(
                this,
                binding.cameraOption,
                R.string.camera_permission,
                "允许使用摄像头",
                R.drawable.baseline_photo_camera_24
        );
        camera.setFunctionListener(v -> showExplanationDialog(
                R.string.camera_permission,
                "该权限允许应用调用摄像头，应用范围如下：\n" +
                        "- 添加流水记录图片时使用内置拍照功能拍照\n",
                () -> requestRuntimePermission(Manifest.permission.CAMERA)
        ));

        //应用列表权限
        SettingClickableTextView appList = new SettingClickableTextView(
                this,
                binding.appListOption,
                R.string.app_list_option,
                "允许获取应用列表",
                R.drawable.outline_apps_24
        );
        appList.setFunctionListener(v -> showExplanationDialog(
                R.string.app_list_option,
                "该权限允许应用读取应用列表，应用范围如下：\n" +
                        "- 在输入通知解析规则时读取应用列表以便快速输入包名\n",
                () -> requestRuntimePermission("com.android.permission.GET_INSTALLED_APPS")
        ));

        //通知权限
        SettingClickableTextView notification = new SettingClickableTextView(
                this,
                binding.notificationOption,
                R.string.notification_permission,
                "允许发送通知",
                R.drawable.outline_notification_settings_24
        );
        notification.setFunctionListener(v -> showExplanationDialog(
                        R.string.notification_permission,
                        "该权限允许应用发送通知，应用范围如下：\n" +
                                "- 预算余额低时发送提醒通知\n" +
                                "- 触发自动记账后发送通知决定是否保留\n",
                        () -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestRuntimePermission(Manifest.permission.POST_NOTIFICATIONS);
                            } else {
                                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
        );

        //通知监听权限
        SettingClickableTextView notificationListener = new SettingClickableTextView(
                this,
                binding.notificationListenerOption,
                R.string.notification_listener_permission,
                "允许监听其他应用的通知",
                R.drawable.outline_notifications_active_24
        );
        notificationListener.setFunctionListener(v -> showExplanationDialog(
                R.string.notification_listener_permission,
                "该权限允许应用读取其他应用发送的通知，本应用不会利用该权限获取用户隐私。该权限应用范围如下：\n" +
                        "- 读取其他应用的通知实现自动记账\n",
                () -> requestSpecialPermission(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, false)
        ));

        //自启动权限
        SettingClickableTextView autoStart = new SettingClickableTextView(
                this,
                binding.autoStartOption,
                R.string.auto_start_permission,
                "允许在后台启动服务",
                R.drawable.outline_autorenew_24
        );
        autoStart.setFunctionListener(v -> showExplanationDialog(
                        R.string.auto_start_permission,
                        "该权限是定制安卓中特有的权限，其允许应用在后台启动服务，应用范围如下：\n" +
                                "- 在退出应用后自动启动通知监听服务，确保自动记账功能能够运行\n",
                        () -> PermissionHelper.requestAutoStartPermission(this)
                )
        );

        //电池优化策略
        SettingClickableTextView batteryOptimizations = new SettingClickableTextView(
                this,
                binding.batteryOptimizationsOption,
                R.string.battery_optimization,
                "设置安卓原生的电池优化策略",
                R.drawable.outline_battery_android_frame_3_24
        );
        batteryOptimizations.setFunctionListener(v -> showExplanationDialog(
                R.string.battery_optimization,
                "该设置项是原生安卓的一个重要设置项，可以更加底层地调整应用的省电策略。" +
                        "如果需要确保应用在后台能够运行，请在电池优化设置界面找到对应应用，点击开关左侧的文本并勾选“无限制”。" +
                        "影响范围如下：\n" +
                        "- 自动记账的通知监听服务能否在后台保持运行\n" +
                        "- 自动记账触发后能否第一时间发送通知\n",
                () -> requestSpecialPermission(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, false)
        ));

        //精确闹钟权限
        SettingClickableTextView alarm = new SettingClickableTextView(
                this,
                binding.alarmOption,
                R.string.alarm_permission,
                "允许设置定时任务",
                R.drawable.outline_alarm_24
        );
        alarm.setFunctionListener(v -> showExplanationDialog(
                R.string.alarm_permission,
                "该权限允许应用执行某些定时任务，以实现一些自动化功能，应用范围如下：\n" +
                        "- 每日0点自动检查并重置预算\n",
                () -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestSpecialPermission(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, true);
                    } else {
                        Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    /**
     * 显示权限解释对话框
     *
     * @param title   对话框标题
     * @param message 对话框内容，支持Markdown格式
     * @param action  点击确定按钮后执行的操作
     */
    private void showExplanationDialog(@StringRes int title, String message, Runnable action) {
        //获取自定义弹窗视图
        View dialogBody = LayoutInflater.from(this)
                .inflate(R.layout.view_markdown_text, null);
        MaterialTextView textView = dialogBody.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(textView, message);

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setView(dialogBody)
                .setPositiveButton("前往设置", (dialog, which) -> action.run())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 申请运行时权限
     *
     * @param permission 运行时权限
     */
    private void requestRuntimePermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
        } else {
            runtimeLauncher.launch(permission);
        }
    }

    /**
     * 跳转到特殊应用权限设置界面
     *
     * @param permission     特殊应用权限
     * @param needPackageUri 是否需要在构建Intent的时候添加package:开头的Uri，以便直接跳转到该应用的设置界面
     */
    private void requestSpecialPermission(String permission, boolean needPackageUri) {
        Intent permissionIntent;
        if (needPackageUri) {
            permissionIntent = new Intent(permission, Uri.parse("package:" + getPackageName()));
        } else {
            permissionIntent = new Intent(permission);
        }

        permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(permissionIntent);
    }
}