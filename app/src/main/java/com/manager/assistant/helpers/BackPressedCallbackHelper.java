package com.manager.assistant.helpers;

import androidx.activity.OnBackPressedCallback;

import java.util.ArrayList;
import java.util.List;

public class BackPressedCallbackHelper {
    private final OnBackPressedCallback callback;                       //返回拦截器
    private final List<BackHandler> handlerList = new ArrayList<>();    //返回处理器列表

    public interface BackHandler {
        /**
         * 处理返回拦截的逻辑
         *
         * @return 是否处理了返回逻辑，如果没有处理则交给更低优先级的处理器处理
         */
        boolean handleBack();

        /**
         * 优先级定义
         *
         * @return 优先级，越大优先级越高
         */
        int getPriority();
    }

    /**
     * 返回处理器构造方法
     *
     * @param callback 注册到 Activity 中的{@link OnBackPressedCallback}实例
     */
    public BackPressedCallbackHelper(OnBackPressedCallback callback) {
        this.callback = callback;
        // 初始状态下没有添加任何模式，拦截器默认关闭
        this.callback.setEnabled(false);
    }

    /**
     * 当某个模式开启时，注册进来
     *
     * @param handler 需要注册的返回处理器
     */
    public void registerHandler(BackHandler handler) {
        if (!handlerList.contains(handler)) {
            handlerList.add(handler);
            // 按优先级降序排序
            handlerList.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        }
        updateCallbackState();
    }

    /**
     * 注销指定的处理器
     *
     * @param handler 需要注销的返回处理器
     */
    public void unregisterHandler(BackHandler handler) {
        handlerList.remove(handler);
        updateCallbackState();
    }

    /**
     * 触发返回时，执行优先级最高的那个返回逻辑
     */
    public void dispatchBackPressed() {
        if (!handlerList.isEmpty()) {
            BackHandler highestPriorityHandler = handlerList.get(0);
            boolean handled = highestPriorityHandler.handleBack();

            if (handled) {
                // 如果该模式在 handleBack 内部处理完后自己注销了，updateCallbackState 会被触发
                // 如果它没有自注销，我们需要手动更新状态
                updateCallbackState();
            }
        }
    }

    /**
     * 根据当前是否有模式在运行，动态告诉系统是否需要拦截
     * 如果列表为空，立刻设为 false，下一次滑动边缘时，系统就会展现预见性返回动画！
     */
    private void updateCallbackState() {
        callback.setEnabled(!handlerList.isEmpty());
    }
}
