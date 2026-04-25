package com.manager.assistant.helpers.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.LifecycleManager;
import com.manager.assistant.data.save.preference.AutoBackupPreference;
import com.manager.assistant.generic_enums.options.BackupFrequency;
import com.manager.assistant.ui.pages.main.setting.setting_option_views.SettingSwitchView;
import com.manager.assistant.automation.schedulers.BackupScheduler;

public class AutoBackupHelper {
    private final Context context;              //上下文
    private SettingSwitchView switchOptionView; //设置界面的开关选项

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
        String backupDirUri = AutoBackupPreference.getBackupDirectoryUri(context);

        //启动SAF
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (backupDirUri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, backupDirUri);  //设置初始目录为已保存的目录
        }
        LifecycleManager.startExternalActivity(launcher, intent);
    }

    /**
     * 处理SAF数据的回调方法（应在宿主的ActivityResultLauncher<Intent>类中调用）
     *
     * @param resultCode   响应代码
     * @param data         包含回调数据的Intent
     * @param pathTextView 显示选择目录的路径的文本视图
     */
    public void handleActivityResult(int resultCode, @Nullable Intent data, MaterialTextView pathTextView) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Toast.makeText(context, "成功设置备份存储目录", Toast.LENGTH_SHORT).show();

            //获取用户选择的目录URI
            Uri backupDirUri = data.getData();
            if (backupDirUri == null) return;

            //保存URI以便后续使用
            String oldDirUriStr = AutoBackupPreference.getBackupDirectoryUri(context);
            String uriStr = backupDirUri.toString();
            AutoBackupPreference.setBackupDirectoryUri(context, uriStr);

            //通过TextView显示路径
            String path = UriPathHelper.getDisplayPathFromSAFUri(context, backupDirUri);
            pathTextView.setText(path);

            //请求持久化权限
            context.getContentResolver().takePersistableUriPermission(
                    backupDirUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );

            //第一次设置备份目录时创建自动备份定时任务
            if (oldDirUriStr == null) {
                int frequencyIndex = AutoBackupPreference.getBackupFrequency(context);
                long intervalMillis = BackupFrequency.values()[frequencyIndex].getIntervalMillis();
                BackupScheduler.schedulePeriodicBackup(context, intervalMillis);
            }
        } else {
            String backupDir = AutoBackupPreference.getBackupDirectoryUri(context);
            if (switchOptionView != null && backupDir.isEmpty()) {
                switchOptionView.setChecked(false);
            }
        }
    }
}
