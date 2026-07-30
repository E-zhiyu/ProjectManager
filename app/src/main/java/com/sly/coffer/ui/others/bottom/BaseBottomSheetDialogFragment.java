package com.sly.coffer.ui.others.bottom;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

abstract public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {
    protected View bottomSheetView;                 //底部对话框视图实例
    protected BottomSheetBehavior<View> behavior;   //对话框行为管理器
    private PredictiveBackCallback backCallback;    //返回回调
    private OnDismissListener onDismissListener;    //消失监听器

    public interface OnDismissListener {
        void onDismiss();
    }

    /**
     * 设置消失监听器
     *
     * @param listener 监听器实例
     */
    protected void setOnDismissListener(OnDismissListener listener) {
        onDismissListener = listener;
    }

    /**
     * 返回回调内部类
     */
    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private final class PredictiveBackCallback extends OnBackPressedCallback implements OnBackAnimationCallback {
        PredictiveBackCallback() {
            super(true);
        }

        /* Android 13+ */

        @Override
        public void onBackProgressed(@NonNull BackEvent backEvent) {
            applyInsetAnimation(backEvent.getProgress());
        }

        @Override
        public void onBackCancelled() {
            applyInsetAnimation(1f);
        }

        /* 所有版本都会回调 */

        @Override
        public void handleOnBackPressed() {
            onBackGestureInvoked();
        }

        @Override
        public void onBackInvoked() {

        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog =
                (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog sheetDialog = (BottomSheetDialog) d;

            FrameLayout bottomSheet = sheetDialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );

            if (bottomSheet != null) {
                bottomSheetView = bottomSheet;
                behavior = BottomSheetBehavior.from(bottomSheet);

                configureBehavior(behavior);
            }
        });

        //设置沉浸式导航栏
        Window window = dialog.getWindow();
        if (window != null) {
            // 允许内容延伸到系统栏（状态栏+导航栏）的边界外面
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );

            // 【适配 Android 10+ 控制器】：让导航栏上的“小横条”指针自动根据你的 App 背景变黑或变白
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.setNavigationBarContrastEnforced(false);
            }
        }

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBackCallback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (backCallback != null) {
            backCallback.remove();
            backCallback = null;
        }

        bottomSheetView = null;
        behavior = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    /**
     * 配置行为管理器
     *
     * @param behavior 行为管理器
     */
    protected void configureBehavior(@NonNull BottomSheetBehavior<View> behavior) {
        behavior.setFitToContents(true);    //弹窗高度适应内容
        behavior.setHideable(true);         //可隐藏
    }

    /**
     * 注册返回回调
     */
    private void registerBackCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            backCallback = new PredictiveBackCallback();
        }

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(this, backCallback);
    }

    /**
     * 返回调用回调
     */
    protected void onBackGestureInvoked() {
        if (behavior != null) {
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            dismissAllowingStateLoss();
        }
    }

    /**
     * 应用内陷动画
     *
     * @param progress 返回过程值（0~1）
     */
    protected void applyInsetAnimation(float progress) {
        if (bottomSheetView == null) return;

        float finalProgress = Math.max(0f, Math.min(1f, progress)); //限定范围为0~1
        ViewCompat.setOnApplyWindowInsetsListener(
                bottomSheetView,
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    int bottomInset = (int) (systemBars.bottom * (1f - finalProgress));

                    view.setPadding(
                            view.getPaddingLeft(),
                            systemBars.top,
                            view.getPaddingRight(),
                            bottomInset
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(bottomSheetView);
    }
}
