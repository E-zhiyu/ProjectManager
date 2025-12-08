package com.project.manager.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SAFFileHelper {
    private final Context context;
    private SAFFileReadCallback readCallback;
    private SAFFileWriteCallback writeCallback;

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

    public SAFFileHelper(Context context) {
        this.context = context;
    }

    /**
     * 创建文件并写入内容
     *
     * @param callback        文件写入后的回调
     * @param mimeType        文件MIME类型
     * @param defaultFileName 默认文件名
     * @param content         要写入的内容
     * @param launcher        启动SAF的启动器
     */
    public void createFileWithContent(SAFFileWriteCallback callback,
                                      String mimeType,
                                      String defaultFileName,
                                      String content,
                                      ActivityResultLauncher<Intent> launcher) {
        this.writeCallback = callback;
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TITLE, defaultFileName);

            // 临时保存内容，以便在onFileSelected中使用
            FileContentHolder.setContent(content);

            launcher.launch(intent);
        } catch (Exception e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (callback != null) {
                callback.onError("SAF出错");
            }
        }
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
                String content = FileContentHolder.getContent();

                if (uri != null && content != null) {
                    writeContentToUri(uri, content);
                }
            }
        }
        // 处理读取文件的结果
        else {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    readContentFromUri(uri);
                }
            }
        }
    }

    /**
     * 将文件内容写入到目标文件
     *
     * @param uri     目标文件的uri
     * @param content 待写入的内容
     */
    private void writeContentToUri(Uri uri, String content) {
        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            if (outputStream != null) {
                writer.write(content);
                writer.flush();
                writeCallback.onFileWrote();
            } else {
                IOException e = new IOException("无法获取输出流");
                ExceptionHelper.showExceptionDialog(context, e);
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            writeCallback.onError("无法获取输出流");
        }
    }

    /**
     * 从目标文件中读取字符串
     *
     * @param uri 目标文件的uri
     */
    private void readContentFromUri(Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream != null) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                if (readCallback != null) {
                    readCallback.onFileRead(stringBuilder.toString());
                }
            } else {
                IOException e = new IOException("无法获取输入流");
                ExceptionHelper.showExceptionDialog(context, e);
                readCallback.onError("无法获取输入流");
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            readCallback.onError("无法获取输入流");
        }
    }

    /**
     * 辅助类，用于临时保存文件内容
     */
    private static class FileContentHolder {
        private static String content;

        public static void setContent(String content) {
            FileContentHolder.content = content;
        }

        public static String getContent() {
            return content;
        }
    }
}
