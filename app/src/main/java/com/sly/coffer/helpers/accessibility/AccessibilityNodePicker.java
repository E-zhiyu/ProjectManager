package com.sly.coffer.helpers.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.sly.coffer.auxiliary.classes.PickResult;

public final class AccessibilityNodePicker {

    private AccessibilityNodePicker() {
    }

    public static PickResult pick(
            AccessibilityService service,
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

        return createResult(target, x, y);
    }

    private static AccessibilityNodeInfo findDeepestNode(
            AccessibilityNodeInfo node,
            int x,
            int y) {

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

        return AccessibilityNodeInfo.obtain(node);
    }

    @NonNull
    private static PickResult createResult(
            @NonNull AccessibilityNodeInfo node,
            float x,
            float y) {
        PickResult result = new PickResult();
        result.x = x;
        result.y = y;

        result.viewId = node.getViewIdResourceName();
        CharSequence className = node.getClassName();
        if (className != null) {
            result.className = className.toString();
        }

        CharSequence text = node.getText();
        if (text != null) {
            result.text = text.toString();
        }

        CharSequence description = node.getContentDescription();
        if (description != null) {
            result.contentDescription =
                    description.toString();
        }

        result.clickable = node.isClickable();
        result.enabled = node.isEnabled();
        result.visible = node.isVisibleToUser();
        result.bounds = new Rect();
        node.getBoundsInScreen(result.bounds);

        CharSequence packageName = node.getPackageName();
        if (packageName != null) {
            result.packageName = packageName.toString();
        }

        return result;
    }
}
