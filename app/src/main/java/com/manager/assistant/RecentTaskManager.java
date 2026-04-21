package com.manager.assistant;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;

import java.lang.ref.WeakReference;

public class RecentTaskManager implements Application.ActivityLifecycleCallbacks {

    private static RecentTaskManager instance;
    private boolean doNotHideOnce = false;      //豁免一次后台隐藏
    private int foregroundCount = 0;
    private WeakReference<Activity> rootActivityRef;

    private RecentTaskManager(@NonNull Application app) {
        app.registerActivityLifecycleCallbacks(this);
    }

    /**
     * 初始化并注册 Activity 生命周期监听器
     *
     * @param app {@link Application}应用类实例
     */
    public static void init(Application app) {
        if (instance == null) {
            instance = new RecentTaskManager(app);
        }
    }

    /**
     * 获取{@link RecentTaskManager}实例
     *
     * @return {@link RecentTaskManager}实例
     */
    private static RecentTaskManager get() {
        if (instance == null) {
            throw new IllegalStateException("RecentTaskManager not initialized");
        }
        return instance;
    }

    // =========================
    // 🔹 外部跳转支持（核心）
    // =========================

    /**
     * 跳转到外部活动
     *
     * @param context 上下文
     * @param intent  意图
     */
    public static void startExternalActivity(@NonNull Context context, Intent intent) {
        //设置豁免标识
        RecentTaskManager manager = get();
        manager.doNotHideOnce = true;

        context.startActivity(intent);
    }

    /**
     * 启动有回调的外部活动
     *
     * @param launcher 活动启动器
     * @param intent   意图
     */
    public static void startExternalActivity(@NonNull ActivityResultLauncher<Intent> launcher, Intent intent) {
        //设置豁免标识
        RecentTaskManager manager = get();
        manager.doNotHideOnce = true;

        launcher.launch(intent);
    }

    // =========================
    // 🔹 生命周期核心逻辑
    // =========================

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        if (activity.isTaskRoot()) {
            rootActivityRef = new WeakReference<>(activity);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        foregroundCount++;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        foregroundCount--;

        if (foregroundCount == 0) {
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
            rootActivity.finishAndRemoveTask();
            rootActivityRef = null;
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
