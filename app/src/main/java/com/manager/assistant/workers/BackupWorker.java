package com.manager.assistant.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.manager.assistant.enums.LogTags;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.helpers.DataIOHelper;
import com.manager.assistant.ui.pages.setting.SettingFragment;
import com.manager.assistant.ui.pages.setting.data_io.data_helpers.DataHelperBase;

import java.util.ArrayList;
import java.util.List;

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
        Context context = getApplicationContext();

        //获取APP数据
        List<String> fileNameList = new ArrayList<>(), fileContentList = new ArrayList<>();
        for (SettingFragment.IODataType dataType : SettingFragment.IODataType.values()) {
            DataHelperBase<BookkeepingDbHelper, ?> dataHelper = dataType.getDataHelper(context);

            try {
                String fileName = dataType.getDefaultFileName();
                String fileContent = dataHelper.getDataInJSON();

                fileNameList.add(fileName);
                fileContentList.add(fileContent);
            } catch (JsonProcessingException e) {
                Log.e(LogTags.BACKUP_WORKER.getV(), "JSON序列化出错");
                return Result.failure();
            }
        }

        //打包为zip文件
        DataIOHelper DataIOHelper = new DataIOHelper(context);
        DataIOHelper.packDataInZip(fileNameList, fileContentList);

        return Result.success();
    }
}
