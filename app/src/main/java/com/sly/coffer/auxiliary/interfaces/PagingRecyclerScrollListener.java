package com.sly.coffer.auxiliary.interfaces;

public interface PagingRecyclerScrollListener {
    void onSucceed();
    void onRetry(int failCount);
    void onFailed();
}
