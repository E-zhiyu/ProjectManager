package com.project.manager.ui.setting.settingOptions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.card.MaterialCardView;
import com.project.manager.R;
import com.project.manager.databinding.ViewSettingClickableTextBinding;

public class SettingClickableTextView extends MaterialCardView {
    private ViewSettingClickableTextBinding binding;   //绑定的XML视图引用

    public SettingClickableTextView(Context context) {
        this(context, null);
    }

    public SettingClickableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingClickableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = ViewSettingClickableTextBinding.inflate(LayoutInflater.from(context), this, true);
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
            View.OnClickListener listener) {
        setTitle(title);
        setDescription(description);
        setIcon(resId);
        binding.constraintLayout.setOnClickListener(listener);
    }
}
