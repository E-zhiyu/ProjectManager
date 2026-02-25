package com.manager.assistant.isolated_enums;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.function.Function;

public enum DirectoryPaths {
    DATA_TEMP(Context::getExternalCacheDir, "data_temp"),
    PICTURE(context -> context.getExternalFilesDir(null), "pictures"),
    PICTURE_TEMP(Context::getExternalCacheDir, "picture_temp"),
    UPDATE_BACKUP(Context::getExternalCacheDir, "update_backup");

    private final Function<Context, File> contextFileFunction;
    private final String childDirName;

    DirectoryPaths(Function<Context, File> contextFileFunction, String childDirName) {
        this.contextFileFunction = contextFileFunction;
        this.childDirName = childDirName;
    }

    /**
     * 获取文件夹路径并自动创建文件夹
     *
     * @param context 上下文
     * @return 文件夹File实例(文件夹创建失败返回null)
     */
    @Nullable
    public File getDir(Context context) {
        File parentDIr = contextFileFunction.apply(context);
        File targetDir = new File(parentDIr, childDirName);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return null;
        } else {
            return targetDir;
        }
    }

    public String getChildDirName() {
        return childDirName;
    }
}
