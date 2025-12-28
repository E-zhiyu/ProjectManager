package com.project.manager.ui.setting.setting_options;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;

public class SettingSwitchView extends SettingOptionViewBase<MaterialSwitch> {
    public SettingSwitchView(Context context, @StringRes int title, String description, @DrawableRes int iconId) {
        super(context);

        initView(context);

        setTitle(title);
        setDescription(description);
        setIcon(iconId);
    }

    public SettingSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context) {
        binding = ViewSettingOptionBinding.inflate(LayoutInflater.from(context), this, true);

        functionComponent = new MaterialSwitch(new ContextThemeWrapper(context, R.style.SwitchBtnStyle));
        binding.constraintLayout.setOnClickListener(v -> functionComponent.toggle());
        binding.freeLayout.addView(functionComponent);
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
