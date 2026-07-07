package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 自定义对话框构造器基类
 *
 * @param <VB> 自定义视图的 ViewBinding 类型
 * @param <P>  确认按钮点击回调类型
 * @param <N>  取消按钮点击回调类型
 */
public abstract class CustomDialogBuilderBase<VB extends ViewBinding, P, N> {
    protected final MaterialAlertDialogBuilder builder; //对话框构造器
    protected final VB binding;                         //绑定的自定义 XML 布局

    /**
     * 自定义对话框构造器基类构造方法
     *
     * @param context     上下文
     * @param dialogTitle 对话框标题
     */
    public CustomDialogBuilderBase(@NonNull Context context, String dialogTitle) {
        builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(dialogTitle);

        //设置自定义视图
        binding = getCustomView();
        builder.setView(binding.getRoot());
    }

    /**
     * 获取对话框视图
     *
     * @return 通过LayoutInflater获取的对话框视图
     */
    abstract protected VB getCustomView();

    /**
     * 设置确认按钮
     *
     * @param btnTitle 按钮的标题
     * @param callback 点击回调
     * @return 设置了确认按钮的对话框构造器
     */
    abstract public CustomDialogBuilderBase<VB, P, N> setPositiveButton(String btnTitle, P callback);

    /**
     * 设置取消按钮
     *
     * @param btnTitle 按钮标题
     * @param callback 取消按钮的监听器
     * @return 设置了取消按钮的对话框构造器
     */
    abstract public CustomDialogBuilderBase<VB, P, N> setNegativeButton(String btnTitle, N callback);

    /**
     * 显示对话框
     *
     * @return 显示的对话框
     */
    public AlertDialog show() {
        return builder.show();
    }

    /**
     * 创建对话框实例但不显示
     *
     * @return 对话框实例
     */
    public AlertDialog create() {
        return builder.create();
    }
}
