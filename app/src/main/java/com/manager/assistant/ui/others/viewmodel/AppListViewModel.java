package com.manager.assistant.ui.others.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.manager.assistant.auxiliary.classes.AppInfo;
import com.manager.assistant.helpers.AppListHelper;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class AppListViewModel extends ViewModel {
    private final BehaviorProcessor<String> searchKeywordProcessor =
            BehaviorProcessor.createDefault("");    //搜索关键词处理器
    private final BehaviorProcessor<Boolean> sysAppVisibilityProcessor =
            BehaviorProcessor.createDefault(false); //搜索关键词处理器
    private boolean isSysAppVisible = false;                   //是否显示系统应用
    private List<AppInfo> appListCache = null;                 //应用列表缓存

    public boolean isSysAppVisible() {
        return isSysAppVisible;
    }

    /**
     * 获取应用列表
     *
     * @param context 上下文
     * @return 应用数据，支持响应式更新
     */
    public Flowable<List<AppInfo>> getAppListFlowable(Context context) {
        return Flowable.combineLatest(
                searchKeywordProcessor.debounce(50, TimeUnit.MILLISECONDS),
                sysAppVisibilityProcessor.debounce(50, TimeUnit.MILLISECONDS),
                (keyword, sysAppVisible) -> {
//                    if (sysAppVisible != isSysAppVisible) {
                        appListCache = AppListHelper.getInstalledApps(sysAppVisible, context);
                        isSysAppVisible = sysAppVisible;
//                    }

                    return appListCache.stream()
                            .filter(app -> keyword.isEmpty() || app.getAppName().toLowerCase().contains(keyword.toLowerCase()))
                            .sorted(Comparator.comparing(AppInfo::getAppName))
                            .collect(Collectors.toList());
                }
        );
    }

    /**
     * 执行一次搜索
     *
     * @param keyword 搜索的关键词
     */
    public void executeSearch(String keyword) {
        searchKeywordProcessor.onNext(keyword);
    }

    /**
     * 切换系统应用可见性
     */
    public void toggleSysAppVisibility(boolean isVisible) {
        sysAppVisibilityProcessor.onNext(isVisible);
    }
}
