package com.manager.assistant;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.FragmentActivity;

import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.data.save.preference.SecurityPreference;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.generic_enums.options.AuthOpportunity;
import com.manager.assistant.helpers.BiometricHelper;

import java.util.List;
import java.util.Locale;

public class LifecycleManager implements Application.ActivityLifecycleCallbacks {
    private static LifecycleManager instance;
    private boolean doNotHideOnce = false;      //豁免一次后台隐藏
    private boolean userLeft = true;            //用户离开应用（即前台活动数量为0）
    private long lastAuthTimeMilli = 0;         //上次进行身份验证的时间戳
    private int foregroundCount = 0;

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
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d(LogTags.LIFECYCLE_MANAGER.getV(), "活动启动");
        foregroundCount++;

        boolean isAuthOpened = SecurityPreference.getAuthSwitchStat(activity);
        AuthOpportunity authOpportunity = AuthOpportunity.values()[SecurityPreference.getAuthOpportunity(activity)];
        if (userLeft && isAuthOpened && System.currentTimeMillis() - lastAuthTimeMilli >= authOpportunity.getTimeMilli()) {
            BiometricHelper.showBiometricPrompt((FragmentActivity) activity, new BiometricHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(activity, "身份验证成功", Toast.LENGTH_SHORT).show();
                    lastAuthTimeMilli = System.currentTimeMillis();
                }

                @Override
                public void onError(int errCode, CharSequence errStr) {
                    if (errCode == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
                            || errCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
                    ) {
                        //设备无锁屏/不支持生物验证时关闭身份验证，否则用户可能无法进入APP
                        Toast.makeText(activity, errStr + "，已自动关闭身份验证", Toast.LENGTH_SHORT).show();
                        SecurityPreference.setAuthSwitchStat(activity, false);
                    } else {
                        Toast.makeText(activity, errStr, Toast.LENGTH_SHORT).show();

                        //强制退出但是保持后台运行
                        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                        if (am != null) {
                            List<ActivityManager.AppTask> taskList = am.getAppTasks();
                            if (taskList != null) {
                                for (ActivityManager.AppTask task : taskList) {
                                    task.finishAndRemoveTask();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailed() {

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
            Log.i(LogTags.LIFECYCLE_MANAGER.getV(), "前台活动数量为0");

            //延迟修改用户离开标志位
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (foregroundCount == 0) {
                    Log.i(LogTags.LIFECYCLE_MANAGER.getV(), "判定为用户离开应用");
                    userLeft = true;
                }
            }, 500);

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

        //延迟清除最近任务，防止重新创建活动时误执行
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (foregroundCount != 0) {
                return;
            }

            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.AppTask> taskList = am.getAppTasks();
                if (taskList != null) {
                    for (ActivityManager.AppTask task : taskList) {
                        // 将当前应用的 Task 从最近任务列表中彻底移除
                        task.finishAndRemoveTask();
                    }
                }
            }
        }, 3000);
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
