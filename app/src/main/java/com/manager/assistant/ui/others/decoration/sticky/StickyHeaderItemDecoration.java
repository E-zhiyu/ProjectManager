package com.manager.assistant.ui.others.decoration.sticky;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public class StickyHeaderItemDecoration<VB extends ViewBinding> extends RecyclerView.ItemDecoration {
    private final StickyHeaderAdapter<?> adapter;
    private final BindingInflater<VB> inflater;
    private final HeaderBinder<VB, Object> binder;

    private VB binding;
    private Object lastData = null;

    public interface HeaderBinder<VB extends ViewBinding, T> {
        void bind(VB binding, T data);
    }

    public interface BindingInflater<VB extends ViewBinding> {
        VB inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToRoot);
    }

    /**
     * @param adapter  数据适配器接口
     * @param inflater ViewBinding 的 inflate 方法引用 (例如 ItemDateSeparatorBinding::inflate)
     * @param binder   如何将数据绑定到 ViewBinding 上的回调
     */
    @SuppressWarnings("unchecked")
    public <T> StickyHeaderItemDecoration(
            StickyHeaderAdapter<T> adapter,
            BindingInflater<VB> inflater,
            HeaderBinder<VB, T> binder) {
        this.adapter = adapter;
        this.inflater = inflater;
        this.binder = (HeaderBinder<VB, Object>) binder;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);

        if (parent.getChildCount() <= 0) return;

        // 1. 获取屏幕上可见第一个 View 的 Position
        View firstChild = parent.getChildAt(0);
        int firstVisiblePosition = parent.getChildAdapterPosition(firstChild);
        if (firstVisiblePosition == RecyclerView.NO_POSITION) return;

        // 2. 创建或更新头部 View
        Object data = adapter.getHeaderData(firstVisiblePosition, parent.getContext());
        if (data == null || (data instanceof String && ((String) data).isEmpty())) return;
        ensureHeaderView(parent, data);

        // 3. 计算粘性头部的 Y 轴坐标（处理“推开”效果）
        int headerTop = getHeaderTop(parent);

        // 控制列表中真实分隔符的显示与隐藏
        processChildVisibility(parent);

        // 4. 绘制头部
        canvas.save();
        canvas.translate(0, headerTop);
        binding.getRoot().draw(canvas);
        canvas.restore();
    }

    /**
     * 确保 HeaderView 被正确创建和测量大小
     *
     * @param parent 宿主 RecyclerView
     * @param data   新的头部数据
     */
    private void ensureHeaderView(RecyclerView parent, Object data) {
        if (binding == null) {
            // 实例化你自定义的日期分隔符布局（请替换为你自己的 layout id 和 view id）
            binding = inflater.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        }

        //两次文本不一样时重新渲染并测量布局
        // 2. 如果数据发生变化，重新绑定数据并触发测量
        if (!data.equals(lastData)) {
            lastData = data;

            // 调用外部传入的逻辑去更新 UI
            binder.bind(binding, data);

            // 核心：用父布局（RecyclerView）的当前宽度来约束 Header 的宽度
            int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

            View headerRoot = binding.getRoot();
            headerRoot.measure(widthSpec, heightSpec);
            headerRoot.layout(0, 0, headerRoot.getMeasuredWidth(), headerRoot.getMeasuredHeight());
        }
    }

    /**
     * 计算 Top 坐标，实现下一个 Header 顶起当前 Header 的动画效果
     *
     * @param parent 宿主 RecyclerView
     */
    private int getHeaderTop(@NonNull RecyclerView parent) {
        int headerHeight = binding.getRoot().getHeight();
        int maxTop = 0;

        // 遍历当前屏幕上可见的 View，寻找下一个分隔符
        for (int i = 1; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (position != RecyclerView.NO_POSITION && adapter.isHeader(position)) {
                // 如果下一个分隔符快要碰到顶部固定的 Header 了
                if (child.getTop() <= headerHeight) {
                    // 返回一个负值，随着滚动，把当前的 Header 往上推
                    return child.getTop() - headerHeight;
                }
                break;
            }
        }
        return maxTop;
    }

    /**
     * 动态控制真实分隔符的透明度，避免出现两个相同的分隔符视图
     *
     * @param parent 宿主 RecyclerView
     */
    private void processChildVisibility(@NonNull RecyclerView parent) {
        int headerHeight = binding.getRoot().getHeight();

        // 遍历当前屏幕上所有可见的子 View
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (position != RecyclerView.NO_POSITION && adapter.isHeader(position)) {
                // 如果这个真实的分隔符的底部，已经进入或穿过了粘性头部的覆盖区域
                if (child.getBottom() <= headerHeight) {
                    // 将其设为完全透明
                    child.setAlpha(0f);
                } else {
                    // 底部在粘性头部以下时恢复正常显示
                    child.setAlpha(1f);
                }
            }
        }
    }
}
