package com.project.manager.ui.setting.setting_options;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.project.manager.R;
import com.project.manager.databinding.ViewSettingOptionBinding;
import com.project.manager.helpers.ColorHelper;

public class SettingClickableTextView extends SettingOptionViewBase<ImageView> {
    public SettingClickableTextView(Context context, @StringRes int title, String description, @DrawableRes int iconId) {
        super(context);

        initView(context);

        setTitle(title);
        setDescription(description);
        setIcon(iconId);
    }

    public SettingClickableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingClickableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context) {
        binding = ViewSettingOptionBinding.inflate(LayoutInflater.from(context), this, true);

        functionComponent = new ImageView(context);
        functionComponent.setImageResource(R.drawable.baseline_keyboard_arrow_right_24);
        functionComponent.setImageTintList(ColorStateList.valueOf(ColorHelper.getPrimaryColor(context)));
        functionComponent.setPadding(10, 10, 25, 10);
        binding.freeLayout.addView(functionComponent);
    }
}
