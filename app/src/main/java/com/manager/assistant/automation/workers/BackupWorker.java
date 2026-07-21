package com.manager.assistant.automation.workers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.WorkerParameters;
import androidx.work.rxjava3.RxWorker;

import com.manager.assistant.auxiliary.enums.BackupDataType;
import com.manager.assistant.auxiliary.enums.LogTags;
import com.manager.assistant.data.backup.helpers.BackupHelperBase;
import com.manager.assistant.data.save.preference.AutoBackupPreference;
import com.manager.assistant.helpers.file.FileHelper;
import com.manager.assistant.helpers.file.ZipHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 自动备份的Worker类
 */
public class BackupWorker extends RxWorker {
    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Single<Result> createWork() {
        Context context = getApplicationContext();

        //创建备份任务
        List<Completable> taskList = new ArrayList<>();
        for (BackupDataType type : BackupDataType.values()) {
            BackupHelperBase<?, ?> backupHelper = type.createBackupHelper(context);
            taskList.add(backupHelper.exportDataToTempFile(context));
        }

        //获取保存的备份目录 Uri
        String uriStr = AutoBackupPreference.getBackupDirectoryUri(context);
        if (uriStr.isEmpty()) {
            return Single.just(Result.failure());
        }
        Uri dirUri = Uri.parse(uriStr);

        //创建备份文件
        String fileName = FileHelper.generateBackupFileName();
        DocumentFile backupDir = DocumentFile.fromTreeUri(context, dirUri);
        if (backupDir == null) return Single.just(Result.failure());
        DocumentFile backupFile = backupDir.createFile("application/zip", fileName);
        if (backupFile == null) return Single.just(Result.failure());
        Log.d(LogTags.BACKUP_WORKER.n(), "备份文件成功创建");

        //执行备份逻辑
        return Completable.merge(taskList)
                .andThen(ZipHelper.createBackupFile(backupFile.getUri(), context, true))
                .subscribeOn(Schedulers.io())
                .toSingleDefault(Result.success())
                .onErrorReturn(e -> {
                    Log.e(LogTags.BACKUP_WORKER.n(), "备份失败");
                    return Result.failure();
                });
    }
}
