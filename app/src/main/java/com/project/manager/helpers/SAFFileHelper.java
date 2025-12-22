package com.project.manager.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SAFFileHelper {
    private final Context context;                              //上下文
    private SAFFileReadCallback readCallback;
    private SAFFileWriteCallback writeCallback;
    private final List<File> tempFileList = new ArrayList<>(); //临时文件列表
    private final File tempDir;                                //临时文件目录
    private File tempZipFile;                                  //临时zip压缩文件

    /**
     * 文件写入回调接口
     */
    public interface SAFFileWriteCallback {
        void onFileWrote();

        void onError(String errMessage);
    }

    /**
     * 文件读取回调接口
     */
    public interface SAFFileReadCallback {
        void onFileRead(String content);

        void onError(String errMessage);
    }

    public SAFFileHelper(@NonNull Context context) {
        this.context = context;
        tempDir = new File(context.getExternalFilesDir(null), "temp");
    }

    /**
     * 创建临时JSON文件
     *
     * @param file_name 文件名称
     * @param content   文件内容
     */
    public void createTempJsonFile(String file_name, String content) {
        //创建临时目录
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File tempJsonFile = new File(tempDir, file_name);
        tempFileList.add(tempJsonFile);
        writeContentToFile(tempJsonFile, content);
    }

    /**
     * 将字符串内容写入文件
     *
     * @param targetFile 待写入的目标文件
     * @param content    字符串内容
     */
    private void writeContentToFile(File targetFile, String content) {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            writeCallback.onError("临时文件写入失败");
        }
    }

    /**
     * 创建临时zip压缩包并将临时数据文件放入压缩包内
     */
    public void createTempZipFile(ActivityResultLauncher<Intent> launcher) {
        //获取当前日期和时间并生成默认文件名
        Calendar calendar = Calendar.getInstance();
        String now_date = String.format(
                Locale.getDefault(),
                "%04d%02d%02d(%02d%02d%02d)",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        );
        String zip_file_name = String.format(Locale.getDefault(), "ManagerAssistantData_%s.zip", now_date);

        //创建zip文件
        tempZipFile = new File(tempDir, zip_file_name);
        try (FileOutputStream fos = new FileOutputStream(tempZipFile)) {
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File tempFile : tempFileList) {
                try (FileInputStream fis = new FileInputStream(tempFile)) {
                    ZipEntry zipEntry = new ZipEntry(tempFile.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
            saveFileUsingSAF(launcher, tempZipFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用SAF保存zip文件
     *
     * @param launcher   用于启动SAF的意图启动器
     * @param sourceFile 临时zip文件对象
     */
    private void saveFileUsingSAF(@NonNull ActivityResultLauncher<Intent> launcher, @NonNull File sourceFile) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, sourceFile.getName());
        launcher.launch(intent);
    }

    /**
     * 处理SAF的结果（应在宿主的ActivityResultLauncher<Intent>类中调用）
     *
     * @param resultCode  回应代码
     * @param data        包含目标文件Uri的Intent
     * @param isWriteMode 是否为文件写入模式
     */
    public void handleActivityResult(int resultCode, @Nullable Intent data, boolean isWriteMode) {
        // 处理创建文件的结果（保持原有逻辑）
        if (isWriteMode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    copyZipToUri(uri);
                }
            }
        }
//        // 处理读取文件的结果
//        else {
//            if (resultCode == Activity.RESULT_OK && data != null) {
//                Uri uri = data.getData();
//                if (uri != null) {
//                    readContentFromUri(uri);
//                }
//            }
//        }
    }

    /**
     * 通过SAF选择文件并读取内容
     *
     * @param callback 读取结果回调
     * @param fileType 文件类型
     * @param launcher SAF启动器
     */
    public void openFileAndReadContent(SAFFileReadCallback callback,
                                       String fileType,
                                       ActivityResultLauncher<Intent> launcher) {
        this.readCallback = callback;

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(fileType); // 允许选择任何文件类型

            launcher.launch(intent);
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            callback.onError("SAF出错");
        }
    }

    /**
     * 将临时zip文件写入到uri指定的位置
     *
     * @param uri 用户通过SAF生成的uri
     */
    private void copyZipToUri(Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        ParcelFileDescriptor pfd = null;
        FileInputStream fis = null;
        FileOutputStream finalFos = null;

        //将临时文件写入SAF URI
        try {
            pfd = resolver.openFileDescriptor(uri, "w");
            if (pfd != null) {
                finalFos = new FileOutputStream(pfd.getFileDescriptor());
                fis = new FileInputStream(tempZipFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    finalFos.write(buffer, 0, length);
                }
            }

            writeCallback.onFileWrote();    //触发写入成功回调
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            writeCallback.onError("复制临时zip文件失败");
        } finally {
            // 关闭所有流和删除临时文件
            try {
                if (fis != null) fis.close();
                if (finalFos != null) finalFos.close();
                if (pfd != null) pfd.close();
                clearTempFile();    //删除临时文件
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(context, e);
            }
        }
    }

    /**
     * 清除临时文件
     */
    public void clearTempFile() {
        for (File tempFile : tempFileList) {
            tempFile.delete();
        }
        tempFileList.clear();

        if (tempZipFile != null) {
            tempZipFile.delete();
            tempZipFile = null;
        }
    }
}
