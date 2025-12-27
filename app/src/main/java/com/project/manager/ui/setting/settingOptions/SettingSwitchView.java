package com.project.manager.ui.setting.settingOptions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.project.manager.R;
import com.project.manager.databinding.ViewSettingSwitchBinding;

public class SettingSwitchView extends MaterialCardView {
    private ViewSettingSwitchBinding binding;   //绑定的XML视图引用
    private MaterialSwitch switchBtn;           //开关按钮

    public SettingSwitchView(Context context) {
        this(context, null);
    }

    public SettingSwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewSettingSwitchBinding.inflate(LayoutInflater.from(context), this, true);

        switchBtn = new MaterialSwitch(new ContextThemeWrapper(context, R.style.SwitchBtnStyle));
        binding.constraintLayout.setOnClickListener(v -> switchBtn.toggle());
        binding.freeLayout.addView(switchBtn);
    }

    private void setTitle(@StringRes int title) {
        binding.titleText.setText(title);
    }

    private void setDescription(String description) {
        binding.descriptionText.setText(description);
        if (description == null || description.isEmpty()) {
            binding.descriptionText.setVisibility(GONE);

            //令标题文本居中
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(binding.constraintLayout);
            constraintSet.connect(
                    R.id.title_text,
                    ConstraintSet.BOTTOM,
                    R.id.constraint_layout,
                    ConstraintSet.BOTTOM
            );
            constraintSet.applyTo(binding.constraintLayout);
        }
    }

    private void setIcon(@DrawableRes int resId) {
        binding.iconView.setImageResource(resId);
        binding.iconView.setVisibility(resId == 0 ? INVISIBLE : VISIBLE);
    }

    /**
     * 设置各个属性
     *
     * @param title       标题文本
     * @param description 描述文本
     * @param resId       图标资源ID
     * @param listener    开关按钮状态变化监听器
     */
    public void setActions(
            @StringRes int title,
            String description,
            @DrawableRes int resId,
            MaterialSwitch.OnCheckedChangeListener listener) {
        setTitle(title);
        setDescription(description);
        setIcon(resId);
        switchBtn.setOnCheckedChangeListener(listener);
    }

    /**
     * 设置开关状态(在setActions之前调用)
     *
     * @param isChecked 目标开关状态
     */
    public void setChecked(boolean isChecked) {
        if (switchBtn != null) {
            switchBtn.setChecked(isChecked);
        }
    }
}
