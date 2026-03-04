package com.manager.assistant.ui.pages.home.report;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.AccountSourceInfo;
import com.manager.assistant.data.data_class.MonthAccountInfo;
import com.manager.assistant.data.data_save.database.Columns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.Tables;
import com.manager.assistant.databinding.ActivityReportBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.data.data_class.Tag;

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
    private DateRangeType dateRangeType = DateRangeType.TODAY;                              //日期范围种类
    private AccountSourceAdapter expenseAdapter, incomeAdapter;                             //收支来源布局适配器
    private MonthAccountInfoType monthAccountInfoType = MonthAccountInfoType.BALANCE;       //月流水信息种类
    private MonthAccountAdapter monthAccountAdapter;                                        //月流水信息适配器
    private double year_expense = 0, year_income = 0;                                       //年支出和年收入
    private int year, month, day;                                                           //年月日
    private ActivityReportBinding binding;                                                  //XML界面绑定引用

    //月流水信息种类
    public enum MonthAccountInfoType {
        EXPENSE, INCOME, BALANCE
    }

    //日期范围
    enum DateRangeType {
        TODAY, THIS_MONTH, RECENT_3_MONTH, THIS_YEAR
    }

    static class ReportRunningAccountData {
        private final RunningAccountType type;  //流水种类
        private final double amount;            //金额
        private final long tag_no;              //标签编号
        private final int month;                //月份

        public ReportRunningAccountData(RunningAccountType type, double amount, long tag_no, int month) {
            this.type = type;
            this.amount = amount;
            this.tag_no = tag_no;
            this.month = month;
        }

        public RunningAccountType getType() {
            return type;
        }

        public double getAmount() {
            return amount;
        }

        public long getTag_no() {
            return tag_no;
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

        initViews();

        List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);    //加载报表数据
        updateAccountSource(dataList);                                                //更新收支来源视图

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
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        String dateStr = formatter.format(now);
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();

        //设置点击监听器
        binding.reportDateSelectBtn.setText(dateStr);
        binding.reportDateSelectBtn.setOnClickListener(v -> showDatePickerDialog());
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

        //获取RecyclerView并设置适配器
        expenseAdapter = new AccountSourceAdapter(expenseSourceInfoList);
        binding.expenseSourceRecycler.setAdapter(expenseAdapter);
        incomeAdapter = new AccountSourceAdapter(incomeSourceInfoList);
        binding.incomeSourceRecycler.setAdapter(incomeAdapter);
        monthAccountAdapter = new MonthAccountAdapter(monthAccountInfoType);
        binding.monthAccountRecycler.setAdapter(monthAccountAdapter);
    }

    /**
     * 加载流水信息并生成报表数据
     */
    private List<ReportRunningAccountData> loadReportData(@NonNull DateRangeType dateRangeType) throws SQLiteException {
        List<ReportRunningAccountData> dataList = new ArrayList<>();
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(this);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = new String[]{
                Columns.AMOUNT.toString(),
                Columns.TYPE.toString(),
                Columns.TAG_NO.toString(),
                Columns.DATETIME.toString()
        };
        String selection = Columns.DATETIME + ">=? AND " + Columns.DATETIME + "<?";

        //根据日期范围设置selection语句的参数
        String[] selectionArgs;
        switch (dateRangeType) {
            case TODAY:
                selectionArgs = new String[]{
                        String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day),
                        String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day + 1)
                };
                break;
            case THIS_MONTH:
                selectionArgs = new String[]{
                        String.format(Locale.getDefault(), "%04d-%02d-01", year, month),
                        String.format(Locale.getDefault(), "%04d-%02d-01", year, month + 1)
                };
                break;
            case RECENT_3_MONTH:
                int early_month = month - 2;
                int early_year;
                if (early_month <= 0) {
                    early_year = year - 1;
                    early_month += 12;
                } else {
                    early_year = year;
                }
                selectionArgs = new String[]{
                        String.format(Locale.getDefault(), "%04d-%02d-01", early_year, early_month),
                        String.format(Locale.getDefault(), "%04d-%02d-01", year, month + 1)
                };
                break;
            case THIS_YEAR:
                selectionArgs = new String[]{
                        String.format(Locale.getDefault(), "%04d-01-01", year),
                        String.format(Locale.getDefault(), "%04d-01-01", year + 1)
                };
                break;
            default:
                NullPointerException e = new NullPointerException("无法设置合法的日期范围");
                ExceptionHelper.showExceptionDialog(this, e);
                return dataList;
        }

        Cursor basicCursor = db.query(
                Tables.BASIC.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                Columns.DATETIME + " DESC"
        );

        while (basicCursor.moveToNext()) {
            RunningAccountType type = RunningAccountType.valueOf(basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
            double amount = basicCursor.getDouble(basicCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
            long tag_no = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            String datetime = basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));

            //获取月份
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime localDateTime = LocalDateTime.parse(datetime, formatter);
            int month = localDateTime.getMonthValue();

            ReportRunningAccountData oneRecordedData = new ReportRunningAccountData(type, amount, tag_no, month);
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
        double expense = 0, income = 0; //总支出和总收入
        double balance = 0;             //结余
        incomeSourceInfoList.clear();
        expenseSourceInfoList.clear();

        for (ReportRunningAccountData data : dataList) {
            RunningAccountType type = data.getType();
            double amount = data.getAmount();
            long tag_no = data.getTag_no();

            //获取收入或者支出列表的引用以便操作其中的元素
            List<AccountSourceInfo> expenseOrIncome;
            if (type.isIncomeType()) {
                income += amount;
                balance += amount;
                expenseOrIncome = incomeSourceInfoList;
            } else if (type.isExpenseType()) {
                expense += amount;
                balance -= amount;
                expenseOrIncome = expenseSourceInfoList;
            } else {
                continue;   //既不是收入也不是支出则直接跳过
            }

            //判断目标列表是否为空
            int index = isContainedInArray(expenseOrIncome, tag_no);
            if (index != -1) {      //判断是否查询到对应的来源卡片
                expenseOrIncome.get(index).amountAdd(amount);
            } else {
                if (tag_no != 0) {  //判断该流水记录是否有标签
                    String tag_name = Tag.tagNoTransToName(tag_no, this);
                    AccountSourceInfo newSource = new AccountSourceInfo(amount, tag_name, tag_no);
                    expenseOrIncome.add(newSource);
                } else {
                    AccountSourceInfo otherSource = new AccountSourceInfo(amount, "其他", tag_no);
                    expenseOrIncome.add(otherSource);
                }
            }
        }

        //更新文本视图
        binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", balance));
        binding.expenseIncomeText.setText(String.format(
                Locale.getDefault(),
                "支出:%.2f | 收入:%.2f",
                expense, income
        ));

        //计算各来源的收支占比
        for (AccountSourceInfo expenseSourceCard : expenseSourceInfoList) {
            double source_amount = expenseSourceCard.getAmount();
            int percentage = (int) (source_amount * 100 / expense);
            expenseSourceCard.setPercentage(percentage);
        }
        for (AccountSourceInfo incomeSourceCard : incomeSourceInfoList) {
            double source_amount = incomeSourceCard.getAmount();
            int percentage = (int) (source_amount * 100 / income);
            incomeSourceCard.setPercentage(percentage);
        }

        //将收支卡片按照占比排序（降序）
        expenseSourceInfoList.sort(Comparator.comparing(AccountSourceInfo::getAmount).reversed());
        incomeSourceInfoList.sort(Comparator.comparing(AccountSourceInfo::getAmount).reversed());

        //补偿浮点数运算导致的占比精度
        compensatePrecision(expenseSourceInfoList);
        compensatePrecision(incomeSourceInfoList);

        //设置收支来源卡片容器可见性
        boolean isNoExpense = false, isNoIncome = false;
        MaterialCardView expenseSourceCard = binding.expenseSourceCard;
        if (expenseSourceInfoList.isEmpty()) {
            expenseSourceCard.setVisibility(View.GONE);
            isNoExpense = true;
        } else {
            expenseSourceCard.setVisibility(View.VISIBLE);
        }
        MaterialCardView incomeSourceCard = binding.incomeSourceCard;
        if (incomeSourceInfoList.isEmpty()) {
            incomeSourceCard.setVisibility(View.GONE);
            isNoIncome = true;
        } else {
            incomeSourceCard.setVisibility(View.VISIBLE);
        }
        if (isNoIncome && isNoExpense) {
            String tip_str = "该时间段没有流水记录";
            switch (dateRangeType) {
                case TODAY:
                    tip_str = "这一天没有流水记录";
                    break;
                case THIS_MONTH:
                    tip_str = "这个月没有流水记录";
                    break;
                case RECENT_3_MONTH:
                    tip_str = "最近三个月没有流水记录";
                    break;
                case THIS_YEAR:
                    tip_str = "这一年没有流水记录";
                    break;
            }
            Toast.makeText(this, tip_str, Toast.LENGTH_SHORT).show();
            binding.expenseIncomeLayout.setVisibility(View.GONE);
            binding.monthAccountLayout.setVisibility(View.GONE);
        } else {
            binding.expenseIncomeLayout.setVisibility(View.VISIBLE);
            if (dateRangeType == DateRangeType.THIS_YEAR) {
                binding.monthAccountLayout.setVisibility(View.VISIBLE);
            }
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
        int first_zero_index = -1, index = 0;   //首个百分比为0的元素的下标
        boolean isEndsWithZero = false;         //是否以百分比为0的元素结尾
        for (AccountSourceInfo sourceInfo : sourceInfoList) {
            int currentPercentage = sourceInfo.getPercentage();
            percentageLeft -= currentPercentage;

            //如果未找到百分比为0的元素且当前百分比为0，则记录该元素的下标
            if (!isEndsWithZero && currentPercentage == 0) {
                isEndsWithZero = true;
                first_zero_index = index;
            }
            index++;
        }
        if (percentageLeft == 0) return;    //如果百分比之和恰好为100则直接结束该方法

        //循环为每个元素百分比+1，直到剩余百分比为0
        int cycle_index;
        if (isEndsWithZero) {
            cycle_index = first_zero_index;
        } else {
            cycle_index = 0;
        }
        while (percentageLeft > 0) {
            percentageLeft--;
            AccountSourceInfo currentSource = sourceInfoList.get(cycle_index);
            currentSource.setPercentage(currentSource.getPercentage() + 1);
            cycle_index = (cycle_index + 1) % sourceInfoList.size();
        }
    }

    /**
     * 更新每月流水数据
     *
     * @param dataList 新数据列表
     */
    private void updateMonthAccountData(@NonNull List<ReportRunningAccountData> dataList) {
        double[] month_expense = new double[12];    //月支出
        double[] month_income = new double[12];     //月收入
        year_expense = 0;
        year_income = 0;
        monthAccountInfoList.clear();               //清空每月流水数据

        //读取数据并计算每月收支金额以及年度收支金额
        for (ReportRunningAccountData data : dataList) {
            RunningAccountType type = data.getType();
            double amount = data.getAmount();
            int month = data.getMonth();

            if (type.isExpenseType()) {
                month_expense[month - 1] += amount;
                year_expense += amount;
            } else {
                month_income[month - 1] += amount;
                year_income += amount;
            }
        }

        for (int index = 0; index < 12; index++) {
            MonthAccountInfo monthAccountInfo = new MonthAccountInfo(month_expense[index], month_income[index]);
            monthAccountInfoList.add(monthAccountInfo);
        }
        monthAccountAdapter.refreshMonthAccountInfo(monthAccountInfoList, monthAccountInfoType);
    }

    //刷新每月收支数据视图
    private void refreshMonthAccountInfoViews() {
        switch (monthAccountInfoType) {
            case BALANCE:
                double abs_total_balance = 0;   //各月份结余绝对值总和
                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double month_expense = monthAccountInfo.getExpense();
                    double month_income = monthAccountInfo.getIncome();
                    double month_balance = month_income - month_expense;

                    abs_total_balance += (month_balance < 0) ? -month_balance : month_balance;
                }

                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double month_expense = monthAccountInfo.getExpense();
                    double month_income = monthAccountInfo.getIncome();
                    double month_balance = month_income - month_expense;

                    int percentage = (int) (month_balance * 100 / abs_total_balance);
                    if (percentage < 0) percentage = -percentage;
                    monthAccountInfo.setPercentage(percentage);
                }
                break;
            case INCOME:
                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double month_income = monthAccountInfo.getIncome();

                    int percentage = (int) (month_income * 100 / year_income);
                    monthAccountInfo.setPercentage(percentage);
                }
                break;
            case EXPENSE:
                for (MonthAccountInfo monthAccountInfo : monthAccountInfoList) {
                    double month_expense = monthAccountInfo.getExpense();

                    int percentage = (int) (month_expense * 100 / year_expense);
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
            if (oneCard.getSource_no() == source_no) {
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
        //实例化一个日期对象用于存放选中的日期
        LocalDate date = LocalDate.of(year, month, day);

        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");

        //显示日期选择器
        long dateSelection = date.atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
        MaterialDatePicker<Long> datePicker = dateBuilder
                .setSelection(dateSelection)
                .setCalendarConstraints(
                        new CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointBackward.now()) //限制为过去日期
                                .build()
                )
                .build();
        datePicker.show(getSupportFragmentManager(), TagString.DATE_PICKER.getValue());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            //更新日期文本视图
            int old_year = this.year;
            this.year = selectedDate.getYear();
            this.month = selectedDate.getMonthValue();
            this.day = selectedDate.getDayOfMonth();
            binding.reportDateSelectBtn.setText(String.format(Locale.getDefault(), "%04d年%02d月%02d日", year, month, day));

            //重新加载报表信息
            List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
            updateAccountSource(dataList);
            if (old_year != this.year) {
                dataList = loadReportData(DateRangeType.THIS_YEAR);
                updateMonthAccountData(dataList);
                refreshMonthAccountInfoViews();
            }
        });
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
            LinearLayout monthAccountLayout = binding.monthAccountLayout;
            boolean itemClicked = false;    //是否点击某个选项
            if (item.getItemId() == R.id.action_today) {
                monthAccountLayout.setVisibility(View.GONE);
                dateRangeType = DateRangeType.TODAY;
                binding.dateRangeText.setText(R.string.today);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_month) {
                monthAccountLayout.setVisibility(View.GONE);
                dateRangeType = DateRangeType.THIS_MONTH;
                binding.dateRangeText.setText(R.string.this_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_recent_3_month) {
                monthAccountLayout.setVisibility(View.GONE);
                dateRangeType = DateRangeType.RECENT_3_MONTH;
                binding.dateRangeText.setText(R.string.recent_3_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_year) {
                monthAccountLayout.setVisibility(View.VISIBLE);
                dateRangeType = DateRangeType.THIS_YEAR;
                binding.dateRangeText.setText(R.string.this_year);
                itemClicked = true;
            }

            if (itemClicked) {
                List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
                updateAccountSource(dataList);
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
            MonthAccountInfoType old_type = monthAccountInfoType;   //用于比较两次选择是否相同
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
            }

            if (itemClicked && old_type != monthAccountInfoType) {
                List<ReportRunningAccountData> dataList = loadReportData(DateRangeType.THIS_YEAR);
                updateMonthAccountData(dataList);
                refreshMonthAccountInfoViews();
            }

            return itemClicked;
        });

        monthAccountInfoTypeMenu.setOnDismissListener(menu -> binding.monthAccountTypeSelectBtn.setChecked(false));
        monthAccountInfoTypeMenu.show();
    }
}