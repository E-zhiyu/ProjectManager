package com.manager.assistant;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.helpers.BiometricHelper;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class LifecycleManager implements Application.ActivityLifecycleCallbacks {
    private static LifecycleManager instance;
    private boolean doNotHideOnce = false;      //豁免一次后台隐藏
    private boolean userLeft = true;            //用户离开应用（即前台活动数量为0）
    private int foregroundCount = 0;
    private WeakReference<Activity> rootActivityRef;

    private LifecycleManager(@NonNull Application app) {
        app.registerActivityLifecycleCallbacks(this);
    }

    /**
     * 初始化并注册 Activity 生命周期监听器
     *
     * @param app {@link Application}应用类实例
     */
    public static void init(Application app) {
        if (instance == null) {
            instance = new LifecycleManager(app);
        }
    }

    /**
     * 获取{@link LifecycleManager}实例
     *
     * @return {@link LifecycleManager}实例
     */
    private static LifecycleManager get() {
        if (instance == null) {
            throw new IllegalStateException("RecentTaskManager not initialized");
        }
        return instance;
    }

    // =========================
    // 🔹 外部跳转支持（核心）
    // =========================

    /**
     * 跳转到外部活动并豁免一次后台隐藏
     *
     * @param context 上下文
     * @param intent  意图
     */
    public static void startExternalActivity(@NonNull Context context, Intent intent) {
        //设置豁免标识
        LifecycleManager manager = get();
        manager.doNotHideOnce = true;

        context.startActivity(intent);
    }

    /**
     * 启动有回调的外部活动并豁免一次后台隐藏
     *
     * @param launcher 活动启动器
     * @param intent   意图
     */
    public static void startExternalActivity(@NonNull ActivityResultLauncher<Intent> launcher, Intent intent) {
        //设置豁免标识
        LifecycleManager manager = get();
        manager.doNotHideOnce = true;

        launcher.launch(intent);
    }

    // =========================
    // 🔹 生命周期核心逻辑
    // =========================

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        //保存根 Activity 依赖
        if (activity.isTaskRoot()) {
            rootActivityRef = new WeakReference<>(activity);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d(LogTags.LIFECYCLE_MANAGER.getV(), "活动启动");
        foregroundCount++;

        //TODO:采用时间间隔法判断，时间到了进入Activity就要身份验证
        if (userLeft) {
            BiometricHelper.showBiometricPrompt((FragmentActivity) activity, new BiometricHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(activity, "身份验证成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {
                    Toast.makeText(activity, "身份验证失败", Toast.LENGTH_SHORT).show();

                    //强制退出但是保持后台运行
                    if (rootActivityRef != null) {
                        rootActivityRef.get().finishAndRemoveTask();
                    }
                }
            });
        }
        userLeft = false;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.d(LogTags.LIFECYCLE_MANAGER.getV(), "活动停止");
        foregroundCount--;

        Log.d(LogTags.LIFECYCLE_MANAGER.getV(), String.format(Locale.getDefault(), "前台活动数：%d", foregroundCount));
        if (foregroundCount == 0) {
            Log.i(LogTags.LIFECYCLE_MANAGER.getV(), "用户离开应用");
            userLeft = true;
            handleAppToBackground(activity);
        }
    }

    /**
     * 处理应用变为后台的情况
     *
     * @param context 上下文
     */
    private void handleAppToBackground(Context context) {
        //判断是否豁免
        if (doNotHideOnce) {
            doNotHideOnce = false;
            return;
        }

        //判断开关
        boolean isHideInRecents = AutoBookKeepingPreference.getHideRecentTask(context);
        if (!isHideInRecents) {
            return;
        }

        //获取 root Activity
        Activity rootActivity = rootActivityRef != null ? rootActivityRef.get() : null;

        //在合适情况下清除最近任务
        if (rootActivity != null && !rootActivity.isFinishing()) {
            Log.i(LogTags.LIFECYCLE_MANAGER.getV(), "结束根活动并隐藏后台");
            rootActivity.finishAndRemoveTask();
            rootActivityRef = null;
        } else if (rootActivity == null) {
            Log.e(LogTags.LIFECYCLE_MANAGER.getV(), "无法定位根Activity");
        } else {
            Log.e(LogTags.LIFECYCLE_MANAGER.getV(), "根Activity正在Finishing");
        }
    }

    // =========================
    // 🔹 其他生命周期（空实现）
    // =========================

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }
}
