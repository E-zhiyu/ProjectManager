package com.project.manager.ui.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

import io.noties.markwon.Markwon;

public class UpdateLogHelper {
    private static final String update_log_md = "# v1.2.0  \n" +
            "## 新增内容  \n" +
            "- 设置界面添加更新日志查看功能  \n" +
            "- 添加标签和标签分组合并功能  \n" +
            "- 标签修改和新增标签界面添加有关分组名称输入的提示  \n" +
            "## 修改内容  \n" +
            "- 调整标签选择弹窗内容的边距，提升曲面屏设备的观感  \n" +
            "- 标签编辑页面的标签文本视图添加点击时的波纹效果  \n" +
            "- 为透明背景的按钮添加点击时的波纹效果  \n" +
            "- 报表界面的收支来源改为根据金额降序排序  \n" +
            "- 如果输入流水数据时未输入备注则会使用默认备注  \n" +
            "- 添加标签名称和分组名称不能同名的输入限制  \n" +
            "## BUG修复  \n" +
            "- 修复记录两位小数的金额时可能导致报表界面出现精度问题的BUG  \n" +
            "# v1.1.2  \n" +
            "## BUG修复  \n" +
            "- 修复标签较多时标签编辑页面无法正常滚动的BUG  \n" +
            "# v1.1.1  \n" +
            "## BUG修复  \n" +
            "- 修复报表界面无法滚动至底部的BUG  \n" +
            "# v1.1.0  \n" +
            "## BUG修复  \n" +
            "- 修复编辑流水数据时删除该流水记录的标签对应的分组后不重启APP并重新导入删除前的流水数据，会导致标签文本框无法自动填充标签名称的BUG  \n" +
            "- 修复删除流水数据输入界面中输入的标签后流水数据输入界面无法清空标签的BUG  \n" +
            "## 新增内容  \n" +
            "- 报表界面添加收支来源数据  \n" +
            "- 为一些界面添加标题栏的返回按钮（主界面除外）  \n" +
            "## 更改内容  \n" +
            "- 报表界面的日期选择对话框会记住上一次选择的日期  \n" +
            "- 更改报表界面的日期选择逻辑  \n" +
            "- 更改关于软件弹窗的内容  \n" +
            "# v1.0.0  \n" +
            "第一个版本发布";

    public static void showUpdateLogDialog(Context context) {
        View update_dialog_view = LayoutInflater.from(context)
                .inflate(R.layout.md_textview_in_dialog, null);
        MaterialTextView text_view = update_dialog_view.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(context);
        markwon.setMarkdown(text_view, update_log_md);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.update_log)
                .setView(update_dialog_view)
                .setPositiveButton("关闭", ((dialog, which) -> dialog.dismiss()))
                .show();
    }
}
