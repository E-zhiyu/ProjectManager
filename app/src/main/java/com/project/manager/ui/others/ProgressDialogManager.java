package com.project.manager.ui.others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

import java.util.Locale;

public class ProgressDialogManager {
    private AlertDialog dialog;
    private LinearProgressIndicator progressIndicator;
    private MaterialTextView tvProgressText;
    private MaterialTextView tvCurrentFile;

    /**
     * 显示进度对话框
     */
    public void show(Context context, OnCancelListener cancelListener) {
        //创建自定义布局
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_progress, null);

        progressIndicator = view.findViewById(R.id.progress_indicator);
        tvProgressText = view.findViewById(R.id.progress_text);
        tvCurrentFile = view.findViewById(R.id.title_text);

        //创建 Material Dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("复制图片")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("取消", (d, which) -> d.cancel());

        builder.setOnCancelListener(d -> {
            if (cancelListener != null) {
                cancelListener.onCancel();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    /**
     * 更新进度
     *
     * @param current 当前数量
     * @param total   总数量
     * @param title   标题
     */
    public void updateProgress(int current, int total, String title) {
        if (progressIndicator != null) {
            // 设置进度（0-100）
            int progressPercent = (int) ((current / (float) total) * 100);
            progressIndicator.setProgressCompat(progressPercent, true);

            // 更新文本
            tvProgressText.setText(String.format(Locale.getDefault(),
                    "%d/%d (%d%%)", current, total, progressPercent));

            // 显示当前文件名
            tvCurrentFile.setText(title != null ? title : "正在处理...");
        }
    }

    /**
     * 设置为不确定模式（用于初始阶段）
     *
     * @param indeterminate 是否为不确定模式
     */
    public void setIndeterminate(boolean indeterminate) {
        if (progressIndicator != null) {
            progressIndicator.setIndeterminate(indeterminate);
        }
    }

    /**
     * 关闭对话框
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public interface OnCancelListener {
        void onCancel();
    }
}
