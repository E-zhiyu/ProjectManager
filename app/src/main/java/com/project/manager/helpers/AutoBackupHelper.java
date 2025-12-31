package com.project.manager.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.project.manager.data.data_save.preference.AutoBackupPreference;
import com.project.manager.ui.setting.setting_option_views.SettingSwitchView;

public class AutoBackupHelper {
    private final Context context;              //上下文
    private SettingSwitchView switchOptionView; //设置界面的开关选项

    enum BackupFrequency {
        DAY("每天"),       //每天
        WEEK("每星期"),    //每个星期
        MONTH("每个月");   //每月
        private final String name;

        BackupFrequency(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public AutoBackupHelper(Context context) {
        this.context = context;
    }

    public void setSwitchOptionView(SettingSwitchView switchOptionView) {
        this.switchOptionView = switchOptionView;
    }

    /**
     * 通过SAF指定备份目录
     *
     * @param launcher 启动SAF并处理回调的启动器
     */
    public void selectBackupDirectory(ActivityResultLauncher<Intent> launcher) {
        //获取之前设置的备份目录
        String backupDirUri = AutoBackupPreference.getBackupDirectory(context);

        //启动SAF
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (backupDirUri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, backupDirUri);  //设置初始目录为已保存的目录
        }
        launcher.launch(intent);
    }

    /**
     * 处理SAF数据的回调方法（应在宿主的ActivityResultLauncher<Intent>类中调用）
     *
     * @param resultCode 响应代码
     * @param data       包含回调数据的Intent
     */
    public void handleActivityResult(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            //获取用户选择的目录URI
            Uri backupDirUri = data.getData();
            if (backupDirUri == null) return;

            //保存URI以便后续使用
            String uriStr = backupDirUri.toString();
            AutoBackupPreference.setBackupDirectory(context, uriStr);

            //请求持久化权限
            context.getContentResolver().takePersistableUriPermission(
                    backupDirUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
        } else {
            if (switchOptionView != null) {
                switchOptionView.setChecked(false);
            }
        }
    }
}
