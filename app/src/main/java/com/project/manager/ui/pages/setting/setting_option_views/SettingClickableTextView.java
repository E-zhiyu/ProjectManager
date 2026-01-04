package com.project.manager.ui.pages.setting.setting_option_views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;
import com.project.manager.helpers.ColorHelper;

public class SettingClickableTextView extends SettingOptionViewBase<ImageView, View.OnClickListener> {
    /**
     * 点击式设置项构造方法
     *
     * @param context     上下文
     * @param binding     对应于XML文件中的databinding
     * @param title       标题
     * @param description 描述（可选）
     * @param iconId      左侧图标资源
     */
    public SettingClickableTextView(Context context, ViewSettingOptionBinding binding, @StringRes int title, String description, @DrawableRes int iconId) {
        super(context, binding, title, description, iconId);
    }

    @Override
    protected void initView(Context context) {
        functionComponent = new ImageView(context);
        functionComponent.setImageResource(R.drawable.baseline_keyboard_arrow_right_24);
        functionComponent.setImageTintList(ColorStateList.valueOf(ColorHelper.getPrimaryColor(context)));
        functionComponent.setPadding(10, 10, 25, 10);
        binding.freeLayout.addView(functionComponent);
    }

    @Override
    public void setFunctionListener(View.OnClickListener listener) {
        binding.constraintLayout.setOnClickListener(listener);
    }
}
