package com.sly.coffer.helpers.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sly.coffer.auxiliary.classes.PickResult;

public class AccessibilityNodePicker {
    /**
     * 拾取视图
     *
     * @param service 无障碍服务
     * @param x       x轴位置
     * @param y       y轴位置
     * @return 拾取到的无障碍节点
     */
    @Nullable
    public static PickResult pick(
            @NonNull AccessibilityService service,
            float x,
            float y
    ) {
        AccessibilityNodeInfo root =
                service.getRootInActiveWindow();

        if (root == null) {
            return null;
        }

        AccessibilityNodeInfo target = findDeepestNode(
                root,
                (int) x,
                (int) y
        );
        if (target == null) {
            return null;
        }

        return createResult(target);
    }

    /**
     * 找到层级最深的节点
     *
     * @param node 起始节点
     * @param x    x轴位置
     * @param y    y轴位置
     * @return 层级最深的无障碍节点
     */
    private static AccessibilityNodeInfo findDeepestNode(
            AccessibilityNodeInfo node,
            int x,
            int y
    ) {
        if (node == null) {
            return null;
        }

        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        if (!bounds.contains(x, y)) {
            return null;
        }
        AccessibilityNodeInfo bestChild = null;
        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) {
                continue;
            }

            AccessibilityNodeInfo candidate = findDeepestNode(
                    child,
                    x,
                    y
            );
            if (candidate != null) {
                bestChild = candidate;
            }
        }

        if (bestChild != null) {
            return bestChild;
        }

        return node;
    }

    /**
     * 生成拾取结果
     *
     * @param node 拾取到的节点
     * @return 视图拾取结果
     */
    @NonNull
    private static PickResult createResult(@NonNull AccessibilityNodeInfo node) {
        PickResult result = new PickResult();

        result.viewId = node.getViewIdResourceName();
        result.content = node.getContentDescription().toString();

        CharSequence packageName = node.getPackageName();
        if (packageName != null) {
            result.packageName = packageName.toString();
        }

        return result;
    }
}
