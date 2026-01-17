package com.manager.assistant.ui.others.dialogs;

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
     * 构建对话框
     *
     * @param confirmListener 确认回调(为null则不显示确认按钮)
     * @param cancelListener  取消回调(为null则不显示取消按钮)
     * @param isCancelable    是否可以点击对话框外部以取消
     */
    public void buildDialog(@Nullable OnConfirmListener confirmListener, @Nullable OnCancelListener cancelListener, boolean isCancelable) {
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

        dialog = builder.create();
    }

    /**
     * 显示对话框
     */
    public void show() {
        dialog.show();
    }

    /**
     * 构建对话框(可点击对话框以外的地方取消)
     *
     * @param confirmListener 确认回调(为null则不显示确认按钮)
     * @param cancelListener  取消回调(为null则不显示取消按钮)
     */
    public void buildDialog(@Nullable OnConfirmListener confirmListener, @Nullable OnCancelListener cancelListener) {
        buildDialog(confirmListener, cancelListener, true);
    }

    /**
     * 关闭对话框
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 获取对话框实例
     *
     * @return 对话框实例
     * @throws RuntimeException 对话框未构建时调用引发的异常
     */
    public AlertDialog getDialog() throws RuntimeException {
        if (dialog != null) {
            return dialog;
        } else {
            throw new RuntimeException("对话框还未被构建就尝试使用");
        }
    }
}
