package com.project.manager.ui.setting.setting_option_views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;
import com.project.manager.helpers.ColorHelper;

public class SettingSpinnerView extends SettingOptionViewBase<MaterialTextView, View.OnClickListener> {
    /**
     * 下拉框设置项构造方法
     *
     * @param context     上下文
     * @param binding     对应于XML文件中的databinding
     * @param title       标题
     * @param description 描述（可选）
     * @param iconId      左侧图标资源
     */
    public SettingSpinnerView(Context context, ViewSettingOptionBinding binding, @StringRes int title, String description, @DrawableRes int iconId) {
        super(context, binding, title, description, iconId);
    }

    @Override
    protected void initView(Context context) {
        Drawable endDrawable = AppCompatResources.getDrawable(context, R.drawable.baseline_unfold_more_24);

        functionComponent = new MaterialTextView(context);
        functionComponent.setTextSize(15);
        functionComponent.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, endDrawable, null);
        TextViewCompat.setCompoundDrawableTintList(functionComponent, ColorStateList.valueOf(ColorHelper.getPrimaryColor(context)));
        functionComponent.setPadding(10, 10, 25, 10);
        binding.freeLayout.addView(functionComponent);
    }

    @Override
    public void setFunctionListener(View.OnClickListener listener) {
        binding.constraintLayout.setOnClickListener(listener);
    }

    /**
     * 设置下拉框文本
     *
     * @param text 目标文本
     */
    public void setSpinnerText(String text) {
        functionComponent.setText(text);
    }
}
