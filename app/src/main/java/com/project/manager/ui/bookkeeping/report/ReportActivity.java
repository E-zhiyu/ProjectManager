package com.project.manager.ui.bookkeeping.report;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.data.data_class.AccountSourceInfo;
import com.project.manager.data.data_class.MonthAccountInfo;
import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDbHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;
import com.project.manager.databinding.ActivityReportBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.TagString;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.data.data_class.Tag;

import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {
    private final List<AccountSourceInfo> expenseSourceInfoList = new ArrayList<>();        //支出来源列表
    private final List<AccountSourceInfo> incomeSourceInfoList = new ArrayList<>();         //收入来源列表
    private int year, month, day;                                                           //年月日
    private DateRangeType dateRangeType = DateRangeType.TODAY;                              //日期范围种类
    private AccountSourceAdapter expense_adapter, income_adapter;                           //收支来源布局适配器
    private MonthAccountInfoType monthAccountInfoType = MonthAccountInfoType.BALANCE;       //月流水信息种类
    private final List<MonthAccountInfo> monthAccountInfoList = new ArrayList<>();          //月流水信息列表
    private MonthAccountAdapter month_account_adapter;                                      //月流水信息适配器
    private double year_expense = 0, year_income = 0;                                       //年支出和年收入
    private ActivityReportBinding binding;                                                  //XML界面绑定引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);    //加载报表数据
        updateSourceViews(dataList);                                                //更新收支来源视图

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

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.report_date_textview) {
            showDatePickerDialog();
        } else if (v.getId() == R.id.date_range_select_view) {
            showDateRangeSelectPopupMenu(v);
        } else if (v.getId() == R.id.month_account_type_select_view) {
            showMonthAccountInfoTypePopupMenu(v);
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH) + 1;
        day = now.get(Calendar.DAY_OF_MONTH);
        @SuppressLint("DefaultLocale") String date_str = String.format("%04d年%02d月%02d日", year, month, day);

        //设置点击监听器
        MaterialTextView date_textview = binding.reportDateTextview;
        date_textview.setText(date_str);
        date_textview.setOnClickListener(this);
        MaterialTextView date_range_select_view = binding.dateRangeSelectView;
        date_range_select_view.setOnClickListener(this);
        MaterialTextView month_account_info_type_select_view = binding.monthAccountTypeSelectView;
        month_account_info_type_select_view.setOnClickListener(this);

        //获取RecyclerView并设置适配器
        RecyclerView expense_source_recycler = binding.expenseSourceRecycler;
        expense_adapter = new AccountSourceAdapter(expenseSourceInfoList);
        expense_source_recycler.setAdapter(expense_adapter);
        RecyclerView income_source_recycler = binding.incomeSourceRecycler;
        income_adapter = new AccountSourceAdapter(incomeSourceInfoList);
        income_source_recycler.setAdapter(income_adapter);
        RecyclerView month_account_recycler = binding.monthAccountRecycler;
        month_account_adapter = new MonthAccountAdapter(monthAccountInfoList, monthAccountInfoType, this);
        month_account_recycler.setAdapter(month_account_adapter);
    }

    /**
     * 加载流水信息并生成报表数据
     */
    private List<ReportRunningAccountData> loadReportData(@NonNull DateRangeType dateRangeType) throws SQLiteException {
        List<ReportRunningAccountData> dataList = new ArrayList<>();
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(this);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = new String[]{
                BookKeepingColumns.AMOUNT.toString(),
                BookKeepingColumns.TYPE.toString(),
                BookKeepingColumns.TAG_NO.toString(),
                BookKeepingColumns.DATETIME.toString()
        };
        String selection = BookKeepingColumns.DATETIME + ">=? AND " + BookKeepingColumns.DATETIME + "<?";

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

        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                BookKeepingColumns.DATETIME + " DESC"
        );

        while (basic_cursor.moveToNext()) {
            RunningAccountType type = RunningAccountType.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString())));
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.AMOUNT.toString()));
            long tag_no = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            String date_time = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.DATETIME.toString()));

            //获取月份
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime localDateTime = LocalDateTime.parse(date_time, formatter);
            int month = localDateTime.getMonthValue();

            ReportRunningAccountData oneRecordedData = new ReportRunningAccountData(type, amount, tag_no, month);
            dataList.add(oneRecordedData);
        }

        basic_cursor.close();
        db.close();
        return dataList;
    }

    /**
     * 更新收支来源视图
     *
     * @param dataList 更新视图所需的数据
     */
    @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
    private void updateSourceViews(@NonNull List<ReportRunningAccountData> dataList) {
        double expense = 0, income = 0; //总支出和总收入
        double balance = 0;             //结余
        incomeSourceInfoList.clear();
        expenseSourceInfoList.clear();
        for (ReportRunningAccountData oneRecordedData : dataList) {
            RunningAccountType type = oneRecordedData.getType();
            double amount = oneRecordedData.getAmount();
            long tag_no = oneRecordedData.getTag_no();

            //处理金额数据
            List<AccountSourceInfo> targetList; //待操作的来源列表
            if (!type.isExpenseType()) {
                income += amount;
                balance += amount;
                targetList = incomeSourceInfoList;
            } else {
                expense += amount;
                balance -= amount;
                targetList = expenseSourceInfoList;
            }

            //判断目标列表是否为空
            int index = isContainedInArray(targetList, tag_no);
            if (index != -1) {      //判断是否查询到对应的来源卡片
                targetList.get(index).amountAdd(amount);
            } else {
                if (tag_no != 0) {  //判断该流水记录是否有标签
                    String tag_name = Tag.tagNoTransToName(tag_no, this);
                    AccountSourceInfo newSource = new AccountSourceInfo(amount, tag_name, tag_no);
                    targetList.add(newSource);
                } else {
                    AccountSourceInfo otherSource = new AccountSourceInfo(amount, "其他", tag_no);
                    targetList.add(otherSource);
                }
            }
        }

        //更新文本视图
        MaterialTextView balance_textview = binding.balanceText;
        balance_textview.setText(String.format("%.2f", balance));
        String expenditure_income = String.format("支出：%.2f | 收入：%.2f", expense, income);
        MaterialTextView expense_income_textview = binding.expenseIncomeText;
        expense_income_textview.setText(expenditure_income);

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

        //补偿占比精度问题
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
        expense_adapter.notifyDataSetChanged();
        income_adapter.notifyDataSetChanged();
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
    @Contract(pure = true)
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
    @SuppressLint("DefaultLocale")
    private void showDatePickerDialog() {
        //实例化一个日期对象用于存放选中的日期
        Calendar selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));  //指定UTC时区以确保早晨不会选择到昨天
        selectedCalendar.set(year, month - 1, day);

        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");

        //显示日期选择器
        MaterialDatePicker<Long> datePicker = dateBuilder
                .setSelection(selectedCalendar.getTimeInMillis())
                .setCalendarConstraints(
                        new CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointBackward.now()) //限制为过去日期
                                .build()
                )
                .build();
        datePicker.show(getSupportFragmentManager(), TagString.DATE_PICKER.getValue());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected_calendar = Calendar.getInstance();
            selected_calendar.setTimeInMillis(selection);

            //更新日期文本视图
            int old_year = this.year;
            this.year = selected_calendar.get(Calendar.YEAR);
            this.month = selected_calendar.get(Calendar.MONTH) + 1;
            this.day = selected_calendar.get(Calendar.DAY_OF_MONTH);
            TextView date_textview = binding.reportDateTextview;
            date_textview.setText(String.format("%04d年%02d月%02d日", year, month, day));

            //重新加载报表信息
            List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
            updateSourceViews(dataList);
            if (old_year != selected_calendar.get(Calendar.YEAR)) {
                dataList = loadReportData(DateRangeType.THIS_YEAR);
                updateMonthAccountData(dataList);
                refreshMonthAccountInfoViews();
                month_account_adapter.onYearChanged();
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
                ((MaterialTextView) view).setText(R.string.today);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_month) {
                monthAccountLayout.setVisibility(View.GONE);
                dateRangeType = DateRangeType.THIS_MONTH;
                ((MaterialTextView) view).setText(R.string.this_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_recent_3_month) {
                monthAccountLayout.setVisibility(View.GONE);
                dateRangeType = DateRangeType.RECENT_3_MONTH;
                ((MaterialTextView) view).setText(R.string.recent_3_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_year) {
                monthAccountLayout.setVisibility(View.VISIBLE);
                dateRangeType = DateRangeType.THIS_YEAR;
                ((MaterialTextView) view).setText(R.string.this_year);
                itemClicked = true;
            }

            if (itemClicked) {
                List<ReportRunningAccountData> dataList = loadReportData(dateRangeType);
                updateSourceViews(dataList);
            }
            return itemClicked;
        });

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
                ((MaterialTextView) view).setText(R.string.balance);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_expense) {
                monthAccountInfoType = MonthAccountInfoType.EXPENSE;
                ((MaterialTextView) view).setText(R.string.expense);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_income) {
                monthAccountInfoType = MonthAccountInfoType.INCOME;
                ((MaterialTextView) view).setText(R.string.income);
                itemClicked = true;
            }

            if (itemClicked && old_type != monthAccountInfoType) {
                List<ReportRunningAccountData> dataList = loadReportData(DateRangeType.THIS_YEAR);
                updateMonthAccountData(dataList);
                refreshMonthAccountInfoViews();
                month_account_adapter.onMonthAccountInfoTypeChanged(monthAccountInfoType);
            }

            return itemClicked;
        });

        monthAccountInfoTypeMenu.show();
    }
}