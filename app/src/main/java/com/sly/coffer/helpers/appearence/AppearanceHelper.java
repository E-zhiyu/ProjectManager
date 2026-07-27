package com.sly.coffer.helpers.appearence;

import android.content.Context;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.sly.coffer.auxiliary.enums.RadiusStyle;
import com.sly.coffer.ui.others.listeners.SpringAnimationOnTouchListener;

/**
 * 视图显示帮助器
 */
public class AppearanceHelper {
    public static final int SMALL_CARD_RADIUS = 5;     //小卡片圆角大小（dp）
    public static final int MEDIUM_CARD_RADIUS = 20;   //中等卡片圆角大小（dp）

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
        Vibrator vibrator = view.getContext().getSystemService(Vibrator.class);

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
                .setTopLeftCornerSize(dpToPx(context, topLeft))
                .setTopRightCornerSize(dpToPx(context, topRight))
                .setBottomLeftCornerSize(dpToPx(context, bottomLeft))
                .setBottomRightCornerSize(dpToPx(context, bottomRight))
                .build()
        );
    }

    /**
     * 快速设置圆角样式
     *
     * @param view  需要设置圆角的视图
     * @param style 圆角种类
     */
    public static void setRadiusStyle(
            @NonNull View view,
            @NonNull RadiusStyle style
    ) {
        Context context = view.getContext();
        switch (style) {
            case TOP:
                setRadius(context, view, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS);
                break;
            case MIDDLE:
                setRadius(context, view, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS);
                break;
            case BOTTOM:
                setRadius(context, view, SMALL_CARD_RADIUS, SMALL_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS);
                break;
            case SINGLE:
                setRadius(context, view, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS, MEDIUM_CARD_RADIUS);
                break;
        }
    }

    /**
     * 根据视图所在位置设置四个圆角的大小
     *
     * @param view     实现了{@link Shapeable}的视图
     * @param listSize 列表大小
     * @param position 当前视图所处的下标
     */
    public static void setRecyclerItemRadius(@NonNull View view, int listSize, int position) {
        //列表长度为1或者列表为空则设置为中等圆角
        if (listSize == 1 || listSize == 0) {
            setRadiusStyle(view, RadiusStyle.SINGLE);
            return;
        }

        //设置新的圆角值
        if (position == 0) {
            setRadiusStyle(view, RadiusStyle.TOP);
        } else if (position == listSize - 1) {
            setRadiusStyle(view, RadiusStyle.BOTTOM);
        } else {
            setRadiusStyle(view, RadiusStyle.MIDDLE);
        }
    }

    /**
     * 设置视图与导航栏的距离为20dp
     *
     * @param view    需要确保不被遮挡的视图
     * @param context 上下文
     */
    public static void setMarginToNavigation(View view, Context context) {
        setMarginToNavigation(view, 20, context);
    }

    /**
     * 设置视图与导航栏的距离
     *
     * @param view    需要确保不被遮挡的视图
     * @param dp      视图与底部导航栏的距离，单位为dp
     * @param context 上下文
     */
    public static void setMarginToNavigation(View view, int dp, Context context) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            //获取系统栏的间距（包含导航栏高度）
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            //获取原本的边距
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            //加上导航栏高度并应用到视图
            lp.bottomMargin = systemBars.bottom + dpToPx(context, dp);
            v.setLayoutParams(lp);

            return windowInsets;
        });
    }

    /**
     * 将dp转换为像素
     *
     * @param context 上下文
     * @return 目标dp对应的像素数量
     */
    public static int dpToPx(@NonNull Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * 将dp转换为像素
     *
     * @param context 上下文
     * @return 目标dp对应的像素数量
     */
    public static float dpToPx(@NonNull Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕像素宽度
     */
    public static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     * @return 屏幕像素高度
     */
    public static int getScreenHeight(@NonNull Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
