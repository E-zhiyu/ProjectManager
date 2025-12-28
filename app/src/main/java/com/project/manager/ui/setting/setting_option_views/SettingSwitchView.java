package com.project.manager.ui.setting.setting_option_views;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.CompoundButton;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;

public class SettingSwitchView extends SettingOptionViewBase<MaterialSwitch, CompoundButton.OnCheckedChangeListener> {
    public SettingSwitchView(Context context, ViewSettingOptionBinding binding, @StringRes int title, String description, @DrawableRes int iconId) {
        this.binding = binding;
        initView(context);
        setTitle(title);
        setDescription(description);
        setIcon(iconId);
    }

    @Override
    protected void initView(Context context) {
        functionComponent = new MaterialSwitch(new ContextThemeWrapper(context, R.style.SwitchBtnStyle));
        binding.constraintLayout.setOnClickListener(v -> functionComponent.toggle());
        binding.freeLayout.addView(functionComponent);
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
