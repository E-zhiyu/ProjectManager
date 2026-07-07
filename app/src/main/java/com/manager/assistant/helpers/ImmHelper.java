package com.manager.assistant.helpers;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

public class ImmHelper {
    /**
     * 显示输入法
     *
     * @param view 需要输入文本的视图
     */
    public static void showImm(@NonNull View view) {
        Context context = view.getContext();
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        boolean success = imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        if (!success) { //失败后再尝试
            view.postDelayed(() -> {
                view.requestFocus();
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }, 300); // 稍微长一点的延迟，确保布局彻底稳定
        }
    }

    /**
     * 隐藏输入法
     *
     * @param view 需要隐藏输入法的视图
     */
    public static void hideImm(@NonNull View view) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        view.clearFocus();
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
