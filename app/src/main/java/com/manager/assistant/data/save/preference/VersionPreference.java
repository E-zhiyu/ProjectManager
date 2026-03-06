package com.manager.assistant.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class VersionPreference {
    private static final String PREF_NAME = "VersionPreference";
    private static final String KEY_SKIP_VERSION_CODE = "skip_version_code";            //跳过更新的版本代码
    private static final String KEY_START_VERSION_CHECK = "start_version_check";        //控制软件启动时运行更新检测的整数值
    public static final int VERSION_CHECK_RECYCLE_NUM = 3;                              //控制每启动几次就检测更新的整数值
    private static final String KEY_FIND_MANDATORY_UPDATE = "find_mandatory_update";    //是否发现强制更新条目
    private static final String KEY_MANDATORY_VERSION_NAME = "m_version_name";          //强制更新版本名称
    private static final String KEY_MANDATORY_UPDATE_LOG = "m_update_log";              //强制更新版本的更新日志
    private static final String KEY_MANDATORY_DOWNLOAD_URL = "m_download_url";          //强制更新版本的下载链接
    private static final String KEY_APK_URI = "apk_uri";                                //安装包Uri

    /**
     * 设置跳过的版本代码
     *
     * @param context      上下文
     * @param version_code 版本代码
     */
    public static void setSkipVersionCode(@NonNull Context context, int version_code) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_SKIP_VERSION_CODE, version_code).apply();
    }

    /**
     * 获取跳过的版本代码
     *
     * @param context 上下文
     * @return 跳过的版本代码
     */
    public static int getSkipVersionCode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_SKIP_VERSION_CODE, 0);
    }

    /**
     * 写入启动时控制检测更新的整数
     *
     * @param context 上下文
     * @param num     旧数值+1
     */
    public static void setStartVersionCheckNum(@NonNull Context context, int num) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_START_VERSION_CHECK, num).apply();
    }

    /**
     * 获取启动时控制检测更新的整数
     *
     * @param context 上下文
     * @return 启动时控制检测更新的整数
     */
    public static int getStartVersionCheckNum(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_START_VERSION_CHECK, 0);
    }

    /**
     * 写入是否获取到强制更新版本
     *
     * @param context             上下文
     * @param findMandatoryUpdate 是否获取到强制更新版本
     */
    public static void setFindMandatoryUpdate(@NonNull Context context, boolean findMandatoryUpdate) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_FIND_MANDATORY_UPDATE, findMandatoryUpdate).apply();
    }

    /**
     * 读取是否获取到强制更新版本
     *
     * @param context 上下文
     * @return 是否获取到强制更新版本
     */
    public static boolean getFindMandatoryUpdate(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_FIND_MANDATORY_UPDATE, false);
    }

    /**
     * 写入强制更新版本的版本名称
     *
     * @param context      上下文
     * @param version_name 强制更新的版本名称
     */
    public static void setMandatoryVersionName(@NonNull Context context, String version_name) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_MANDATORY_VERSION_NAME, version_name).apply();
    }

    /**
     * 获取强制更新的版本名称
     *
     * @param context 上下文
     * @return 强制更新的版本名称
     */
    public static String getMandatoryVersionName(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_MANDATORY_VERSION_NAME, "");
    }

    /**
     * 写入强制更新版本的更新日志
     *
     * @param context    上下文
     * @param update_log 强制更新版本的更新日志
     */
    public static void setMandatoryUpdateLog(@NonNull Context context, String update_log) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_MANDATORY_UPDATE_LOG, update_log).apply();
    }

    /**
     * 获取强制更新版本的更新日志
     *
     * @param context 上下文
     * @return 强制更新版本的更新日志
     */
    public static String getMandatoryUpdateLog(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_MANDATORY_UPDATE_LOG, "");
    }

    /**
     * 写入强制更新安装包的下载链接
     *
     * @param context     上下文
     * @param downloadUrl 强制更新安装包的下载链接
     */
    public static void setMandatoryDownloadUrl(@NonNull Context context, String downloadUrl) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_MANDATORY_DOWNLOAD_URL, downloadUrl).apply();
    }

    /**
     * 获取强制更新安装包的下载链接
     *
     * @param context 上下文
     * @return 强制更新安装包的下载链接
     */
    public static String getMandatoryDownloadUrl(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_MANDATORY_DOWNLOAD_URL, "");
    }

    /**
     * 设置安装包Uri
     *
     * @param context 上下文
     * @param uriStr  安装包Uri字符串
     */
    public static void setApkUri(@NonNull Context context, String uriStr) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_APK_URI, uriStr).apply();
    }

    /**
     * 获取安装包Uri
     *
     * @param context 上下文
     * @return 安装包Uri字符串
     */
    public static String getApkUri(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_APK_URI, "");
    }
}
