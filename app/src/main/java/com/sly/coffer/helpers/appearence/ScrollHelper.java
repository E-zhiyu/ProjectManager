package com.sly.coffer.helpers.appearence;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.auxiliary.enums.LogTags;
import com.sly.coffer.auxiliary.enums.ViewTags;
import com.sly.coffer.auxiliary.interfaces.PagingRecyclerScrollListener;
import com.sly.coffer.auxiliary.interfaces.RecyclerViewScrollListener;
import com.sly.coffer.ui.others.scroller.CustomOffsetSmoothScroller;

public class ScrollHelper {
    /**
     * 将 RecyclerView平滑滚动到指定位置
     *
     * @param recyclerView        需要滚动的 RecyclerView
     * @param layoutManager       RecyclerView 的布局管理器
     * @param targetPosition      需要滚动到的目标下标
     * @param distanceThresholder 最大平滑滚动的距离，超出该距离先闪现到附近再平滑滚动
     * @param offset              滚动结束后目标视图与 RecyclerView顶部的距离(px)
     * @param listener            滚动结果监听器
     */
    public static void scrollRecycler(
            @NonNull RecyclerView recyclerView,
            LinearLayoutManager layoutManager,
            int targetPosition,
            int distanceThresholder,
            int offset,
            @Nullable RecyclerViewScrollListener listener
    ) {
        //判断布局管理器
        if (layoutManager == null) {
            if (listener != null) {
                listener.onFailed("布局管理器为空");
            }
            return;
        }

        //判断位置是否有效
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null) {
            if (listener != null) {
                listener.onFailed("无法获取适配器");
            }
            return;
        } else if (targetPosition < 0 || targetPosition >= adapter.getItemCount()) {
            if (listener != null) {
                listener.onFailed("目标位置无效");
            }
            return;
        }

        //获取可见位置并比较
        int firstCompletePos = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastCompletePos = layoutManager.findLastCompletelyVisibleItemPosition();
        int firstVisiblePos = firstCompletePos == RecyclerView.NO_POSITION ?
                layoutManager.findFirstVisibleItemPosition() :
                firstCompletePos;
        int lastVisiblePos = lastCompletePos == RecyclerView.NO_POSITION ?
                layoutManager.findLastVisibleItemPosition() :
                lastCompletePos;
        if (firstVisiblePos == RecyclerView.NO_POSITION || lastVisiblePos == RecyclerView.NO_POSITION) {
            //处理没有可见视图的情况
            if (listener != null) {
                listener.onFailed("没有可见视图");
            }
            return;
        }

