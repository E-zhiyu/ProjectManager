package com.manager.assistant.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.composite.ui.AccountUiModel;
import com.manager.assistant.data.save.db.services.AccountService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AccountFilterViewModel extends ViewModel {
    private LocalDate start, end;                                       //起止日期（闭区间）
    private final HashSet<Integer> filterTypeSet = new HashSet<>();     //种类白名单集合
    private final HashSet<Long> filterTagSet = new HashSet<>();         //标签 ID 白名单集合
    private String searchText = "";                                     //搜索关键词
    private final MutableLiveData<Boolean> needExecuteSearch = new MutableLiveData<>(false);    //是否需要执行搜索
    private final BehaviorProcessor<String> searchKeywordProcessor =
            BehaviorProcessor.createDefault("");    //搜索关键词处理器

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public HashSet<Integer> getFilterTypeSet() {
        return filterTypeSet;
    }

    public HashSet<Long> getFilterTagSet() {
        return filterTagSet;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public MutableLiveData<Boolean> getNeedExecuteSearch() {
        return needExecuteSearch;
    }

    /**
     * 加载流水记录列表数据
     *
     * @param db 数据库实例
     * @return 支持响应式更新的流水记录列表数据
     */
    public Flowable<List<AccountUiModel>> loadAccountListDataFlowable(BookkeepingDb db) {
        return searchKeywordProcessor
                .debounce(50, TimeUnit.MILLISECONDS)
                .switchMap(
                        keyword -> AccountService.loadAccountListDataFlowable(
                                        filterTagSet,
                                        filterTypeSet,
                                        start,
                                        end,
                                        searchText,
                                        db
                                )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                );
    }
}
