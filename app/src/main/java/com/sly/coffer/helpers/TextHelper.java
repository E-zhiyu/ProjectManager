package com.sly.coffer.helpers;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class TextHelper {
    /**
     * 获取指定无障碍节点及其子节点出现的所有文本
     *
     * @param node 任意无障碍节点
     * @return 该节点及其子节点出现过的文本集合
     */
    @NonNull
    public static Set<String> extractAllTextsFromNode(AccessibilityNodeInfo node) {
        Set<String> texts = new HashSet<>();
        if (node == null) return texts;

        // 使用栈实现非递归遍历，避免递归过深导致栈溢出
        Stack<AccessibilityNodeInfo> stack = new Stack<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            AccessibilityNodeInfo currentNode = stack.pop();
            if (currentNode == null) continue;

            // 收集文本
            CharSequence text = currentNode.getText();
            if (text != null && !TextUtils.isEmpty(text)) {
                texts.add(text.toString().toLowerCase()); // 转小写实现大小写不敏感
            }

            // 子节点入栈
            for (int i = 0; i < currentNode.getChildCount(); i++) {
                AccessibilityNodeInfo child = currentNode.getChild(i);
                if (child != null) {
                    stack.push(child);
                }
            }
        }

        return texts;
    }

    /**
     * 检查文本集合是否包含某个关键词（包含匹配）
     *
     * @param keyword 关键词
     * @param texts   待匹配的文本集合
     */
    @Contract(pure = true)
    public static boolean containsKeyword(@NonNull Set<String> texts, String keyword) {
        for (String text : texts) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一组关键词是否都在文本集合中出现（包含匹配）
     *
     * @param keywords 关键词组合
     * @param allTexts 待搜索的文本集合
     * @return keywords 中的所有关键词是否都在 allTexts 中出现
     */
    public static boolean checkGroupMatchOptimized(@NonNull String[] keywords, Set<String> allTexts) {
        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            if (!containsKeyword(allTexts, keyword)) {
                return false;
            }
        }
        return true;
    }
}
