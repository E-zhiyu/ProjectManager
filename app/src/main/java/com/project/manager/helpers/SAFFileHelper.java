package com.project.manager.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SAFFileHelper {
    private final Context context;
    private SAFFileCallback callback;

    public interface SAFFileCallback {
        void onFileCreated(Uri fileUri);

        void onError(String errorMessage);
    }

    public SAFFileHelper(Context context) {
        this.context = context;
    }

    /**
     * 创建文件并写入内容
     *
     * @param mimeType        文件MIME类型
     * @param defaultFileName 默认文件名
     * @param content         要写入的内容
     * @param callback        回调接口
     * @param launcher        启动SAF的启动器
     */
    public void createFileWithContent(String mimeType,
                                      String defaultFileName,
                                      String content,
                                      ActivityResultLauncher<Intent> launcher,
                                      SAFFileCallback callback) {
        this.callback = callback;

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
                callback.onError("创建文件失败");
            }
        }
    }

    /**
     * 处理SAF的结果（应在宿主的ActivityResultLauncher<Intent>类中调用）
     *
     * @param resultCode 回应代码
     * @param data       包含目标文件Uri的Intent
     */
    public void onFileSelected(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            String content = FileContentHolder.getContent();

            if (uri != null && content != null) {
                writeContentToUri(uri, content);
                return;
            }
        }

        if (callback != null) {
            callback.onError("用户取消了操作或发生错误");
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

                if (callback != null) {
                    callback.onFileCreated(uri);
                }
            } else {
                IOException e = new IOException("无法获取输出流");
                ExceptionHelper.showExceptionDialog(context, e);
            }
        } catch (IOException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            if (callback != null) {
                callback.onError("写入文件失败");
            }
        }
    }

    /**
     * 从目标文件中读取字符串
     *
     * @param uri 目标文件的uri
     * @return 读取到的字符串
     */
//    private String readContentFromUri(Uri uri) {
//
//    }

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
