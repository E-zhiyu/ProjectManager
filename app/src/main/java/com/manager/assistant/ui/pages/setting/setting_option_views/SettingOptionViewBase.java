package com.manager.assistant.ui.pages.setting.setting_option_views;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.manager.assistant.databinding.ViewSettingOptionBinding;

/**
 * 设置项基类
 *
 * @param <C> 右侧布局中添加的组件类型
 * @param <L> 功能监听器类型
 */
abstract public class SettingOptionViewBase<C, L> {
    protected ViewSettingOptionBinding binding;     //绑定的XML视图引用
    protected C functionComponent;                  //功能组件

    /**
     * 设置项构造方法
     *
     * @param context     上下文
     * @param binding     对应于XML布局中的databinding
     * @param title       标题
     * @param description 描述（可选）
     * @param iconId      左侧图标资源ID
     */
    public SettingOptionViewBase(Context context, ViewSettingOptionBinding binding, @StringRes int title, String description, @DrawableRes int iconId) {
        this.binding = binding;
        initView(context);
        setTitle(title);
        setDescription(description);
        setIcon(iconId);
    }

    protected void setTitle(@StringRes int title) {
        binding.titleText.setText(title);
    }

    protected void setDescription(String description) {
        binding.descriptionText.setText(description);
        if (description == null || description.isEmpty()) {
            binding.descriptionText.setVisibility(View.GONE);
        }
    }

    protected void setIcon(@DrawableRes int resId) {
        binding.iconView.setImageResource(resId);
        binding.iconView.setVisibility(resId == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    public C getFunctionComponent() {
        return functionComponent;
    }

    /**
     * 布局容器设置长按监听器
     *
     * @param listener 长按监听器
     */
    public void setOnLongClickListener(View.OnLongClickListener listener) {
        binding.constraintLayout.setOnLongClickListener(listener);
    }

    /**
     * 设置可见性
     *
     * @param visibility 可见性代码
     */
    public void setVisibility(int visibility) {
        binding.constraintLayout.setVisibility(visibility);
    }

    /**
     * 初始化视图的方法
     */
    protected abstract void initView(Context context);

    abstract public void setFunctionListener(L listener);
}
