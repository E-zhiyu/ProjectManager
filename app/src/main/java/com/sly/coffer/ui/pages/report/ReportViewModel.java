package com.sly.coffer.ui.pages.report;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sly.coffer.auxiliary.enums.DateRangeType;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.composite.AccountWithDetailModel;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;

public class ReportViewModel extends ViewModel {
    private final MutableLiveData<Pair<LocalDate, LocalDate>> currentDateRangeLiveData;
    private final BehaviorProcessor<DateRangeType> rangeTypeProcessor =
            BehaviorProcessor.createDefault(DateRangeType.MONTH);
    private final BehaviorProcessor<Pair<LocalDate, LocalDate>> selectedDateRangeProcessor;
    private final BehaviorProcessor<LocalDate> selectedDateProcessor =
            BehaviorProcessor.createDefault(LocalDate.now());

    public ReportViewModel() {
        //计算初始日期范围
        LocalDate defaultStart = LocalDate.now().withDayOfMonth(1);
        LocalDate defaultEnd = defaultStart.plusMonths(1);
        Pair<LocalDate, LocalDate> defaultDateRangePair = new Pair<>(defaultStart, defaultEnd);

        //用户选择的日期范围
        selectedDateRangeProcessor = BehaviorProcessor.createDefault(defaultDateRangePair);

        //与当前数据相符的日期范围 LiveData
        currentDateRangeLiveData = new MutableLiveData<>(defaultDateRangePair);
    }

    /**
     * 获取与当前数据匹配的日期范围（需要先订阅收支来呀数据）
     *
     * @return 一对日期，表示数据的日期范围
     */
    public MutableLiveData<Pair<LocalDate, LocalDate>> getCurrentDateRangeLiveData() {
        return currentDateRangeLiveData;
    }

    public DateRangeType getRangeType() {
        return rangeTypeProcessor.getValue();
    }

    public LocalDate getSelectedDate() {
        return selectedDateProcessor.getValue();
    }

    public Pair<LocalDate, LocalDate> getDateRange() {
        return selectedDateRangeProcessor.getValue();
    }

    /**
     * 更新日期范围种类
     *
     * @param rangeType 更新后的日期范围种类
     */
    public void updateRangeType(DateRangeType rangeType) {
        rangeTypeProcessor.onNext(rangeType);
    }

    /**
     * 更新选中的日期
     *
     * @param selectedDate 选中的日期
     */
    public void updateSelectedDate(LocalDate selectedDate) {
        selectedDateProcessor.onNext(selectedDate);
    }

    /**
     * 更新日期范围（包含起始日期和结束日期）
     *
     * @param pair 更新后的日期范围，日期大小顺序可随意
     */
    public void updateDateRange(@NonNull Pair<LocalDate, LocalDate> pair) {
        LocalDate first = pair.first;
        LocalDate second = pair.second;
        if (!first.isAfter(second)) {
            selectedDateRangeProcessor.onNext(pair);
        } else {
            selectedDateRangeProcessor.onNext(new Pair<>(second, first));
        }
    }

    /**
     * 获取用于显示收支来源的流水数据
     *
     * @param db 数据库实例
     * @return 用于显示收支来呀的流水数据
     */
    public Flowable<List<AccountWithDetailModel>> getSourceDataFlowable(BookkeepingDb db) {
        return Flowable.combineLatest(
                        selectedDateRangeProcessor,
                        rangeTypeProcessor,
                        selectedDateProcessor,
                        (pair, type, selectedDate) -> {
                            if (type != DateRangeType.CUSTOM) {
                                LocalDate start, end;
                                switch (type) {
                                    case MONTH:
                                        start = selectedDate.withDayOfMonth(1);
                                        end = start.plusMonths(1);
                                        break;
                                    case YEAR:
                                        start = selectedDate.withDayOfYear(1);
                                        end = start.plusYears(1);
                                        break;
                                    case THAT_DAY:
                                    default:
                                        start = selectedDate;
                                        end = start.plusDays(1);
                                        break;
                                }
                                return new Pair<>(start, end);
                            } else {
                                return new Pair<>(pair.first, pair.second.plusDays(1));
                            }
                        }
                )
                .flatMap(dateRangePair -> {
                    currentDateRangeLiveData.postValue(new Pair<>(dateRangePair.first, dateRangePair.second.plusDays(-1)));
                    return db.accountDao().getAccountWithDetailFlowableByDateRange(dateRangePair.first, dateRangePair.second);
                })
                .debounce(50, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取显示每月流水记录的数据
     *
     * @param db 数据库实例
     * @return 用于显示每月流水的流水数据详情
     */
    public Flowable<List<AccountWithDetailModel>> getMonthAccountDataFlowable(BookkeepingDb db) {
        return Flowable.combineLatest(
                        rangeTypeProcessor,
                        selectedDateProcessor,
                        (type, selectedDate) -> {
                            if (type != DateRangeType.CUSTOM) {
                                LocalDate yearStart = selectedDate.withDayOfYear(1);
                                LocalDate end = yearStart.plusYears(1);
                                return new Pair<>(yearStart, end);
                            } else {
                                return new Pair<>(LocalDate.now(), LocalDate.now());
                            }
                        }
                )
                .flatMap(dateRangePair ->
                        db.accountDao().getAccountWithDetailFlowableByDateRange(
                                dateRangePair.first,
                                dateRangePair.second
                        ))
                .debounce(50, TimeUnit.MILLISECONDS);
    }
}
