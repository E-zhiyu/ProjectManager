package com.manager.assistant.ui.data_sync;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class UnPeekLiveData<T> extends MutableLiveData<T> {
    private int mVersion = 0;  // 全局版本号，每次 setValue 递增

    @MainThread
    @Override
    public void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        super.observe(owner, new Observer<>() {
            private int mLastVersion = mVersion;  // 观察者自己的版本号

            @Override
            public void onChanged(T t) {
                if (mLastVersion < mVersion) {
                    // 版本号变新，说明是这次新发出的事件
                    mLastVersion = mVersion;
                    observer.onChanged(t);
                }
                // 否则忽略（新观察者注册时不会收到旧值）
            }
        });
    }
}