package com.project.manager.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.project.manager.LogTags;

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
         * @param fileList 读取到的JSON文件列表
         */
        void onZipUnpacked(List<File> fileList);

        /**
         * 用户选择单个JSON文件并成功的回调
         *
         * @param jsonFile 单个JSON文件对象
         */
        void onOneJsonFileRead(File jsonFile);

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
     * 创建临时zip压缩包并将临时数据文件放入压缩包内
     *
     * @param launcher 启动SAF的意图启动器
     */
    private void createTempZipFile(ActivityResultLauncher<Intent> launcher) {
        Log.d(LogTags.SAF_FILE_HELPER.getV(), "正在创建临时zip文件……");

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

            Log.d(LogTags.SAF_FILE_HELPER.getV(), "临时zip文件创建成功");
            saveFileUsingSAF(launcher, tempZipFile);
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            writeCallback.onError("无法创建临时zip文件");
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "无法创建临时zip文件");
        }
    }

    /**
     * 使用SAF保存zip文件
     *
     * @param launcher   用于启动SAF的意图启动器
     * @param sourceFile 临时zip文件对象
     */
    private void saveFileUsingSAF(@NonNull ActivityResultLauncher<Intent> launcher, @NonNull File sourceFile) {
        Log.d(LogTags.SAF_FILE_HELPER.getV(), "正在通过SAF指定zip备份文件存放位置……");

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, sourceFile.getName());
        launcher.launch(intent);

        Log.d(LogTags.SAF_FILE_HELPER.getV(), "SAF启动成功");
    }

    /**
     * 将临时zip文件写入到uri指定的位置
     *
     * @param uri 用户通过SAF生成的uri
     */
    private void copyTempZipToUri(Uri uri) {
        Log.d(LogTags.SAF_FILE_HELPER.getV(), "开始复制临时zip文件到指定位置");

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
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "复制临时zip文件失败");
        } finally {
            //关闭所有流并删除临时文件
            try {
                if (fis != null) fis.close();
                if (finalFos != null) finalFos.close();
                if (pfd != null) pfd.close();
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(context, e);
                writeCallback.onError("无法正确关闭流");
                Log.e(LogTags.SAF_FILE_HELPER.getV(), "无法正确关闭流");
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
        //处理导出数据的结果
        if (isWriteMode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    copyTempZipToUri(uri);
                }
                Log.i(LogTags.SAF_FILE_HELPER.getV(), "用户确认选择并进行下一步");
            } else {
                Log.i(LogTags.SAF_FILE_HELPER.getV(), "用户取消选择并关闭SAF");
            }
            clearTempFile();
        }
        //处理导入数据的结果
        else {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                String type = getMimeType(uri);
                if (uri != null && type != null) {
                    //判断文件类型
                    if (type.equals("application/zip")) {
                        Log.i(LogTags.SAF_FILE_HELPER.getV(), "用户选择zip备份文件");

                        //判断文件大小（字节）
                        long fileSize = getFileSizeFromUri(uri);
                        if (fileSize > 1024 * 1024 * 50) {
                            readCallback.onError("文件大于50MB");
                            Log.e(LogTags.SAF_FILE_HELPER.getV(), "文件大小超出限制");
                        } else if (fileSize <= 0) {
                            readCallback.onError("无法读取文件内容");
                            Log.e(LogTags.SAF_FILE_HELPER.getV(), "无法读取文件内容");
                        } else {
                            Log.i(LogTags.SAF_FILE_HELPER.getV(), "zip备份文件大小合法");
                            copyUriToTempZip(uri);
                        }
                    } else if (type.equals("application/json")) {
                        File oneJsonFile = getFileFromDocumentUri(uri);
                        readCallback.onOneJsonFileRead(oneJsonFile);
                        Log.i(LogTags.SAF_FILE_HELPER.getV(), "用户选择JSON文件");
                    } else {
                        Log.e(LogTags.SAF_FILE_HELPER.getV(), "用户选择了未知种类的文件");
                        readCallback.onError("请选择zip或json文件");
                    }
                } else {
                    Log.e(LogTags.SAF_FILE_HELPER.getV(), "无法获取文件信息");
                }
            } else {
                Log.i(LogTags.SAF_FILE_HELPER.getV(), "用户取消选择并关闭SAF");
            }
        }
    }

    /**
     * 通过SAF打开备份文件
     *
     * @param callback 处理打开文件的回调
     * @param launcher SAF启动器
     */
    public void openFileBySAF(ReadCallback callback,
                              ActivityResultLauncher<Intent> launcher) {
        Log.d(LogTags.SAF_FILE_HELPER.getV(), "正在使用SAF选择zip备份文件……");
        this.readCallback = callback;

        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] fileTypes = {"application/json", "application/zip"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, fileTypes);    //限制只能打开JSON和zip文件
            launcher.launch(intent);
            Log.d(LogTags.SAF_FILE_HELPER.getV(), "SAF启动成功");
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("SAF出错");
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "SAF出错");
        }
    }

    /**
     * 将用户选择的目标zip复制到临时目录
     *
     * @param uri 用户通过SAF选择的uri
     */
    private void copyUriToTempZip(Uri uri) {
        Log.d(LogTags.SAF_FILE_HELPER.getV(), "开始复制目标zip备份文件到临时目录……");

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
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "zip文件复制失败");

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
                Log.e(LogTags.SAF_FILE_HELPER.getV(), "无法正确关闭流");
            }
        }
    }

    /**
     * 解包临时zip文件
     */
    private void unpackZipFile() {
        try (FileInputStream fis = new FileInputStream(tempZipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            Log.d(LogTags.SAF_FILE_HELPER.getV(), "开始解压zip备份文件……");

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
                Log.d(LogTags.SAF_FILE_HELPER.getV(), "zip备份文件解压完成");
                readCallback.onZipUnpacked(tempJsonFileList);
            } else {
                readCallback.onError("zip文件为空");
                Log.w(LogTags.SAF_FILE_HELPER.getV(), "zip备份文件为空");
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("zip文件解压失败");
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "zip备份文件解压失败");
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
        writeContentToTempFile(tempJsonFile, content);
    }

    /**
     * 将字符串内容写入临时文件
     *
     * @param targetFile 待写入的目标文件
     * @param content    字符串内容
     */
    private void writeContentToTempFile(@NonNull File targetFile, String content) {
        Log.d(
                LogTags.SAF_FILE_HELPER.getV(),
                String.format(Locale.getDefault(), "正在将内容写入文件%s……", targetFile.getName())
        );
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            writer.write(content);
            writer.flush();
            Log.d(LogTags.SAF_FILE_HELPER.getV(), "文件内容写入完毕");
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            writeCallback.onError("临时文件写入失败");
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "临时文件写入失败");
        }
    }

    /**
     * 将来自SAF的uri转换为File对象
     *
     * @param uri 来自SAF的uri
     * @return 复制到临时目录下的JSON文件对象
     */
    @Nullable
    private File getFileFromDocumentUri(Uri uri) {
        try {
            //获取DocumentFile对象
            DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile.canRead()) {
                //复制文件到Android/data下的临时目录
                File tempFile = new File(tempDir, "temp_json");
                try (InputStream in = context.getContentResolver().openInputStream(uri);
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    if (in != null) {
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }

                        tempJsonFileList.add(tempFile); //加入临时文件列表
                        return tempFile;                //返回临时文件
                    } else {
                        throw new IOException("无法打开输入流");
                    }
                }
            } else {
                throw new IOException("文件无法读取");
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("JSON文件读取失败");
            Log.e(LogTags.SAF_FILE_HELPER.getV(), "JSON文件读取失败");
            return null;
        }
    }

    /**
     * 清除临时文件
     */
    public void clearTempFile() {
        Log.d(LogTags.SAF_FILE_HELPER.getV(), "开始清除临时文件……");
        boolean isFileDeleteFailed = false;

        for (File tempFile : tempJsonFileList) {
            if (tempFile.exists() && !tempFile.delete()) {
                isFileDeleteFailed = true;
            }
        }
        tempJsonFileList.clear();

        if (tempZipFile != null && !tempZipFile.delete()) {
            isFileDeleteFailed = true;
        }
        tempZipFile = null;

        if (isFileDeleteFailed) {
            Toast.makeText(context, "警告：临时文件删除失败", Toast.LENGTH_SHORT).show();
            Log.w(LogTags.SAF_FILE_HELPER.getV(), "临时文件清除失败");
        } else {
            Log.d(LogTags.SAF_FILE_HELPER.getV(), "临时文件清除完毕");
        }
    }

    /**
     * 通过uri获取文件种类
     *
     * @param uri 目标文件的uri
     * @return 文件种类
     */
    private String getMimeType(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.getType(uri); // 返回 MIME 类型，如 "image/jpeg"
    }

    /**
     * 通过SAF的Uri获取文件大小（字节）
     *
     * @param uri SAF返回的 Uri
     * @return 文件大小（字节），失败返回 -1
     */
    public long getFileSizeFromUri(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(
                uri,
                new String[]{OpenableColumns.SIZE}, // 只查询大小
                null,
                null,
                null
        )) {
            // 查询文件的元数据（只需 SIZE 和 DISPLAY_NAME）
            // 只查询大小

            if (cursor != null && cursor.moveToFirst()) {
                // 获取 SIZE 列的索引
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex); // 返回文件大小（字节）
                }
            }
        } catch (Exception e) {
            Log.e("FileUtils", "查询文件大小失败: " + e.getMessage());
        }
        return -1; // 失败返回 -1
    }
}