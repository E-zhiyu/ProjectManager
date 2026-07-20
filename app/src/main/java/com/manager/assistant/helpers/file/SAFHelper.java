package com.manager.assistant.helpers.file;

import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.manager.assistant.ManagerAssistant;
import com.manager.assistant.generic_enums.LogTags;

public class SAFHelper {
    /**
     * 通过SAF打开文件
     *
     * @param fileTypes 文件种类白名单，只能选择指定类型的文件
     * @param launcher  启动SAF的意图启动器
     */
    public static void openDocumentViaSAF(
            String[] fileTypes,
            @NonNull ActivityResultLauncher<Intent> launcher
    ) {
        ManagerAssistant.lockLifecycleObserver();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, fileTypes);
        launcher.launch(intent);
        Log.d(LogTags.SAF_HELPER.n(), "SAF启动成功");
    }

    /**
     * 通过SAF创建文件
     *
     * @param intentType {@link Intent#setType(String)}中传递的参数
     * @param fileName   新建文件的文件名
     * @param launcher   启动SAF的启动器
     */
    public static void createDocumentViaSAF(
            String intentType,
            String fileName,
            @NonNull ActivityResultLauncher<Intent> launcher
    ) {
        ManagerAssistant.lockLifecycleObserver();
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(intentType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        launcher.launch(intent);
    }
}
