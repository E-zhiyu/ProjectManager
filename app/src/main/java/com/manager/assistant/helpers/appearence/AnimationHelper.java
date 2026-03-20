package com.manager.assistant.helpers.appearence;

import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.Shapeable;
import com.manager.assistant.ui.others.listeners.RecyclerScrollHideShowListener;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;

public class AnimationHelper {
    /**
     * 设置下滑隐藏浮动按钮
     *
     * @param recyclerView 待检测下滑行为的RecyclerView
     * @param btn          需要隐藏的浮动按钮
     */
    public static void setupFloatingBtnBehaviour(@NonNull RecyclerView recyclerView, FloatingActionButton btn) {
        recyclerView.addOnScrollListener(new RecyclerScrollHideShowListener() {
            @Override
            public void onHide() {
                btn.hide();
            }

            @Override
            public void onShow() {
                btn.show();
            }
        });
    }

    /**
     * 将根布局内的所有MaterialButton和FAB组件添加点击时的圆角变化动画
     *
     * @param root 根布局
     */
    public static void setupAllChildMorphAnimation(@NonNull ViewGroup root) {
        for (int index = 0; index < root.getChildCount(); index++) {
            View child = root.getChildAt(index);
            if (child instanceof MaterialButton || child instanceof FloatingActionButton) {
                attachMorphAnimation(child);
            } else if (child instanceof ViewGroup) {
                setupAllChildMorphAnimation((ViewGroup) child);
            }
        }
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画（按下的圆角为8dp）
     *
     * @param view 目标视图 (如 MaterialButton, FAB 等)
     */
    public static void attachMorphAnimation(View view) {
        attachMorphAnimation(view, 0.4f);
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画（每个角分别计算圆角半径）
     *
     * @param view       目标视图 (如 MaterialButton, FAB 等)
     * @param percentage 按下时的圆角半径与初始圆角半径的比例 (单位: dp)
     */
    public static void attachMorphAnimation(View view, float percentage) {
        if (!(view instanceof Shapeable)) {
            throw new IllegalArgumentException("View must implement Shapeable");
        }

        Shapeable shapeable = (Shapeable) view;
        Vibrator vibrator = (Vibrator) view.getContext()
                .getSystemService(Context.VIBRATOR_SERVICE);

        view.setOnTouchListener(new SpringAnimationOnTouchListener(shapeable, vibrator, percentage));
    }
}
