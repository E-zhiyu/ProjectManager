package com.wanderer.journal.ui.others.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.wanderer.journal.databinding.DialogMarkdownTextBinding;

import io.noties.markwon.Markwon;

public class MarkdownDialogBuilder extends CustomDialogBuilderBase<DialogMarkdownTextBinding, DialogInterface.OnClickListener, DialogInterface.OnClickListener> {
    /**
     * 显示 Markdown文本的对话框
     *
     * @param context     上下文
     * @param dialogTitle 对话框标题
     * @param markdown    需要显示的 Markdown 文本
     */
    public MarkdownDialogBuilder(@NonNull Context context, String dialogTitle, String markdown) {
        super(context, dialogTitle);

        //渲染 Markdown 文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(binding.mdTextviewInDialog, markdown);


    }

    @Override
    protected DialogMarkdownTextBinding getCustomView() {
        return DialogMarkdownTextBinding.inflate(LayoutInflater.from(builder.getContext()));
    }

    @Override
    public CustomDialogBuilderBase<DialogMarkdownTextBinding, DialogInterface.OnClickListener, DialogInterface.OnClickListener> setPositiveButton(String btnTitle, DialogInterface.OnClickListener callback) {
        builder.setPositiveButton(btnTitle, callback);
        return this;
    }

    @Override
    public CustomDialogBuilderBase<DialogMarkdownTextBinding, DialogInterface.OnClickListener, DialogInterface.OnClickListener> setNegativeButton(String btnTitle, DialogInterface.OnClickListener callback) {
        builder.setNegativeButton(btnTitle, callback);
        return this;
    }
}
