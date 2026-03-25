package com.manager.assistant.ui.pages.home.report;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.util.Pair;

import com.manager.assistant.R;
import com.manager.assistant.data.classes.AccountSourceInfo;
import com.manager.assistant.data.classes.MonthAccountInfo;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.databinding.ActivityReportBinding;
import com.manager.assistant.helpers.DateTimePickerHelper;
import com.manager.assistant.ui.others.animators.ScaleAnimator;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private final List<AccountSourceInfo> expenseSourceInfoList = new ArrayList<>();        //支出来源列表
    private final List<AccountSourceInfo> incomeSourceInfoList = new ArrayList<>();         //收入来源列表
    private final List<MonthAccountInfo> monthAccountInfoList = new ArrayList<>();          //月流水信息列表
    private DateRangeType dateRangeType = DateRangeType.THIS_MONTH;                         //日期范围种类
    private AccountSourceAdapter expenseAdapter, incomeAdapter;                             //收支来源布局适配器
    private MonthAccountInfoType monthAccountInfoType = MonthAccountInfoType.BALANCE;       //月流水信息种类
    private MonthAccountAdapter monthAccountAdapter;                                        //月流水信息适配器
    private double yearExpense = 0, yearIncome = 0;                                         //年支出和年收入（不是显示的数据）
    private double shownIncome = 0, shownExpense = 0;                                       //显示出来的收入和支出
    private LocalDate selectedDate;                                                         //不是自定义范围时，通过日期对话框选择的日期
    private LocalDate start, end;                                                           //查询流水记录时的日期范围
    private ActivityReportBinding binding;                                                  //XML界面绑定引用

    //月流水信息种类
    public enum MonthAccountInfoType {
        EXPENSE, INCOME, BALANCE
    }

    //日期范围
    enum DateRangeType {
        TODAY, THIS_MONTH, RECENT_3_MONTH, THIS_YEAR, CUSTOM
    }

    static class ReportRunningAccountData {
        private final RunningAccountType type;  //流水种类
        private final double amount;            //金额
        private final long tagNo;               //标签编号
        private final int month;                //月份

        public ReportRunningAccountData(RunningAccountType type, double amount, long tagNo, int month) {
            this.type = type;
            this.amount = amount;
            this.tagNo = tagNo;
            this.month = month;
        }

        public RunningAccountType getType() {
            return type;
        }

        public double getAmount() {
            return amount;
        }

        public long getTagNo() {
            return tagNo;
        }

        public int getMonth() {
            return month;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        start = end = selectedDate = LocalDate.now();   //将各个日期对象初始化为今天
        initViews();

        List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);    //加载报表数据
        updateAccountSource(dataList);                                              //更新收支来源视图

        //读取本年的流水数据并生成每月流水总结
        dataList = loadReportData(DateRangeType.THIS_YEAR);
        updateMonthAccountData(dataList);                                           //初始化每月流水数据
        refreshMonthAccountInfoViews();                                             //刷新每月流水数据视图
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //初始化当前日期
        String dateStr = DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(selectedDate);

        //设置点击监听器
        binding.dateSelectBtn.setText(dateStr);
        binding.dateSelectBtn.setOnClickListener(v -> showDatePickerDialog());
        binding.dateRangeSelectBtn.addOnCheckedChangeListener((materialButton, b) -> {
            if (b) {
                showDateRangeSelectPopupMenu(materialButton);
            }
        });
        binding.monthAccountTypeSelectBtn.addOnCheckedChangeListener(((materialButton, b) -> {
            if (b) {
                showMonthAccountInfoTypePopupMenu(materialButton);
            }
        }));

        //获取 RecyclerView 并设置适配器
        expenseAdapter = new AccountSourceAdapter(
                expenseSourceInfoList,
                (sourceInfo, isExcepted) -> {
                    //计算新的收支结果
                    double amount = sourceInfo.getAmount();
                    if (isExcepted) {
                        shownExpense -= amount;
                    } else {
                        shownExpense += amount;
                    }
                    double balance = shownIncome - shownExpense;

                    //显示到界面中
                    binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", balance));
                    binding.expenseIncomeText.setText(String.format(
                            Locale.getDefault(),
                            "支出:%.2f | 收入:%.2f",
                            shownExpense, shownIncome
                    ));
                }
        );
        binding.expenseSourceRecycler.setAdapter(expenseAdapter);
        incomeAdapter = new AccountSourceAdapter(
                incomeSourceInfoList,
                (sourceInfo, isExcepted) -> {
                    //计算新的收支结果
                    double amount = sourceInfo.getAmount();
                    if (isExcepted) {
                        shownIncome -= amount;
                    } else {
                        shownIncome += amount;
                    }
                    double balance = shownIncome - shownExpense;

                    //显示到界面中
                    binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", balance));
                    binding.expenseIncomeText.setText(String.format(
                            Locale.getDefault(),
                            "支出:%.2f | 收入:%.2f",
                            shownExpense, shownIncome
                    ));
                }
        );
        binding.incomeSourceRecycler.setAdapter(incomeAdapter);
        monthAccountAdapter = new MonthAccountAdapter(monthAccountInfoType);
        binding.monthAccountRecycler.setAdapter(monthAccountAdapter);
    }

    /**
     * 加载流水信息并生成报表数据
     */
    @NonNull
    private List<ReportRunningAccountData> loadReportData(@NonNull DateRangeType dateRangeType) throws SQLiteException {
        List<ReportRunningAccountData> dataList = new ArrayList<>();
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(this);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = new String[]{
                Columns.AMOUNT.toString(),
                Columns.TYPE.toString(),
                Columns.TAG_NO.toString(),
                Columns.DATETIME.toString()
        };
        String selection = Columns.DATETIME + ">=? AND " + Columns.DATETIME + "<?";

        //根据日期范围设置selection语句的参数
        LocalDate start, end;
        start = end = selectedDate;
        switch (dateRangeType) {
            case TODAY:
                end = selectedDate.plusDays(1);
                break;
            case THIS_MONTH:
                start = selectedDate.withDayOfMonth(1);
                end = start.plusMonths(1);
                break;
            case RECENT_3_MONTH:
                end = selectedDate.plusMonths(1).withDayOfMonth(1);
                start = end.plusMonths(-3);
                break;
            case THIS_YEAR:
                start = selectedDate.withMonth(1).withDayOfMonth(1);
                end = start.plusYears(1);
                break;
            case CUSTOM:
                start = this.start;
                end = this.end;
                break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String[] selectionArgs = {
                start.format(formatter),
                end.format(formatter)
        };

        Cursor basicCursor = db.query(
                Tables.BASIC.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (basicCursor.moveToNext()) {
            RunningAccountType type = RunningAccountType.valueOf(basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
            double amount = basicCursor.getDouble(basicCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
            long tagNo = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            String datetime = basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));

            //获取月份
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dateTime = LocalDateTime.parse(datetime, timeFormatter);
            int month = dateTime.getMonthValue();

            ReportRunningAccountData oneRecordedData = new ReportRunningAccountData(type, amount, tagNo, month);
            dataList.add(oneRecordedData);
        }

        basicCursor.close();
        db.close();
        return dataList;
    }

    /**
     * 更新收支来源视图
     *
     * @param dataList 更新视图所需的数据
     */
    private void updateAccountSource(@NonNull List<ReportRunningAccountData> dataList) {
        //清空旧数据
        shownExpense = 0;
        shownIncome = 0;
        double balance = 0; //结余
        incomeSourceInfoList.clear();
        expenseSourceInfoList.clear();

        //将收支卡片都暂时缩小至消失
        ScaleAnimator.hide(binding.expenseSourceCard);
        ScaleAnimator.hide(binding.incomeSourceCard);

        //解析新数据
        for (ReportRunningAccountData data : dataList) {
            RunningAccountType type = data.getType();
            double amount = data.getAmount();
            long tagNo = data.getTagNo();

            //获取收入或者支出列表的引用以便操作其中的元素
            List<AccountSourceInfo> expenseOrIncome;
            if (type.isIncomeType()) {
                shownIncome += amount;
                balance += amount;
                expenseOrIncome = incomeSourceInfoList;
            } else if (type.isExpenseType()) {
                shownExpense += amount;
                balance -= amount;
                expenseOrIncome = expenseSourceInfoList;
            } else {
                continue;   //既不是收入也不是支出则直接跳过
            }

            //判断目标列表是否为空
            int index = isContainedInArray(expenseOrIncome, tagNo);
            if (index != -1) {      //判断是否查询到对应的来源卡片
                expenseOrIncome.get(index).amountAdd(amount);
            } else {
                if (tagNo != 0) {  //判断该流水记录是否有标签
                    String tagName = TagDataController.tagNoTransToName(tagNo, this);
                    AccountSourceInfo newSource = new AccountSourceInfo(amount, tagName, tagNo);
                    expenseOrIncome.add(newSource);
                } else {
                    AccountSourceInfo otherSource = new AccountSourceInfo(amount, "其他", tagNo);
                    expenseOrIncome.add(otherSource);
                }
            }
        }

        //更新文本视图
        binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", balance));
        binding.expenseIncomeText.setText(String.format(
                Locale.getDefault(),
                "支出:%.2f | 收入:%.2f",
                shownExpense, shownIncome
        ));

        //计算各来源的收支占比
        for (AccountSourceInfo expenseSourceCard : expenseSourceInfoList) {
            double sourceAmount = expenseSourceCard.getAmount();
            int percentage = (int) (sourceAmount * 100 / shownExpense);
            expenseSourceCard.setPercentage(percentage);
        }
        for (AccountSourceInfo incomeSourceCard : incomeSourceInfoList) {
            double sourceAmount = incomeSourceCard.getAmount();
            int percentage = (int) (sourceAmount * 100 / shownIncome);
            incomeSourceCard.setPercentage(percentage);
        }

        //将收支卡片按照占比排序（降序）
        expenseSourceInfoList.sort(Comparator.comparing(AccountSourceInfo::getAmount).reversed());
        incomeSourceInfoList.sort(Comparator.comparing(AccountSourceInfo::getAmount).reversed());

        //补偿浮点数运算导致的占比精度
        compensatePrecision(expenseSourceInfoList);
        compensatePrecision(incomeSourceInfoList);

        //设置收支来源卡片容器可见性
        boolean isNoExpense, isNoIncome;
        if (expenseSourceInfoList.isEmpty()) {
            isNoExpense = true;
        } else {
            isNoExpense = false;
            ScaleAnimator.show(binding.expenseSourceCard);
        }
        if (incomeSourceInfoList.isEmpty()) {
            isNoIncome = true;
        } else {
            isNoIncome = false;
            ScaleAnimator.show(binding.incomeSourceCard);
        }
        if (isNoIncome && isNoExpense) {
            String tipStr = "该时间段没有流水记录";
            switch (dateRangeType) {
                case TODAY:
                    tipStr = "这一天没有流水记录";
                    break;
                case THIS_MONTH:
                    tipStr = "这个月没有流水记录";
                    break;
                case RECENT_3_MONTH:
                    tipStr = "最近三个月没有流水记录";
                    break;
                case THIS_YEAR:
                    tipStr = "这一年没有流水记录";
                    break;
            }
            Toast.makeText(this, tipStr, Toast.LENGTH_SHORT).show();
        }

        //更新收支来源视图
        expenseAdapter.refreshSource(expenseSourceInfoList);
        incomeAdapter.refreshSource(incomeSourceInfoList);
    }

    /**
     * 补偿计算金额百分比时的精度问题，使百分比之和恰好为100
     *
     * @param sourceInfoList 需要补偿精度的金额来源列表
     */
    private void compensatePrecision(@NonNull List<AccountSourceInfo> sourceInfoList) {
        if (sourceInfoList.isEmpty()) return;

        int percentageLeft = 100;               //剩余的百分比
        int firstZeroIndex = -1, index = 0;   //首个百分比为0的元素的下标
        boolean isEndsWithZero = false;         //是否以百分比为0的元素结尾
        for (AccountSourceInfo sourceInfo : sourceInfoList) {
            int currentPercentage = sourceInfo.getPercentage();
            percentageLeft -= currentPercentage;

            //如果未找到百分比为0的元素且当前百分比为0，则记录该元素的下标
            if (!isEndsWithZero && currentPercentage == 0) {
                isEndsWithZero = true;
                firstZeroIndex = index;
            }
            index++;
        }
        if (percentageLeft == 0) return;    //如果百分比之和恰好为100则直接结束该方法

        //循环为每个元素百分比+1，直到剩余百分比为0
        int cycleIndex;
        if (isEndsWithZero) {
            cycleIndex = firstZeroIndex;
        } else {
            cycleIndex = 0;
        }
        while (percentageLeft > 0) {
            percentageLeft--;
            AccountSourceInfo currentSource = sourceInfoList.get(cycleIndex);
            currentSource.setPercentage(currentSource.getPercentage() + 1);
            cycleIndex = (cycleIndex + 1) % sourceInfoList.size();
        }
    }

    /**
     * 更新每月流水数据
     *
     * @param dataList 新数据列表
     */
    private void updateMonthAccountData(@NonNull List<ReportRunningAccountData> dataList) {
        double[] monthExpense = new double[12];    //月支出
        double[] monthIncome = new double[12];     //月收入
        yearExpense = 0;
        yearIncome = 0;
        monthAccountInfoList.clear();               //清空每月流水数据

        //读取数据并计算每月收支金额以及年度收支金额
        for (ReportRunningAccountData data : dataList) {
            RunningAccountType type = data.getType();
            double amount = data.getAmount();
            int month = data.getMonth();

            if (type.isExpenseType()) {
                monthExpense[month - 1] += amount;
                yearExpense += amount;
            } else {
                monthIncome[month - 1] += amount;
                yearIncome += amount;
            }
        }

        for (int index = 0; index < 12; index++) {
            MonthAccountInfo monthAccountInfo = new MonthAccountInfo(monthExpense[index], monthIncome[index]);
            monthAccountInfoList.add(monthAccountInfo);
        }
        monthAccountAdapter.refreshMonthAccountInfo(monthAccountInfoList, monthAccountInfoType);
    }

    //刷新每月收支数据视图
    private void refreshMonthAccountInfoViews() {
        switch (monthAccountInfoType) {
            case BALANCE:
                double absTotalBalance = 0;   //各月份结余绝对值总和
                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double monthExpense = monthAccountInfo.getExpense();
                    double monthIncome = monthAccountInfo.getIncome();
                    double monthBalance = monthIncome - monthExpense;

                    absTotalBalance += (monthBalance < 0) ? -monthBalance : monthBalance;
                }

                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double monthExpense = monthAccountInfo.getExpense();
                    double monthIncome = monthAccountInfo.getIncome();
                    double monthBalance = monthIncome - monthExpense;

                    int percentage = (int) (monthBalance * 100 / absTotalBalance);
                    if (percentage < 0) percentage = -percentage;
                    monthAccountInfo.setPercentage(percentage);
                }
                break;
            case INCOME:
                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double month_income = monthAccountInfo.getIncome();

                    int percentage = (int) (month_income * 100 / yearIncome);
                    monthAccountInfo.setPercentage(percentage);
                }
                break;
            case EXPENSE:
                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double month_expense = monthAccountInfo.getExpense();

                    int percentage = (int) (month_expense * 100 / yearExpense);
                    monthAccountInfo.setPercentage(percentage);
                }
                break;
        }

        //补偿浮点数精度导致的百分比总和不为100
        int index = 0;
        boolean isNonZeroFound = false;
        int minPercentageIndex = 0;
        int minPercentage = 0;
        int totalPercentage = 0;
        for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
            int currentPercentage = monthAccountInfo.getPercentage();

            //寻找非零最小百分比
            if (currentPercentage != 0) {
                if (!isNonZeroFound) {  //只执行一次，功能：找到第一个非零元素
                    isNonZeroFound = true;
                    minPercentage = currentPercentage;
                    minPercentageIndex = index;
                }

                if (currentPercentage < minPercentage) {
                    minPercentageIndex = index;
                    minPercentage = currentPercentage;
                }
            }

            totalPercentage += currentPercentage;
            index++;
        }
        if (totalPercentage != 0 && totalPercentage < 100) {
            monthAccountInfoList.get(minPercentageIndex).setPercentage(minPercentage + 100 - totalPercentage);
        }
    }

    /**
     * 判断某来源是否在来源列表中
     *
     * @param accountSourceInfoList 来源列表
     * @param source_no             该来源的编号
     * @return 该来源对应在列表中的下标（未找到为-1）
     */
    private int isContainedInArray(@NonNull List<AccountSourceInfo> accountSourceInfoList, long source_no) {
        int index = 0;
        for (AccountSourceInfo oneCard : accountSourceInfoList) {
            if (oneCard.getSourceNo() == source_no) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * 弹出日期选择页
     */
    private void showDatePickerDialog() {
        if (dateRangeType != DateRangeType.CUSTOM) {
            DateTimePickerHelper.selectDate(
                    selectedDate,
                    getSupportFragmentManager(),
                    selection -> {
                        LocalDate selected = Instant.ofEpochMilli(selection)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate();

                        //更新日期文本视图
                        int oldYear = start.getYear();
                        selectedDate = selected;
                        DateTimeFormatter chineseFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
                        binding.dateSelectBtn.setText(selected.format(chineseFormatter));

                        //重新加载报表信息
                        List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
                        updateAccountSource(dataList);
                        if (oldYear != selectedDate.getYear()) {
                            dataList = loadReportData(DateRangeType.THIS_YEAR);
                            updateMonthAccountData(dataList);
                            refreshMonthAccountInfoViews();
                        }
                    }
            );
        } else {
            DateTimePickerHelper.selectDateRange(
                    start,
                    end,
                    getSupportFragmentManager(),
                    this,
                    this::onDateRangeDialogPositiveBtnClicked
            );
        }
    }

    /**
     * 显示日期范围选择的PopupMenu
     *
     * @param view 需要绑定PopupMenu的视图
     */
    private void showDateRangeSelectPopupMenu(View view) {
        PopupMenu dateRangeSelectMenu = new PopupMenu(this, view);
        dateRangeSelectMenu.getMenuInflater().inflate(R.menu.popup_menu_date_range_select, dateRangeSelectMenu.getMenu());

        dateRangeSelectMenu.setOnMenuItemClickListener(item -> {
            boolean itemClicked = false;        //是否点击某个选项
            boolean isCustomRange = false;      //是否为自定义日期范围
            if (item.getItemId() == R.id.action_today) {
                ScaleAnimator.show(binding.monthAccountCard);
                dateRangeType = DateRangeType.TODAY;
                binding.dateRangeText.setText(R.string.today);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_month) {
                ScaleAnimator.show(binding.monthAccountCard);
                dateRangeType = DateRangeType.THIS_MONTH;
                binding.dateRangeText.setText(R.string.this_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_recent_3_month) {
                ScaleAnimator.show(binding.monthAccountCard);
                dateRangeType = DateRangeType.RECENT_3_MONTH;
                binding.dateRangeText.setText(R.string.recent_3_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_year) {
                ScaleAnimator.show(binding.monthAccountCard);
                dateRangeType = DateRangeType.THIS_YEAR;
                binding.dateRangeText.setText(R.string.this_year);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_custom) {
                dateRangeType = DateRangeType.CUSTOM;
                itemClicked = true;
                isCustomRange = true;

                //显示日期范围选择对话框
                DateTimePickerHelper.selectDateRange(
                        start,
                        end,
                        getSupportFragmentManager(),
                        ReportActivity.this,
                        this::onDateRangeDialogPositiveBtnClicked
                );
            }

            //不是自定义日期范围时刷新收支来源卡片
            if (!isCustomRange) {
                List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
                updateAccountSource(dataList);

                //更新日期选择按钮的文本
                DateTimeFormatter chineseFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
                String selectedDateStr = selectedDate.format(chineseFormatter);
                binding.dateSelectBtn.setText(selectedDateStr);
            }

            return itemClicked;
        });

        //设置菜单消失监听并显示菜单
        dateRangeSelectMenu.setOnDismissListener(menu -> binding.dateRangeSelectBtn.setChecked(false));
        dateRangeSelectMenu.show();
    }

    /**
     * 显示选择月流水数据类型的下拉框
     *
     * @param view 绑定到的视图
     */
    private void showMonthAccountInfoTypePopupMenu(View view) {
        PopupMenu monthAccountInfoTypeMenu = new PopupMenu(this, view);
        monthAccountInfoTypeMenu.getMenuInflater().inflate(R.menu.popup_menu_month_account_type_select, monthAccountInfoTypeMenu.getMenu());

        monthAccountInfoTypeMenu.setOnMenuItemClickListener(item -> {
            boolean itemClicked = false;    //是否点击了选项
            MonthAccountInfoType oldType = monthAccountInfoType;    //用于比较两次选择是否相同
            if (item.getItemId() == R.id.action_balance) {
                monthAccountInfoType = MonthAccountInfoType.BALANCE;
                binding.monthAccountTypeLeadingBtn.setText(R.string.balance);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_expense) {
                monthAccountInfoType = MonthAccountInfoType.EXPENSE;
                binding.monthAccountTypeLeadingBtn.setText(R.string.expense);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_income) {
                monthAccountInfoType = MonthAccountInfoType.INCOME;
                binding.monthAccountTypeLeadingBtn.setText(R.string.income);
                itemClicked = true;

                //弹出关于日期范围边界的提示
                Toast.makeText(this, "提示：统计结果包含起止日期", Toast.LENGTH_SHORT).show();
            }

            if (itemClicked && oldType != monthAccountInfoType) {
                List<ReportRunningAccountData> dataList = loadReportData(DateRangeType.THIS_YEAR);
                updateMonthAccountData(dataList);
                refreshMonthAccountInfoViews();
            }

            return itemClicked;
        });

        monthAccountInfoTypeMenu.setOnDismissListener(menu -> binding.monthAccountTypeSelectBtn.setChecked(false));
        monthAccountInfoTypeMenu.show();
    }

    /**
     * 日期范围选择对话框确认回调
     *
     * @param selection 选择的日期范围对
     */
    private void onDateRangeDialogPositiveBtnClicked(@NonNull Pair<Long, Long> selection) {
        start = DateTimePickerHelper.getLocalDateFromTimeMilli(selection.first);
        end = DateTimePickerHelper.getLocalDateFromTimeMilli(selection.second).plusDays(1);   //因为数据库的WHERE子句为左闭右开，因此需要+1天

        //隐藏每月流水报表（因为日期范围可以跨年）
        ScaleAnimator.hide(binding.monthAccountCard);

        //更换日期选择按钮的文本
        binding.dateSelectBtn.setText(R.string.select_date);

        //显示选中的日期范围
        DateTimeFormatter chineseFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        String chineseDateRange = String.format(
                Locale.getDefault(),
                "%s ~ %s",
                start.format(chineseFormatter),
                end.format(chineseFormatter)
        );
        binding.dateRangeText.setText(chineseDateRange);

        //刷新收支来源卡片
        List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
        updateAccountSource(dataList);
    }
}