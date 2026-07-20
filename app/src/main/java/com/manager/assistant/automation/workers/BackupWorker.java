package com.manager.assistant.automation.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * 自动备份的Worker类
 */
public class BackupWorker extends Worker {
    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //TODO:实现自动备份
//        Context context = getApplicationContext();
//
//        //获取APP数据
//        List<String> fileNameList = new ArrayList<>(), fileContentList = new ArrayList<>();
//        for (BackupDataType dataType : BackupDataType.values()) {
//            BackupHelperBase<?, ?> dataHelper = dataType.createBackupHelper(context);
//
//            try {
//                String fileName = dataType.getFileName();
//                String fileContent = dataHelper.getDataInJSON();
//
//                fileNameList.add(fileName);
//                fileContentList.add(fileContent);
//            } catch (JsonProcessingException e) {
//                Log.e(LogTags.BACKUP_WORKER.n(), "JSON序列化出错");
//                return Result.failure();
//            }
//        }
//
//        //打包为zip文件
//        DataIOHelper DataIOHelper = new DataIOHelper(context);
//        DataIOHelper.packDataInZip(fileNameList, fileContentList);

        return Result.success();
    }
}
