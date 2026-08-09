package com.sly.coffer.ui.others.overlay;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class PickerOverlay {

    public interface OnPickListener {
        void onPick(float x, float y);
    }

    private final AccessibilityService service;
    private final OnPickListener listener;

    private final WindowManager windowManager;
    private View overlayView;

    /**
     * @param service  无障碍服务
     * @param listener 拾取回调
     */
    public PickerOverlay(
            AccessibilityService service,
            OnPickListener listener
    ) {
        this.service = service;
        this.listener = listener;
        windowManager = service.getSystemService(WindowManager.class);
    }

    /**
     * 显示遮罩
     */
    @SuppressLint("ClickableViewAccessibility")
    public void show() {
        if (overlayView != null) {
            return;
        }

        overlayView = new View(service);
        overlayView.setBackgroundColor(
                Color.TRANSPARENT
        );

        overlayView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float x = event.getRawX();
                float y = event.getRawY();

                if (listener != null) {
                    listener.onPick(x, y);
                }

                return true;
            }
            return true;
        });

        WindowManager.LayoutParams params =
                new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(overlayView, params);
    }

    /**
     * 隐藏遮罩
     */
    public void dismiss() {
        if (overlayView == null) {
            return;
        }

        try {
            windowManager.removeView(overlayView);
        } catch (Exception ignored) {
        }

        overlayView = null;
    }
}