        //根据距离远近采用不同的滚动方式
        int distance = Math.abs(targetPosition - firstVisiblePos);
        if (distance > distanceThresholder) {
            //然后再平滑滚动
            recyclerView.post(() -> {
                //移除旧的滚动监听器
                Object tag = recyclerView.getTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT());
                if (tag instanceof RecyclerView.OnScrollListener) {
                    recyclerView.removeOnScrollListener((RecyclerView.OnScrollListener) tag);
                    recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                }

                //添加滚动监听器
                RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        // 当滚动完全停止 (IDLE) 时再闪烁
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            //闪烁视图以提醒用户
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
                            if (viewHolder != null) {
                                AnimationHelper.blink(viewHolder.itemView);
                            } else {
                                Log.w(LogTags.SCROLL_HELPER.n(), "待闪烁的ViewHolder为null");
                            }

                            //移除滚动监听器防止用户滚动时触发闪烁
                            recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                            recyclerView.removeOnScrollListener(this);
                        }
                    }
                };
                recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), scrollListener);
                recyclerView.addOnScrollListener(scrollListener);

                //开始平滑滚动
                CustomOffsetSmoothScroller scroller = new CustomOffsetSmoothScroller(
                        recyclerView.getContext(),
                        offset
                );
                scroller.setTargetPosition(targetPosition);
                layoutManager.startSmoothScroll(scroller);
                Log.d(LogTags.SCROLL_HELPER.n(), "平滑滚动目标：" + targetPosition);

                //一定时间后瞬间滚动到附近以缩短行程
                recyclerView.post(() -> {
                    int momentPosition = targetPosition > firstVisiblePos ?
                            targetPosition - distanceThresholder :
                            targetPosition + distanceThresholder;
                    layoutManager.scrollToPositionWithOffset(momentPosition, 0);
                    Log.d(LogTags.SCROLL_HELPER.n(), "滚动到附近：" + momentPosition);
                });
            });
        } else {
            //移除旧的滚动监听器
            Object tag = recyclerView.getTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT());
            if (tag instanceof RecyclerView.OnScrollListener) {
                recyclerView.removeOnScrollListener((RecyclerView.OnScrollListener) tag);
                recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
            }

            //添加滚动监听器
            RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    // 当滚动完全停止 (IDLE) 时再闪烁
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        //闪烁视图以提醒用户
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition);
                        if (viewHolder != null) {
                            AnimationHelper.blink(viewHolder.itemView);
                        }

                        //移除滚动监听器防止用户滚动时触发闪烁
                        recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), null);
                        recyclerView.removeOnScrollListener(this);
                    }
                }
            };
            recyclerView.setTag(ViewTags.RECYCLER_SCROLL_LISTENER.getT(), scrollListener);
            recyclerView.addOnScrollListener(scrollListener);

            //开始平滑滚动
            CustomOffsetSmoothScroller scroller = new CustomOffsetSmoothScroller(
                    recyclerView.getContext(),
                    offset
            );
            scroller.setTargetPosition(targetPosition);
            layoutManager.startSmoothScroll(scroller);
        }

        if (listener != null) {
            listener.onSucceed();
        }
    }

    /**
     * 滚动带有{@link PagingDataAdapter}类型适配器的{@link RecyclerView}
     *
     * @param recyclerView     需要滚动的 RecyclerView
     * @param layoutManager    RecyclerView 的布局管理器
     * @param adapter          RecyclerView 的适配器
     * @param targetPosition   需要滚动到的位置
     * @param offset           滚动结束后目标视图到 RecyclerView 顶部的距离
     * @param maxRetryCount    最大重试次数
     * @param retryDelayMillis 重试时间间隔（毫秒）
     * @param listener         滚动状态监听器
     */
    public static void scrollPagingRecycler(
            RecyclerView recyclerView,
            LinearLayoutManager layoutManager,
            PagingDataAdapter<?, ?> adapter,
            int targetPosition,
            int offset,
            int maxRetryCount,
            int retryDelayMillis,
            PagingRecyclerScrollListener listener
    ) {
        if (layoutManager == null) {
            return;
        }

        recyclerView.post(() -> {
                    Object o;
                    try {
                        o = adapter.peek(targetPosition);
                    } catch (IndexOutOfBoundsException e) {
                        o = null;
                    }
                    if (o != null) {
                        // 已真实加载
                        scrollRecycler(
                                recyclerView,
                                layoutManager,
                                targetPosition,
                                10,
                                offset,
                                new RecyclerViewScrollListener() {
                                    @Override
                                    public void onSucceed() {
                                        listener.onSucceed();
                                    }

                                    @Override
                                    public void onFailed(String errMessage) {
                                        listener.onFailed();
                                    }
                                }
                        );
                    } else {
                        // 未加载成功，继续等待
                        recyclerView.postDelayed(new Runnable() {
                            private int failCount = 0;
                            private boolean scrollBottomOrTop = true;   //true:下次滚动到底部，false:下次滚动到顶部

                            @Override
                            public void run() {
                                //获取目标位置的数据实例，判断是否滚动成功
                                Object object;
                                try {
                                    object = adapter.peek(targetPosition);
                                } catch (IndexOutOfBoundsException e) {
                                    object = null;
                                }

                                //计算下次重试的滚动位置
                                int nextRetryPosition;
                                if (scrollBottomOrTop) {
                                    nextRetryPosition = adapter.getItemCount() - 1;
                                } else {
                                    nextRetryPosition = 0;
                                }

                                if (object != null) {
                                    scrollRecycler(
                                            recyclerView,
                                            layoutManager,
                                            targetPosition,
                                            10,
                                            offset,
                                            new RecyclerViewScrollListener() {
                                                @Override
                                                public void onSucceed() {
                                                    listener.onSucceed();
                                                }

                                                @Override
                                                public void onFailed(String errMessage) {
                                                    listener.onFailed();
                                                }
                                            }
                                    );
                                } else if (failCount < maxRetryCount) {
                                    failCount++;
                                    listener.onRetry(failCount);

                                    //跳转到边界以触发加载
                                    layoutManager.scrollToPositionWithOffset(nextRetryPosition, 0);
                                    scrollBottomOrTop = !scrollBottomOrTop;

                                    recyclerView.postDelayed(this, retryDelayMillis);
                                } else {
                                    listener.onFailed();
                                }
                            }
                        }, 50);
                    }
                }
        );
    }
}
