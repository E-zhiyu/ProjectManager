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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SAFFileHelper {
    private final Context context;                                  //上下文
    private ReadCallback readCallback;                              //文件读取回调
    private WriteCallback writeCallback;                            //文件写入回调
    private final List<File> tempJsonFileList = new ArrayList<>();  //临时JSON文件列表
    private final File tempDir;                                     //临时文件目录
    private File tempZipFile;                                       //临时zip压缩文件

    /**
     * 文件写入回调接口
     */
    public interface WriteCallback {
        void onFileWrote();

        void onError(String errMessage);
    }

    /**
     * 文件读取回调接口
     */
    public interface ReadCallback {
        /**
         * 导入数据的zip文件成功解压的回调
         *
         * @param tempJsonFileList 临时JSON文件列表
         */
        void onZipUnpacked(List<File> tempJsonFileList);

        void onError(String errMessage);
    }

    public SAFFileHelper(@NonNull Context context) {
        this.context = context;
        tempDir = new File(context.getExternalFilesDir(null), "temp");
    }

    /**
     * 将临时文件打包至zip文件中
     *
     * @param writeCallback   文件写入回调
     * @param launcher        启动SAF的意图启动器
     * @param fileNameList    文件名列表
     * @param fileContentList 与文件名列表对应的文件内容列表
     */
    public void packFileInZip(
            WriteCallback writeCallback,
            ActivityResultLauncher<Intent> launcher,
            @NonNull List<String> fileNameList,
            List<String> fileContentList) {
        this.writeCallback = writeCallback;

        //创建列表中的临时文件
        for (int index = 0; index < fileNameList.size(); index++) {
            String file_name = fileNameList.get(index);
            String file_content = fileContentList.get(index);

            createTempJsonFile(file_name, file_content);
        }

        //生成zip文件并
        createTempZipFile(launcher);
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
     *
     * @param launcher 启动SAF的意图启动器
     */
    private void createTempZipFile(ActivityResultLauncher<Intent> launcher) {
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

            for (File tempFile : tempJsonFileList) {
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
            ExceptionHelper.showExceptionDialog(context, e);
            writeCallback.onError("无法创建临时zip文件");
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
     * 将临时zip文件写入到uri指定的位置
     *
     * @param uri 用户通过SAF生成的uri
     */
    private void copyTempZipToUri(Uri uri) {
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
            //关闭所有流并删除临时文件
            try {
                if (fis != null) fis.close();
                if (finalFos != null) finalFos.close();
                if (pfd != null) pfd.close();
                clearTempFile();    //删除临时文件
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                writeCallback.onError("无法正确关闭流或删除临时文件");
            }
        }
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
                    copyTempZipToUri(uri);
                }
            }
        }
        // 处理读取文件的结果
        else {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    copyUriToTempZip(uri);
                }
            }
        }
    }

    public void openZipBySAF(ReadCallback callback,
                             ActivityResultLauncher<Intent> launcher) {
        this.readCallback = callback;

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            launcher.launch(intent);
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("SAF出错");
        }
    }

    /**
     * 将用户选择的目标zip复制到临时目录
     *
     * @param uri 用户通过SAF选择的uri
     */
    private void copyUriToTempZip(Uri uri) {
        //生成临时zip文件对象
        tempZipFile = new File(tempDir, "backup_temp.zip");
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            RuntimeException e = new RuntimeException("临时文件目录创建失败");
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("临时文件目录创建失败");
            return;
        }

        ParcelFileDescriptor pfd = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (pfd == null) throw new RuntimeException("无法正确打开流");

            inputStream = new FileInputStream(pfd.getFileDescriptor());
            outputStream = new FileOutputStream(tempZipFile);

            // 使用缓冲流提高复制效率
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            //复制完成后解包
            unpackZipFile();
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("复制zip文件时出错");

            //复制失败时清理可能残留的临时文件
            clearTempFile();
        } finally {
            try {
                if (pfd != null) pfd.close();
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                readCallback.onError("无法正确关闭流");
            }
        }
    }

    /**
     * 解包临时zip文件
     */
    private void unpackZipFile() {
        try (FileInputStream fis = new FileInputStream(tempZipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            byte[] buffer = new byte[8192]; // 8KB缓冲区

            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(tempDir, entry.getName());
                tempJsonFileList.add(entryFile);

                if (entryFile.isDirectory()) continue;  //不处理目录类

                // 写入文件内容
                try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                zis.closeEntry(); //关闭当前条目
            }

            //读取解压得到的文件内容
            if (!tempJsonFileList.isEmpty()) {
                readCallback.onZipUnpacked(tempJsonFileList);
            } else {
                readCallback.onError("zip文件为空");
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("zip文件解压失败");
            clearTempFile();
        }
    }

    /**
     * 创建空白临时JSON文件
     *
     * @param file_name 文件名称
     * @param content   文件内容
     */
    private void createTempJsonFile(String file_name, String content) {
        //创建临时目录
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            RuntimeException e = new RuntimeException("无法创建临时文件目录");
            ExceptionHelper.showExceptionDialog(context, e);
            clearTempFile();
            return;
        }

        File tempJsonFile = new File(tempDir, file_name);
        tempJsonFileList.add(tempJsonFile);
        writeContentToFile(tempJsonFile, content);
    }

    /**
     * 清除临时文件
     */
    public void clearTempFile() {
        for (File tempFile : tempJsonFileList) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        tempJsonFileList.clear();

        if (tempZipFile != null) {
            tempZipFile.delete();
            tempZipFile = null;
        }
    }
}