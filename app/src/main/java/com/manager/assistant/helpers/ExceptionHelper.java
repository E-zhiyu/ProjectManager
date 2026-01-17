package com.manager.assistant.helpers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ExceptionHelper {
    public static void showExceptionDialog(Context context, @NonNull Throwable e) {
        String err_message = e.getMessage();
        new MaterialAlertDialogBuilder(context)
                .setTitle("运行出错")
                .setMessage(err_message)
                .setPositiveButton("复制错误信息", (dialog, which) -> {
                    copyToClipboard(context, err_message);
                    Toast.makeText(context, "已将错误信息复制到剪贴板", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("关闭", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 将文本复制到剪贴板
     *
     * @param context 上下文（Activity 或 Application Context）
     * @param text    要复制的文本
     */
    private static void copyToClipboard(@NonNull Context context, String text) {
        // 获取系统剪贴板服务
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // 创建 ClipData 对象
        ClipData clip = ClipData.newPlainText("Copied Text", text);

        // 设置剪贴板内容
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }
}
