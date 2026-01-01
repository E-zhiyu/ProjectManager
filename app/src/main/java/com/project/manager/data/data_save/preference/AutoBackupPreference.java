package com.project.manager.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class AutoBackupPreference {
    private static final String PREF_NAME = "AutoBackupPreference";
    private static final String KEY_SWITCH_STAT = "switch_stat";            //功能开关状态
    private static final String KEY_BACKUP_DIRECTORY = "backup_directory";  //自动备份保存的目录
    private static final String KEY_BACKUP_FREQUENCY = "backup_frequency";  //自动备份频率

    public static void setSwitchStat(@NonNull Context context, boolean stat) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_SWITCH_STAT, stat).apply();
    }

    public static boolean getSwitchStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_SWITCH_STAT, false);
    }

    /**
     * 设置备份目录Uri
     *
     * @param context   上下文
     * @param directory Uri字符串
     */
    public static void setBackupDirectoryUri(@NonNull Context context, String directory) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_BACKUP_DIRECTORY, directory).apply();
    }

    /**
     * 获取备份目录Uri字符串
     *
     * @param context 上下文
     * @return 可以转换为Uri的字符串
     */
    public static String getBackupDirectoryUri(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_BACKUP_DIRECTORY, null);
    }

    public static void setBackupFrequency(@NonNull Context context, int frequency) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_BACKUP_FREQUENCY, frequency).apply();
    }

    public static int getBackupFrequency(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_BACKUP_FREQUENCY, 0);
    }
}
