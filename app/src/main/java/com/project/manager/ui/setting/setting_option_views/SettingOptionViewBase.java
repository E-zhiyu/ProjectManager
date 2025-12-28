package com.project.manager.ui.setting.setting_option_views;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintSet;

import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;

/**
 * 设置项基类
 *
 * @param <C> 右侧布局中添加的组件类型
 * @param <L> 功能监听器类型
 */
abstract public class SettingOptionViewBase<C, L> {
    protected ViewSettingOptionBinding binding;     //绑定的XML视图引用
    protected C functionComponent;                  //功能组件

    protected void setTitle(@StringRes int title) {
        binding.titleText.setText(title);
    }

    protected void setDescription(String description) {
        binding.descriptionText.setText(description);
        if (description == null || description.isEmpty()) {
            binding.descriptionText.setVisibility(View.GONE);

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
        binding.iconView.setVisibility(resId == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * 初始化视图的方法
     */
    protected abstract void initView(Context context);

    abstract public void setFunctionListener(L listener);
}
