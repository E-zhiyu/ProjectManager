package com.project.manager.ui.setting.setting_options;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.card.MaterialCardView;
import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;

/**
 * 设置项基类
 *
 * @param <C> 右侧布局中添加的组件类型
 */
abstract public class SettingOptionViewBase<C> extends MaterialCardView {
    protected ViewSettingOptionBinding binding;     //绑定的XML视图引用
    protected C functionComponent;                  //功能组件

    public SettingOptionViewBase(Context context) {
        this(context, null);
    }

    public SettingOptionViewBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingOptionViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    protected void setTitle(@StringRes int title) {
        binding.titleText.setText(title);
    }

    protected void setDescription(String description) {
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

    protected void setIcon(@DrawableRes int resId) {
        binding.iconView.setImageResource(resId);
        binding.iconView.setVisibility(resId == 0 ? INVISIBLE : VISIBLE);
    }

    /**
     * 初始化视图的方法
     *
     * @param context 上下文
     */
    protected abstract void initView(Context context);

    /**
     * 获取功能组件
     *
     * @return 功能组件实例
     */
    public C getFunctionComponent() {
        return functionComponent;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        binding.constraintLayout.setOnClickListener(l);
    }
}
