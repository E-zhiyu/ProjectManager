package com.sly.coffer.ui.pages.main.settings.sub.media;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sly.coffer.auxiliary.classes.MediaFileInfo;
import com.sly.coffer.helpers.file.MediaHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class MediaListViewModel extends ViewModel {
    private final MutableLiveData<Ordering> orderingLiveData = new MutableLiveData<>(Ordering.TIME);
    private final BehaviorProcessor<Boolean> isInOrderProcessor = BehaviorProcessor.createDefault(true);  //是否顺序排序
    private final BehaviorProcessor<Ordering> orderingProcessor = BehaviorProcessor.createDefault(Ordering.TIME);    //按照什么排序

    public boolean isInOrder() {
        return isInOrderProcessor.getValue() == null || isInOrderProcessor.getValue();
    }

    public void setInOrder(boolean inOrder) {
        isInOrderProcessor.onNext(inOrder);
    }

    public MutableLiveData<Ordering> getOrdering() {
        return orderingLiveData;
    }

    /**
     * 设置排序种类
     *
     * @param ordering 排序种类
     */
    public void setOrdering(Ordering ordering) {
        orderingProcessor.onNext(ordering);
    }

    /**
     * 获取媒体文件数据
     *
     * @param context 上下文
     * @return 媒体文件数据列表
     */
    public Flowable<List<MediaFileInfo>> getMediaFileInfoFlowable(Context context) {
        return Flowable.combineLatest(
                orderingProcessor.debounce(50, TimeUnit.MILLISECONDS),
                isInOrderProcessor.debounce(50, TimeUnit.MILLISECONDS),
                (ordering, isInOrder) -> {
                    List<MediaFileInfo> infoList = MediaHelper.readMediaDir(context);

                    //进行排序
                    switch (ordering) {
                        case SIZE:
                            infoList.sort(Comparator.comparing(MediaFileInfo::getSize));
                            break;
                        case NAME:
                            infoList.sort(Comparator.comparing(MediaFileInfo::getName));
                            break;
                        case TIME:
                        default:
                            infoList.sort(Comparator.comparing(MediaFileInfo::getCreatedTimeStamp));
                    }

                    if (!isInOrder) {
                        Collections.reverse(infoList);
                    }

                    return infoList;
                }
        );
    }
}
