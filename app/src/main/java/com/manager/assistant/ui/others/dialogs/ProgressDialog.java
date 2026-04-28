package com.manager.assistant.ui.others.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;

import java.util.Locale;

public class ProgressDialog extends CustomDialogBase {
    private LinearProgressIndicator progressIndicator;  //进度条
    private MaterialTextView progressText;              //进度文本
    private MaterialTextView subTitleText;              //底部副标题文本
    private final String originSubTitle;                //对话框底部副标题

    /**
     * 进度条对话框构造方法
     * @param context 上下文
     * @param dialogTitle 对话框标题
     * @param originSubTitle 初始进度条底部副标题(为null则使用XML布局的默认副标题)
     */
    public ProgressDialog(Context context, String dialogTitle, @Nullable String originSubTitle) {
        super(context, dialogTitle);
        this.originSubTitle = originSubTitle == null ? ContextCompat.getString(context, R.string.progressing) : originSubTitle;
    }

    @Override
    protected View getView() {
        //创建自定义布局
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_progress, null);

        progressIndicator = view.findViewById(R.id.progress_indicator);
        progressText = view.findViewById(R.id.progress_text);
        subTitleText = view.findViewById(R.id.sub_title_text);
        subTitleText.setText(originSubTitle);

        //默认为不确定进度模式
        setIndeterminate(true);

        return view;
    }

    /**
     * 更新进度
     *
     * @param current  当前数量
     * @param total    总数量
     * @param subTitle 对话框底部副标题
     */
    public void updateProgress(int current, int total, String subTitle) {
        if (progressIndicator != null) {
            //设置进度（0-100）
            int progressPercent = (int) ((current / (float) total) * 100);
            progressIndicator.setProgressCompat(progressPercent, true);

            //更新文本
            progressText.setText(String.format(Locale.getDefault(),
                    "%d/%d (%d%%)", current, total, progressPercent));

            //更新副标题
            subTitleText.setText(subTitle != null ? subTitle : this.originSubTitle);
        }
    }

    /**
     * 设置为不确定模式（用于初始阶段）
     *
     * @param indeterminate 是否为不确定模式
     */
    public void setIndeterminate(boolean indeterminate) {
        //如果当前状态与目标状态相同，则不执行任何操作
        if (progressIndicator.isIndeterminate() == indeterminate) {
            return;
        }

        //修改状态
        if (progressIndicator != null) {
            progressIndicator.setIndeterminate(indeterminate);

            if (indeterminate) {
                progressText.setVisibility(View.GONE);
            } else {
                progressText.setVisibility(View.VISIBLE);
            }
        }
    }
}
