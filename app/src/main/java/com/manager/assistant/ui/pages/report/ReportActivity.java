package com.manager.assistant.ui.pages.report;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.manager.assistant.R;
import com.manager.assistant.auxiliary.classes.CustomDateTimeFormatter;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.auxiliary.enums.DateRangeType;
import com.manager.assistant.data.classes.AmountProportionInfo;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.composite.AccountWithDetailModel;
import com.manager.assistant.databinding.ActivityReportBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.helpers.appearence.VisibilityHelper;
import com.manager.assistant.helpers.time.DateTimePickerHelper;
import com.manager.assistant.ui.others.viewmodel.ReportViewModel;

import org.jetbrains.annotations.Contract;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReportActivity extends AppCompatActivity {
    private ActivityReportBinding binding;  //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.scrollLayout.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        observeLiveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //日期选择按钮
        binding.dateSelectBtn.setOnClickListener(view -> {
            ReportViewModel viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
            showDatePickerDialog(viewModel.getRangeType());
        });
        binding.dateSelectBtn.setText(LocalDate.now().format(CustomDateTimeFormatter.LOCAL_DATE));

        //日期范围种类选择按钮
        binding.dateRangeSelectBtn.addOnCheckedChangeListener((materialButton, b) -> {
            if (b) {
                showDateRangeSelectPopupMenu(materialButton);
            }
        });

        //日期范围和金额卡片
        AppearanceHelper.setRadius(
                this,
                binding.dateCard,
                AppearanceHelper.MEDIUM_CARD_RADIUS,
                AppearanceHelper.MEDIUM_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS
        );
        AppearanceHelper.setRadius(
                this,
                binding.amountCard,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.SMALL_CARD_RADIUS,
                AppearanceHelper.MEDIUM_CARD_RADIUS,
                AppearanceHelper.MEDIUM_CARD_RADIUS
        );

        //收支来源
        AmountProportionAdapter expenseAdapter = new AmountProportionAdapter();
        binding.expenseSourceRecycler.setAdapter(expenseAdapter);
        AmountProportionAdapter incomeAdapter = new AmountProportionAdapter();
        binding.incomeSourceRecycler.setAdapter(incomeAdapter);
        ReportViewModel viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(viewModel.getSourceDataFlowable(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        modelList -> {
                            //收入来源
                            List<AccountWithDetailModel> incomeModelList = modelList.stream()
                                    .filter(model -> model.getAccount().getType() == AccountType.INCOME.ordinal())
                                    .collect(Collectors.toList());
                            Pair<List<AmountProportionInfo>, Double> incomePair = convertToTagProportions(incomeModelList);
                            List<AmountProportionInfo> incomeProportionList = incomePair.first;
                            if (incomeProportionList.isEmpty()) {
                                VisibilityHelper.toggleViewExpansion(
                                        binding.scrollLayout,
                                        false,
                                        () -> incomeAdapter.submitList(new ArrayList<>()),
                                        binding.incomeSourceCard
                                );
                            } else {
                                incomeAdapter.submitList(
                                        incomeProportionList,
                                        () -> VisibilityHelper.toggleViewExpansion(
                                                binding.scrollLayout,
                                                true,
                                                null,
                                                binding.incomeSourceCard
                                        )
                                );
                            }

                            //支出来源
                            List<AccountWithDetailModel> expenseModelList = modelList.stream()
                                    .filter(model -> model.getAccount().getType() == AccountType.EXPENSE.ordinal())
                                    .collect(Collectors.toList());
                            Pair<List<AmountProportionInfo>, Double> expensePair = convertToTagProportions(expenseModelList);
                            List<AmountProportionInfo> expenseProportionList = expensePair.first;
                            if (expenseProportionList.isEmpty()) {
                                VisibilityHelper.toggleViewExpansion(
                                        binding.scrollLayout,
                                        false,
                                        () -> expenseAdapter.submitList(new ArrayList<>()),
                                        binding.expenseSourceCard
                                );
                            } else {
                                expenseAdapter.submitList(
                                        expenseProportionList,
                                        () -> VisibilityHelper.toggleViewExpansion(
                                                binding.scrollLayout,
                                                true,
                                                null,
                                                binding.expenseSourceCard
                                        )
                                );
                            }

                            //结余、收支文本
                            double income = incomePair.second;
                            double expense = expensePair.second;
                            double balance = income - expense;
                            binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", balance));
                            binding.incomeAndExpenseText.setText(String.format(Locale.getDefault(), "+%.2f/-%.2f", income, expense));
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //每月结余
        AmountProportionAdapter monthAdapter = new AmountProportionAdapter();
        binding.monthAccountRecycler.setAdapter(monthAdapter);
        disposable.add(viewModel.getMonthAccountDataFlowable(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        modelList -> {
                            if (modelList.isEmpty()) {
                                VisibilityHelper.toggleViewExpansion(
                                        binding.scrollLayout,
                                        false,
                                        () -> monthAdapter.submitList(new ArrayList<>()),
                                        binding.monthAccountCard
                                );
                            } else {
                                List<AmountProportionInfo> proportionInfoList = convertToMonthProportions(modelList);
                                monthAdapter.submitList(
                                        proportionInfoList,
                                        () -> VisibilityHelper.toggleViewExpansion(
                                                binding.scrollLayout,
                                                true,
                                                null,
                                                binding.monthAccountCard
                                        )
                                );
                            }
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 观察 ViewModel 中的 LiveData
     */
    private void observeLiveData() {
        ReportViewModel reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        reportViewModel.getCurrentDateRangeLiveData().observe(this, rangePair -> {
            if (rangePair == null) {
                binding.dateRangeText.setText(R.string.not_applicable);
                return;
            }

            if (!rangePair.first.isEqual(rangePair.second)) {
                String dateRangeStr = String.format(
                        Locale.getDefault(),
                        "%s ~ %s",
                        rangePair.first.format(CustomDateTimeFormatter.LOCAL_DATE),
                        rangePair.second.format(CustomDateTimeFormatter.LOCAL_DATE)
                );
                binding.dateRangeText.setText(dateRangeStr);
            } else {
                binding.dateRangeText.setText(rangePair.first.format(CustomDateTimeFormatter.LOCAL_DATE));
            }
        });
    }

    /**
     * 将 AccountWithDetailModel 列表转换为根据标签分类的比例列表，并返回总金额
     *
     * @param modelList Room 查询出来的原始数据
     * @return 转换后的比例数据列表和总金额
     */
    @NonNull
    @Contract("null -> new")
    private Pair<List<AmountProportionInfo>, Double> convertToTagProportions(List<AccountWithDetailModel> modelList) {
        if (modelList == null || modelList.isEmpty()) {
            return new Pair<>(new ArrayList<>(), 0.0);
        }

        final String OTHERS_NAME = ContextCompat.getString(this, R.string.others);
        Map<String, Double> amountMap = new HashMap<>();
        double totalAmount = 0.0;
        for (AccountWithDetailModel model : modelList) {
            AccountEntity account = model.getAccount();
            List<TagEntity> tagList = model.getTagList();
            double amount = account.getAmount();

            //根据标签分类
            if (!tagList.isEmpty()) {
                for (TagEntity tag : tagList) {
                    String name = tag.getName();
                    Double oldAmount = amountMap.getOrDefault(name, 0.0);
                    if (oldAmount != null) {
                        amountMap.put(name, oldAmount + amount);
                    } else {
                        amountMap.put(name, amount);
                    }
                }
            } else {
                //处理没有被标签标记的流水记录
                Double oldAmount = amountMap.getOrDefault(OTHERS_NAME, 0.0);
                if (oldAmount != null) {
                    amountMap.put(OTHERS_NAME, oldAmount + amount);
                } else {
                    amountMap.put(OTHERS_NAME, amount);
                }
            }
            totalAmount += amount;
        }

        //转换为 AmountProportionInfo 列表
        List<AmountProportionInfo> proportionList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : amountMap.entrySet()) {
            String name = entry.getKey();
            double amount = entry.getValue();

            // 计算百分比（防止分母为 0）
            int percentage = 0;
            if (totalAmount > 0) {
                // 使用 Math.round 四舍五入计算百分比
                percentage = (int) Math.round(amount * 100 / totalAmount);
            }

            proportionList.add(new AmountProportionInfo(percentage, amount, name));
        }
        proportionList.sort(Comparator.comparing(AmountProportionInfo::getAmount).reversed());  //从大到小排序

        return new Pair<>(proportionList, totalAmount);
    }

    /**
     * 转换为每月结余占比
     *
     * @param modelList 从数据库读出的原始数据
     * @return 金额占比数据列表
     */
    @NonNull
    private List<AmountProportionInfo> convertToMonthProportions(@NonNull List<AccountWithDetailModel> modelList) {
        Map<Integer, Double> amountMap = new HashMap<>();
        double totalBalance = 0.0;
        AccountType[] types = AccountType.values();
        for (int i = 1; i <= 12; i++) {
            amountMap.put(i, 0.0);
        }

        //根据月份分类并存放到 Map 中
        for (AccountWithDetailModel model : modelList) {
            AccountEntity account = model.getAccount();
            AccountType type = types[account.getType()];
            double amount = account.getAmount();
            int monthValue = account.getDateTime().getMonthValue();

            if (type.isExpenseType()) {
                Double oldAmount = amountMap.getOrDefault(monthValue, 0.0);
                if (oldAmount != null) {
                    amountMap.put(monthValue, oldAmount - amount);
                } else {
                    amountMap.put(monthValue, -amount);
                }
            } else if (type.isIncomeType()) {
                Double oldAmount = amountMap.getOrDefault(monthValue, 0.0);
                if (oldAmount != null) {
                    amountMap.put(monthValue, oldAmount + amount);
                } else {
                    amountMap.put(monthValue, amount);
                }
            }
            totalBalance += amount;
        }

        //转换为 AmountProportionInfo 列表
        List<AmountProportionInfo> proportionList = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : amountMap.entrySet()) {
            String name = String.format(Locale.getDefault(), "%d月", entry.getKey());
            double amount = entry.getValue();

            // 计算百分比（防止分母为 0）
            int percentage = 0;
            if (totalBalance != 0) {
                // 使用 Math.round 四舍五入计算百分比
                percentage = Math.abs((int) Math.round(amount * 100 / totalBalance));
            }

            proportionList.add(new AmountProportionInfo(percentage, amount, name));
        }

        return proportionList;
    }

    /**
     * 弹出日期选择页
     *
     * @param dateRangeType 日期范围种类，决定了是日期选择对话框还是日期范围选择对话框
     */
    private void showDatePickerDialog(DateRangeType dateRangeType) {
        ReportViewModel viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        if (dateRangeType != DateRangeType.CUSTOM) {
            LocalDate selectedDate = viewModel.getSelectedDate();
            DateTimePickerHelper.selectDate(
                    selectedDate,
                    getSupportFragmentManager(),
                    selection -> {
                        //更新 ViewModel 中的选中日期的数据
                        LocalDate selected = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                        viewModel.updateSelectedDate(selected);

                        //更新日期选中按钮文本
                        binding.dateSelectBtn.setText(selected.format(CustomDateTimeFormatter.LOCAL_DATE));

                        //更新日期范围
                        Pair<LocalDate, LocalDate> dateRangePair = getDateRangePair(selected, viewModel.getRangeType());
                        viewModel.updateDateRange(dateRangePair);
                    }
            );
        } else {
            Pair<LocalDate, LocalDate> dateRangePair = viewModel.getDateRange();
            DateTimePickerHelper.selectDateRange(
                    dateRangePair.first,
                    dateRangePair.second,
                    getSupportFragmentManager(),
                    this,
                    selection -> {
                        //更新流水日期范围种类
                        viewModel.updateRangeType(DateRangeType.CUSTOM);

                        //更新日期范围
                        LocalDate selectStart = DateTimePickerHelper.getLocalDateFromTimeMilli(selection.first);
                        LocalDate selectEnd = DateTimePickerHelper.getLocalDateFromTimeMilli(selection.second);
                        Pair<LocalDate, LocalDate> selectedDateRangePair = new Pair<>(selectStart, selectEnd);
                        viewModel.updateDateRange(selectedDateRangePair);

                        //更换日期选择按钮的文本
                        binding.dateSelectBtn.setText(R.string.select_date);
                    }
            );
        }
    }

    /**
     * 获取日期范围
     *
     * @param selectedDate 选中的日期
     * @param type         日期范围种类
     * @return 计算得到的日期范围
     */
    @NonNull
    @Contract("_, _ -> new")
    private Pair<LocalDate, LocalDate> getDateRangePair(LocalDate selectedDate, @NonNull DateRangeType type) {
        switch (type) {
            case MONTH:
                LocalDate monthStart = selectedDate.withDayOfMonth(1);
                return new Pair<>(monthStart, monthStart.plusMonths(1));
            case YEAR:
                LocalDate yearStart = selectedDate.withDayOfYear(1);
                return new Pair<>(yearStart, yearStart.plusYears(1));
            case THAT_DAY:
            default:
                return new Pair<>(selectedDate, selectedDate);
        }
    }

    /**
     * 显示日期范围选择的PopupMenu
     *
     * @param anchor PopupMenu 的锚点
     */
    private void showDateRangeSelectPopupMenu(View anchor) {
        ReportViewModel viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        PopupMenu dateRangeSelectMenu = new PopupMenu(this, anchor);
        dateRangeSelectMenu.getMenuInflater().inflate(R.menu.popup_menu_date_range_select, dateRangeSelectMenu.getMenu());

        dateRangeSelectMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_today) {
                viewModel.updateRangeType(DateRangeType.THAT_DAY);
                return true;
            } else if (item.getItemId() == R.id.action_this_month) {
                viewModel.updateRangeType(DateRangeType.MONTH);
                return true;
            } else if (item.getItemId() == R.id.action_this_year) {
                viewModel.updateRangeType(DateRangeType.YEAR);
                return true;
            } else if (item.getItemId() == R.id.action_custom) {
                showDatePickerDialog(DateRangeType.CUSTOM);
                return true;
            }

            return false;
        });

        //设置菜单消失监听并显示菜单
        dateRangeSelectMenu.setOnDismissListener(menu -> binding.dateRangeSelectBtn.setChecked(false));
        dateRangeSelectMenu.show();
    }

//    /**
//     * 加载流水信息并生成报表数据
//     */
//    @NonNull
//    private List<ReportRunningAccountData> loadReportData(@NonNull DateRangeType dateRangeType) throws SQLiteException {
//        List<ReportRunningAccountData> dataList = new ArrayList<>();
//        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(this);
//        SQLiteDatabase db = dbHelper.openReadLink();
//
//        String[] columns = new String[]{
//                Columns.AMOUNT.toString(),
//                Columns.TYPE.toString(),
//                Columns.TAG_NO.toString(),
//                Columns.DATETIME.toString()
//        };
//        String selection = Columns.DATETIME + ">=? AND " + Columns.DATETIME + "<?";
//
//        //根据日期范围设置selection语句的参数
//        LocalDate start, end;
//        start = end = selectedDate;
//        switch (dateRangeType) {
//            case TODAY:
//                end = selectedDate.plusDays(1);
//                break;
//            case THIS_MONTH:
//                start = selectedDate.withDayOfMonth(1);
//                end = start.plusMonths(1);
//                break;
//            case RECENT_3_MONTH:
//                end = selectedDate.plusMonths(1).withDayOfMonth(1);
//                start = end.plusMonths(-3);
//                break;
//            case THIS_YEAR:
//                start = selectedDate.withMonth(1).withDayOfMonth(1);
//                end = start.plusYears(1);
//                break;
//            case CUSTOM:
//                start = this.start;
//                end = this.end.plusDays(1); //由于包含结束的日期，所以需要额外加一天
//                break;
//        }
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String[] selectionArgs = {
//                start.format(formatter),
//                end.format(formatter)
//        };
//
//        Cursor basicCursor = db.query(
//                Tables.BASIC.toString(),
//                columns,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                null
//        );
//
//        while (basicCursor.moveToNext()) {
//            AccountType type = AccountType.valueOf(basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
//            double amount = basicCursor.getDouble(basicCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
//            long tagNo = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
//            String datetime = basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));
//
//            //获取月份
//            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//            LocalDateTime dateTime = LocalDateTime.parse(datetime, timeFormatter);
//            int month = dateTime.getMonthValue();
//
//            ReportRunningAccountData oneRecordedData = new ReportRunningAccountData(type, amount, tagNo, month);
//            dataList.add(oneRecordedData);
//        }
//
//        basicCursor.close();
//        db.close();
//        return dataList;
//    }
//
//    /**
//     * 更新收支来源视图
//     *
//     * @param dataList 更新视图所需的数据
//     */
//    private void updateAccountSource(@NonNull List<ReportRunningAccountData> dataList) {
//        //清空旧数据
//        shownExpense = 0;
//        shownIncome = 0;
//        double balance = 0; //结余
//        incomeSourceInfoList.clear();
//        expenseSourceInfoList.clear();
//
//        //将收支卡片都暂时缩小至消失
//        ScaleAnimator.hide(binding.expenseSourceCard);
//        ScaleAnimator.hide(binding.incomeSourceCard);
//
//        //解析新数据
//        for (ReportRunningAccountData data : dataList) {
//            AccountType type = data.getType();
//            double amount = data.getAmount();
//            long tagNo = data.getTagNo();
//
//            //获取收入或者支出列表的引用以便操作其中的元素
//            List<AccountSourceInfo> expenseOrIncome;
//            if (type.isIncomeType()) {
//                shownIncome += amount;
//                balance += amount;
//                expenseOrIncome = incomeSourceInfoList;
//            } else if (type.isExpenseType()) {
//                shownExpense += amount;
//                balance -= amount;
//                expenseOrIncome = expenseSourceInfoList;
//            } else {
//                continue;   //既不是收入也不是支出则直接跳过
//            }
//
//            //判断目标列表是否为空
//            int index = isContainedInArray(expenseOrIncome, tagNo);
//            if (index != -1) {      //判断是否查询到对应的来源卡片
//                expenseOrIncome.get(index).amountAdd(amount);
//            } else {
//                if (tagNo != 0) {  //判断该流水记录是否有标签
//                    String tagName = TagDataController.tagNoTransToName(tagNo, this);
//                    AccountSourceInfo newSource = new AccountSourceInfo(amount, tagName, tagNo);
//                    expenseOrIncome.add(newSource);
//                } else {
//                    AccountSourceInfo otherSource = new AccountSourceInfo(amount, "其他", tagNo);
//                    expenseOrIncome.add(otherSource);
//                }
//            }
//        }
//
//        //更新文本视图
//        binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", balance));
//        binding.expenseIncomeText.setText(String.format(
//                Locale.getDefault(),
//                "支出:%.2f | 收入:%.2f",
//                shownExpense, shownIncome
//        ));
//
//        //计算各来源的收支占比
//        for (AccountSourceInfo expenseSourceCard : expenseSourceInfoList) {
//            double sourceAmount = expenseSourceCard.getAmount();
//            int percentage = (int) (sourceAmount * 100 / shownExpense);
//            expenseSourceCard.setPercentage(percentage);
//        }
//        for (AccountSourceInfo incomeSourceCard : incomeSourceInfoList) {
//            double sourceAmount = incomeSourceCard.getAmount();
//            int percentage = (int) (sourceAmount * 100 / shownIncome);
//            incomeSourceCard.setPercentage(percentage);
//        }
//
//        //将收支卡片按照占比排序（降序）
//        expenseSourceInfoList.sort(Comparator.comparing(AccountSourceInfo::getAmount).reversed());
//        incomeSourceInfoList.sort(Comparator.comparing(AccountSourceInfo::getAmount).reversed());
//
//        //补偿浮点数运算导致的占比精度
//        compensatePrecision(expenseSourceInfoList);
//        compensatePrecision(incomeSourceInfoList);
//
//        //设置收支来源卡片容器可见性
//        boolean isNoExpense, isNoIncome;
//        if (expenseSourceInfoList.isEmpty()) {
//            isNoExpense = true;
//        } else {
//            isNoExpense = false;
//            ScaleAnimator.show(binding.expenseSourceCard);
//        }
//        if (incomeSourceInfoList.isEmpty()) {
//            isNoIncome = true;
//        } else {
//            isNoIncome = false;
//            ScaleAnimator.show(binding.incomeSourceCard);
//        }
//        if (isNoIncome && isNoExpense) {
//            String tipStr = "该时间段没有流水记录";
//            switch (dateRangeType) {
//                case TODAY:
//                    tipStr = "这一天没有流水记录";
//                    break;
//                case THIS_MONTH:
//                    tipStr = "这个月没有流水记录";
//                    break;
//                case RECENT_3_MONTH:
//                    tipStr = "最近三个月没有流水记录";
//                    break;
//                case THIS_YEAR:
//                    tipStr = "这一年没有流水记录";
//                    break;
//            }
//            Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
//        }
//
//        //更新收支来源视图
//        expenseAdapter.refreshSource(expenseSourceInfoList);
//        incomeAdapter.refreshSource(incomeSourceInfoList);
//    }
//
//    /**
//     * 补偿计算金额百分比时的精度问题，使百分比之和恰好为100
//     *
//     * @param sourceInfoList 需要补偿精度的金额来源列表
//     */
//    private void compensatePrecision(@NonNull List<AccountSourceInfo> sourceInfoList) {
//        if (sourceInfoList.isEmpty()) return;
//
//        int percentageLeft = 100;               //剩余的百分比
//        int firstZeroIndex = -1, index = 0;   //首个百分比为0的元素的下标
//        boolean isEndsWithZero = false;         //是否以百分比为0的元素结尾
//        for (AccountSourceInfo sourceInfo : sourceInfoList) {
//            int currentPercentage = sourceInfo.getPercentage();
//            percentageLeft -= currentPercentage;
//
//            //如果未找到百分比为0的元素且当前百分比为0，则记录该元素的下标
//            if (!isEndsWithZero && currentPercentage == 0) {
//                isEndsWithZero = true;
//                firstZeroIndex = index;
//            }
//            index++;
//        }
//        if (percentageLeft == 0) return;    //如果百分比之和恰好为100则直接结束该方法
//
//        //循环为每个元素百分比+1，直到剩余百分比为0
//        int cycleIndex;
//        if (isEndsWithZero) {
//            cycleIndex = firstZeroIndex;
//        } else {
//            cycleIndex = 0;
//        }
//        while (percentageLeft > 0) {
//            percentageLeft--;
//            AccountSourceInfo currentSource = sourceInfoList.get(cycleIndex);
//            currentSource.setPercentage(currentSource.getPercentage() + 1);
//            cycleIndex = (cycleIndex + 1) % sourceInfoList.size();
//        }
//    }
//
//    /**
//     * 更新每月流水数据
//     *
//     * @param dataList 新数据列表
//     */
//    private void updateMonthAccountData(@NonNull List<ReportRunningAccountData> dataList) {
//        double[] monthExpense = new double[12];    //月支出
//        double[] monthIncome = new double[12];     //月收入
//        yearExpense = 0;
//        yearIncome = 0;
//        monthAccountInfoList.clear();               //清空每月流水数据
//
//        //读取数据并计算每月收支金额以及年度收支金额
//        for (ReportRunningAccountData data : dataList) {
//            AccountType type = data.getType();
//            double amount = data.getAmount();
//            int month = data.getMonth();
//
//            if (type.isExpenseType()) {
//                monthExpense[month - 1] += amount;
//                yearExpense += amount;
//            } else if (type.isIncomeType()) {
//                monthIncome[month - 1] += amount;
//                yearIncome += amount;
//            }
//        }
//
//        for (int index = 0; index < 12; index++) {
//            MonthAccountInfo monthAccountInfo = new MonthAccountInfo(monthExpense[index], monthIncome[index]);
//            monthAccountInfoList.add(monthAccountInfo);
//        }
//        monthAccountAdapter.refreshMonthAccountInfo(monthAccountInfoList, monthAccountInfoType);
//    }
//
//    //刷新每月收支数据视图
//    private void refreshMonthAccountInfoViews() {
//        switch (monthAccountInfoType) {
//            case BALANCE:
//                double absTotalBalance = 0;   //各月份结余绝对值总和
//                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
//                    double monthExpense = monthAccountInfo.getExpense();
//                    double monthIncome = monthAccountInfo.getIncome();
//                    double monthBalance = monthIncome - monthExpense;
//
//                    absTotalBalance += (monthBalance < 0) ? -monthBalance : monthBalance;
//                }
//
//                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
//                    double monthExpense = monthAccountInfo.getExpense();
//                    double monthIncome = monthAccountInfo.getIncome();
//                    double monthBalance = monthIncome - monthExpense;
//
//                    int percentage = (int) (monthBalance * 100 / absTotalBalance);
//                    if (percentage < 0) percentage = -percentage;
//                    monthAccountInfo.setPercentage(percentage);
//                }
//                break;
//            case INCOME:
//                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
//                    double infoIncome = monthAccountInfo.getIncome();
//
//                    int percentage = (int) (infoIncome * 100 / yearIncome);
//                    monthAccountInfo.setPercentage(percentage);
//                }
//                break;
//            case EXPENSE:
//                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
//                    double monthExpense = monthAccountInfo.getExpense();
//
//                    int percentage = (int) (monthExpense * 100 / yearExpense);
//                    monthAccountInfo.setPercentage(percentage);
//                }
//                break;
//        }
//
//        //补偿浮点数精度导致的百分比总和不为100
//        int index = 0;
//        boolean isNonZeroFound = false;
//        int minPercentageIndex = 0;
//        int minPercentage = 0;
//        int totalPercentage = 0;
//        for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
//            int currentPercentage = monthAccountInfo.getPercentage();
//
//            //寻找非零最小百分比
//            if (currentPercentage != 0) {
//                if (!isNonZeroFound) {  //只执行一次，功能：找到第一个非零元素
//                    isNonZeroFound = true;
//                    minPercentage = currentPercentage;
//                    minPercentageIndex = index;
//                }
//
//                if (currentPercentage < minPercentage) {
//                    minPercentageIndex = index;
//                    minPercentage = currentPercentage;
//                }
//            }
//
//            totalPercentage += currentPercentage;
//            index++;
//        }
//        if (totalPercentage != 0 && totalPercentage < 100) {
//            monthAccountInfoList.get(minPercentageIndex).setPercentage(minPercentage + 100 - totalPercentage);
//        }
//    }
//
//    /**
//     * 判断某来源是否在来源列表中
//     *
//     * @param accountSourceInfoList 来源列表
//     * @param source_no             该来源的编号
//     * @return 该来源对应在列表中的下标（未找到为-1）
//     */
//    private int isContainedInArray(@NonNull List<AccountSourceInfo> accountSourceInfoList, long source_no) {
//        int index = 0;
//        for (AccountSourceInfo oneCard : accountSourceInfoList) {
//            if (oneCard.getSourceNo() == source_no) {
//                return index;
//            }
//            index++;
//        }
//
//        return -1;
//    }

//    /**
//     * 显示选择月流水数据类型的下拉框
//     *
//     * @param view 绑定到的视图
//     */
//    private void showMonthAccountInfoTypePopupMenu(View view) {
//        PopupMenu monthAccountInfoTypeMenu = new PopupMenu(this, view);
//        monthAccountInfoTypeMenu.getMenuInflater().inflate(R.menu.popup_menu_month_account_type_select, monthAccountInfoTypeMenu.getMenu());
//
//        monthAccountInfoTypeMenu.setOnMenuItemClickListener(item -> {
//            boolean itemClicked = false;    //是否点击了选项
//            MonthAccountInfoType oldType = monthAccountInfoType;    //用于比较两次选择是否相同
//            if (item.getItemId() == R.id.action_balance) {
//                monthAccountInfoType = MonthAccountInfoType.BALANCE;
//                binding.monthAccountTypeLeadingBtn.setText(R.string.balance);
//                itemClicked = true;
//            } else if (item.getItemId() == R.id.action_expense) {
//                monthAccountInfoType = MonthAccountInfoType.EXPENSE;
//                binding.monthAccountTypeLeadingBtn.setText(R.string.expense);
//                itemClicked = true;
//            } else if (item.getItemId() == R.id.action_income) {
//                monthAccountInfoType = MonthAccountInfoType.INCOME;
//                binding.monthAccountTypeLeadingBtn.setText(R.string.income);
//                itemClicked = true;
//            }
//
//            if (itemClicked && oldType != monthAccountInfoType) {
//                List<ReportRunningAccountData> dataList = loadReportData(DateRangeType.THIS_YEAR);
//                updateMonthAccountData(dataList);
//                refreshMonthAccountInfoViews();
//            }
//
//            return itemClicked;
//        });
//
//        monthAccountInfoTypeMenu.setOnDismissListener(menu -> binding.monthAccountTypeSelectBtn.setChecked(false));
//        monthAccountInfoTypeMenu.show();
//    }
}