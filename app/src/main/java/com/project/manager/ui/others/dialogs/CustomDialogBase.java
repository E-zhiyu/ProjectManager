package com.project.manager.ui.others.dialogs;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

abstract public class CustomDialogBase {
    protected final Context context;                //上下文
    protected final String dialogTitle;             //窗口标题
    protected AlertDialog dialog;                   //由构建器创造的对话框

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnConfirmListener {
        void onConfirm();
    }

    public CustomDialogBase(Context context, String dialogTitle) {
        this.context = context;
        this.dialogTitle = dialogTitle;
    }

    /**
     * 获取对话框视图
     *
     * @return 通过LayoutInflater获取的对话框视图
     */
    abstract protected View getView();

    /**
     * 显示对话框
     *
     * @param confirmListener 确认回调(为null则不显示确认按钮)
     * @param cancelListener  取消回调(为null则不显示取消按钮)
     * @param isCancelable    是否可以点击对话框外部以取消
     */
    public void show(@Nullable OnConfirmListener confirmListener, @Nullable OnCancelListener cancelListener, boolean isCancelable) {
        View dialogView = getView();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(dialogTitle)
                .setView(dialogView)
                .setCancelable(isCancelable);

        if (confirmListener != null) {
            builder.setPositiveButton("确定", (dialog1, which) -> confirmListener.onConfirm());
        }
        if (cancelListener != null) {
            builder.setNegativeButton("取消", (dialog1, which) -> cancelListener.onCancel());
        }

        dialog = builder.show();
    }
}
