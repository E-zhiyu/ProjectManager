package com.sly.coffer.ui.pages.main.settings.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.sly.coffer.R;
import com.sly.coffer.auxiliary.enums.RadiusStyle;
import com.sly.coffer.databinding.ViewSettingOptionBinding;


public class SettingClickableTextView extends SettingOptionViewBase<MaterialTextView, View.OnClickListener> {
    /**
     * 点击式设置项构造方法
     *
     * @param context     上下文
     * @param binding     对应于XML文件中的databinding
     * @param title       标题
     * @param description 描述（可选）
     * @param iconId      左侧图标资源
     * @param radiusStyle 圆角类型
     */
    public SettingClickableTextView(
            Context context,
            ViewSettingOptionBinding binding,
            @StringRes int title,
            String description,
            @DrawableRes int iconId,
            RadiusStyle radiusStyle
    ) {
        super(context, binding, title, description, iconId, radiusStyle);
    }

    @Override
    protected void initView(Context context) {
        Drawable endDrawable = AppCompatResources.getDrawable(context, R.drawable.outline_keyboard_arrow_right_24);
        functionComponent = new MaterialTextView(context);
        functionComponent.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        functionComponent.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, endDrawable, null);
        functionComponent.setGravity(Gravity.CENTER_VERTICAL);
        int primaryColor = MaterialColors.getColor(
                context,
                androidx.appcompat.R.attr.colorPrimary,
                ContextCompat.getColor(context, R.color.color_primary)
        );
        TextViewCompat.setCompoundDrawableTintList(functionComponent, ColorStateList.valueOf(primaryColor));
        functionComponent.setPadding(10, 10, 25, 10);
        binding.componentLayout.addView(functionComponent);
    }

    @Override
    public void setFunctionListener(View.OnClickListener listener) {
        binding.getRoot().setOnClickListener(listener);
    }
}
