package com.manager.assistant.helpers.appearence;

import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.manager.assistant.ui.others.listeners.RecyclerScrollHideShowListener;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;

/**
 * 视图显示和动画帮助器
 */
public class AppearanceAnimationHelper {
    public static final int SMALL_CARD_RADIUS = 5;     //小卡片圆角大小（dp）
    public static final int MEDIUM_CARD_RADIUS = 15;   //中等卡片圆角大小（dp）

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

        Shapeable shapeableView = (Shapeable) view;
        Vibrator vibrator = (Vibrator) view.getContext()
                .getSystemService(Context.VIBRATOR_SERVICE);

        view.setOnTouchListener(new SpringAnimationOnTouchListener(shapeableView, vibrator, percentage));
    }

    /**
     * 设置视图四个边角大小
     *
     * @param context     上下文
     * @param view        实现了{@link Shapeable}的视图
     * @param topLeft     左上角圆角大小，单位dp
     * @param topRight    右上角圆角大小，单位dp
     * @param bottomLeft  左下角圆角大小，单位dp
     * @param bottomRight 右下角圆角大小，单位dp
     */
    public static void setRadius(
            Context context,
            @NonNull View view,
            float topLeft,
            float topRight,
            float bottomLeft,
            float bottomRight
    ) {
        //如果没有实现Shapeable接口，不执行任何操作
        if (!(view instanceof Shapeable)) {
            return;
        }

        Shapeable shapeable = (Shapeable) view;
        ShapeAppearanceModel model = shapeable.getShapeAppearanceModel();
        shapeable.setShapeAppearanceModel(model.toBuilder()
                .setTopLeftCornerSize(ViewEdgeHelper.dpToPx(context, topLeft))
                .setTopRightCornerSize(ViewEdgeHelper.dpToPx(context, topRight))
                .setBottomLeftCornerSize(ViewEdgeHelper.dpToPx(context, bottomLeft))
                .setBottomRightCornerSize(ViewEdgeHelper.dpToPx(context, bottomRight))
                .build()
        );
    }

    /**
     * 根据视图所在位置设置四个圆角的大小
     *
     * @param view     实现了{@link Shapeable}的视图
     * @param listSize 列表大小
     * @param position 当前视图所处的下标
     */
    public static void setRecyclerItemRadius(View view, int listSize, int position) {
        //列表长度为1则不执行任何操作
        if (listSize == 1) {
            return;
        }

        //设置新的圆角值
        Context context = view.getContext();
        if (position == 0) {
            setRadius(context, view, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS);
        } else if (position == listSize - 1) {
            setRadius(context, view, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS);
        } else {
            setRadius(context, view, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS);
        }
    }
}
