package com.manager.assistant.ui.pages.main.setting.components;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.manager.assistant.databinding.ViewSettingOptionBinding;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

import org.jetbrains.annotations.Contract;

/**
 * 设置项基类
 *
 * @param <C> 右侧布局中添加的组件类型
 * @param <L> 功能监听器类型
 */
abstract public class SettingOptionViewBase<C, L> {
    protected ViewSettingOptionBinding binding;     //绑定的XML视图引用
    protected C functionComponent;                  //功能组件

    public enum RadiusStyle {
        TOP,    //顶部
        MIDDLE, //中部
        BOTTOM, //底部
        SINGLE, //单独的
    }

    /**
     * 设置项构造方法
     *
     * @param context     上下文
     * @param binding     对应于XML布局中的databinding
     * @param title       标题
     * @param description 描述（可选）
     * @param iconId      左侧图标资源ID
     * @param radiusStyle 圆角类型
     */
    public SettingOptionViewBase(
            Context context,
            @NonNull ViewSettingOptionBinding binding,
            @StringRes int title,
            String description,
            @DrawableRes int iconId,
            RadiusStyle radiusStyle
    ) {
        this.binding = binding;
        initView(context);
        setTitle(title);
        setDescription(description);
        setIcon(iconId);

        setRadius(radiusStyle, context);

        //设置触摸动画
        AppearanceHelper.attachMorphAnimation(binding.getRoot());
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
        binding.getRoot().setOnLongClickListener(listener);
    }

    /**
     * 设置可见性
     *
     * @param visibility 可见性代码
     */
    public void setVisibility(int visibility) {
        binding.getRoot().setVisibility(visibility);
    }

    /**
     * 设置文本和右侧视图之间的分隔线的可见性
     *
     * @param isVisible 分割线是否可见
     */
    public void setDividerVisibility(boolean isVisible) {
        if (isVisible) {
            binding.settingViewDivider.setVisibility(View.VISIBLE);
        } else {
            binding.settingViewDivider.setVisibility(View.GONE);
        }
    }

    /**
     * 设置圆角类型
     *
     * @param radiusStyle 圆角类型
     * @param context     上下文
     */
    @Contract(pure = true)
    public void setRadius(@NonNull RadiusStyle radiusStyle, Context context) {
        switch (radiusStyle) {
            case TOP:
                AppearanceHelper.setRadius(
                        context,
                        binding.getRoot(),
                        AppearanceHelper.MEDIUM_CARD_RADIUS,
                        AppearanceHelper.MEDIUM_CARD_RADIUS,
                        AppearanceHelper.SMALL_CARD_RADIUS,
                        AppearanceHelper.SMALL_CARD_RADIUS
                );
                break;
            case MIDDLE:
                AppearanceHelper.setRadius(
                        context,
                        binding.getRoot(),
                        AppearanceHelper.SMALL_CARD_RADIUS,
                        AppearanceHelper.SMALL_CARD_RADIUS,
                        AppearanceHelper.SMALL_CARD_RADIUS,
                        AppearanceHelper.SMALL_CARD_RADIUS
                );
                break;
            case BOTTOM:
                AppearanceHelper.setRadius(
                        context,
                        binding.getRoot(),
                        AppearanceHelper.SMALL_CARD_RADIUS,
                        AppearanceHelper.SMALL_CARD_RADIUS,
                        AppearanceHelper.MEDIUM_CARD_RADIUS,
                        AppearanceHelper.MEDIUM_CARD_RADIUS
                );
                break;
            case SINGLE:
                AppearanceHelper.setRadius(
                        context,
                        binding.getRoot(),
                        AppearanceHelper.MEDIUM_CARD_RADIUS,
                        AppearanceHelper.MEDIUM_CARD_RADIUS,
                        AppearanceHelper.MEDIUM_CARD_RADIUS,
                        AppearanceHelper.MEDIUM_CARD_RADIUS
                );
                break;
        }
    }

    /**
     * 初始化视图的方法
     */
    protected abstract void initView(Context context);

    abstract public void setFunctionListener(L listener);
}
