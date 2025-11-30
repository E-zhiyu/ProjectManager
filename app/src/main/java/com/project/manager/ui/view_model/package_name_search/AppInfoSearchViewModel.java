package com.project.manager.ui.view_model.package_name_search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.manager.helpers.PackageNameHelper;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class AppInfoSearchViewModel extends ViewModel {
    private List<AppInfo> fullAppInfoList = new ArrayList<>();  //完整的应用列表
    private final MutableLiveData<List<AppInfo>> resultsLiveData = new MutableLiveData<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Subject<String> searchSubject = PublishSubject.create();

    public void setFullAppInfoList(List<AppInfo> appInfoList) {
        this.fullAppInfoList = appInfoList;
    }

    //初始化订阅
    public void init() {
        compositeDisposable.add(
                searchSubject
                        .debounce(300, TimeUnit.MILLISECONDS)  // 1. 防抖
                        .distinctUntilChanged()               // 2. 忽略重复查询
                        .switchMap(query -> {                 // 3. 切换搜索任务
                            if (query.isEmpty()) {
                                return Observable.just(Collections.emptyList()); // 空查询返回空
                            }
                            return Observable.fromCallable(() -> PackageNameHelper.searchInFullAppList(query, fullAppInfoList)) // 实际搜索
                                    .subscribeOn(Schedulers.computation()) // 4. 指定在计算进程执行
                                    .cast(List.class) //显式声明泛型类型
                                    .onErrorResumeNext(throwable -> { // 5. 错误处理
                                        return Observable.just(Collections.emptyList());
                                    });
                        })
                        .observeOn(AndroidSchedulers.mainThread()) // 6. 切换到主线程
                        .subscribe(resultsLiveData::postValue)     // 7. 更新结果到 LiveData
        );
    }

    public void onSearchQueryChanged(String newQuery) {
        searchSubject.onNext(newQuery); // 8. 触发搜索流
    }

    public LiveData<List<AppInfo>> getResultsLiveData() {
        return resultsLiveData;
    }

    //清理资源
    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
