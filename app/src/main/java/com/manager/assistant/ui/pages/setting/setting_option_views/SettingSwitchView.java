package com.manager.assistant.ui.pages.setting.setting_option_views;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.CompoundButton;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.manager.assistant.R;
import com.manager.assistant.databinding.ViewSettingOptionBinding;

public class SettingSwitchView extends SettingOptionViewBase<MaterialSwitch, CompoundButton.OnCheckedChangeListener> {
    /**
     * 开关式设置项构造方法
     *
     * @param context     上下文
     * @param binding     对应于XML文件中的databinding
     * @param title       标题
     * @param description 描述（可选）
     * @param iconId      左侧图标资源
     * @param radiusStyle 圆角类型
     */
    public SettingSwitchView(Context context,
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
        functionComponent = new MaterialSwitch(new ContextThemeWrapper(context, R.style.SwitchBtnStyle));
        binding.constraintLayout.setOnClickListener(v -> functionComponent.toggle());
        binding.componentLayout.addView(functionComponent);
    }

    @Override
    public void setFunctionListener(CompoundButton.OnCheckedChangeListener listener) {
        functionComponent.setOnCheckedChangeListener(listener);
    }

    /**
     * 设置开关状态(在setActions之前调用)
     *
     * @param isChecked 目标开关状态
     */
    public void setChecked(boolean isChecked) {
        if (functionComponent != null) {
            functionComponent.setChecked(isChecked);
        }
    }
}
