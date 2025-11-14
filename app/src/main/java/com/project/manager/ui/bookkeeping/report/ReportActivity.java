package com.project.manager.ui.bookkeeping.report;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.database.RunningAccountColumns;
import com.project.manager.database.RunningAccountDatabaseHelper;
import com.project.manager.database.RunningAccountTables;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.ui.bookkeeping.tag.Tag;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {
    //余额增加的流水种类
    private final RunningAccountType[] income_type_array = {
            RunningAccountType.INCOME
    };
    //余额减少的流水种类
    private final RunningAccountType[] expense_type_array = {
            RunningAccountType.EXPENSE,
            RunningAccountType.TRANSFER
    };
    private final List<AccountSourceCard> expenseSourceCardList = new ArrayList<>();    //支出来源列表
    private final List<AccountSourceCard> incomeSourceCardList = new ArrayList<>();     //收入来源列表
    private int year, month, day;                                   //年月日
    DateRangeType dateRangeType = DateRangeType.TODAY;              //日期范围种类
    RecyclerView expense_source_recycler, income_source_recycler;   //收支来源布局
    AccountSourceAdapter expense_adapter, income_adapter;           //收支来源布局适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initViews();

        List<ReportRunningAccountData> dataList = loadReportData(); //加载报表数据
        updateViews(dataList);                                      //更新视图
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.report_date_textview) {
            showDatePickerDialog();
        } else if (v.getId() == R.id.date_range_select_view) {
            showDateRangeSelectPopupMenu(v);
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH) + 1;
        day = now.get(Calendar.DAY_OF_MONTH);
        @SuppressLint("DefaultLocale") String date_str = String.format("%04d年%02d月%02d日", year, month, day);

        //设置点击监听器
        MaterialTextView date_textview = findViewById(R.id.report_date_textview);
        date_textview.setText(date_str);
        date_textview.setOnClickListener(this);
        MaterialTextView date_range_select_view = findViewById(R.id.date_range_select_view);
        date_range_select_view.setOnClickListener(this);

        //获取收支来源RecyclerView并设置适配器
        expense_source_recycler = findViewById(R.id.expense_source_recycler);
        expense_adapter = new AccountSourceAdapter(expenseSourceCardList);
        expense_source_recycler.setAdapter(expense_adapter);
        income_source_recycler = findViewById(R.id.income_source_recycler);
        income_adapter = new AccountSourceAdapter(incomeSourceCardList);
        income_source_recycler.setAdapter(income_adapter);

        //禁用两个RecyclerView的滚动
        expense_source_recycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false; // 禁止竖向滑动
            }
        });
        income_source_recycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false; // 禁止竖向滑动
            }
        });
    }

    /**
     * 加载流水信息并生成报表数据
     */
    @SuppressLint("DefaultLocale")
    private List<ReportRunningAccountData> loadReportData() {
        List<ReportRunningAccountData> dataList = new ArrayList<>();

        try (RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(this)) {
            SQLiteDatabase db = db_helper.openReadLink();

            String[] columns = new String[]{
                    RunningAccountColumns.AMOUNT.toString(),
                    RunningAccountColumns.TYPE.toString(),
                    RunningAccountColumns.TAG_NO.toString()
            };
            String selection = RunningAccountColumns.DATETIME + ">=? AND " + RunningAccountColumns.DATETIME + "<?";

            //根据日期范围设置selection语句的参数
            String[] selectionArgs;
            switch (dateRangeType) {
                case TODAY:
                    selectionArgs = new String[]{String.format("%04d-%02d-%02d", year, month, day), String.format("%04d-%02d-%02d", year, month, day + 1)};
                    break;
                case THIS_MONTH:
                    selectionArgs = new String[]{String.format("%04d-%02d-01", year, month), String.format("%04d-%02d-01", year, month + 1)};
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
                    selectionArgs = new String[]{String.format("%04d-%02d-01", early_year, early_month), String.format("%04d-%02d-01", year, month + 1)};
                    break;
                case THIS_YEAR:
                    selectionArgs = new String[]{String.format("%04d-01-01", year), String.format("%04d-01-01", year + 1)};
                    break;
                default:
                    NullPointerException e = new NullPointerException("无法设置合法的日期范围");
                    ExceptionHelper.showExceptionDialog(this, e);
                    return dataList;
            }

            Cursor basic_cursor = db.query(
                    RunningAccountTables.BASIC.toString(),
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    RunningAccountColumns.DATETIME + " DESC"
            );

            while (basic_cursor.moveToNext()) {
                RunningAccountType type = RunningAccountType.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.TYPE.toString())));
                double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.AMOUNT.toString()));
                long tag_no = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.TAG_NO.toString()));

                ReportRunningAccountData oneRecordedData = new ReportRunningAccountData(type, amount, tag_no);
                dataList.add(oneRecordedData);
            }

            basic_cursor.close();
            db.close();
            return dataList;
        } catch (SQLiteDatabaseLockedException e) {
            ExceptionHelper.showExceptionDialog(this, e);
            return dataList;
        }
    }

    /**
     * 更新视图
     *
     * @param dataList 更新视图所需的数据
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateViews(@NonNull List<ReportRunningAccountData> dataList) {
        double expense = 0, income = 0; //总支出和总收入
        double balance = 0;             //结余
        incomeSourceCardList.clear();
        expenseSourceCardList.clear();
        for (ReportRunningAccountData oneRecordedData : dataList) {
            RunningAccountType type = oneRecordedData.getType();
            double amount = oneRecordedData.getAmount();
            long tag_no = oneRecordedData.getTag_no();

            //处理金额数据
            List<AccountSourceCard> targetList = null;  //待操作的来源列表
            if (isContainedInArray(income_type_array, type)) {
                income += amount;
                balance += amount;
                targetList = incomeSourceCardList;
            } else if (isContainedInArray(expense_type_array, type)) {
                expense += amount;
                balance -= amount;
                targetList = expenseSourceCardList;
            }

            if (targetList != null) {   //判断目标列表是否为空
                int index = isContainedInArray(targetList, tag_no);
                if (index != -1) {      //判断是否查询到对应的来源卡片
                    targetList.get(index).amountAdd(amount);
                } else {
                    if (tag_no != 0) {  //判断该流水记录是否有标签
                        String tag_name = Tag.tagNoTransToName(tag_no, this);
                        AccountSourceCard newSource = new AccountSourceCard(amount, tag_name, tag_no);
                        targetList.add(newSource);
                    } else {
                        AccountSourceCard otherSource = new AccountSourceCard(amount, "其他", tag_no);
                        targetList.add(otherSource);
                    }
                }
            }
        }

        //更新文本视图
        MaterialTextView balance_textview = findViewById(R.id.report_balance_textview);
        balance_textview.setText(String.valueOf(balance));
        String expenditure_income = String.format("支出：%s | 收入：%s", expense, income);
        MaterialTextView expense_income_textview = findViewById(R.id.expense_income_textview);
        expense_income_textview.setText(expenditure_income);

        //计算各来源的收支占比
        for (AccountSourceCard expenseSourceCard : expenseSourceCardList) {
            double source_amount = expenseSourceCard.getAmount();
            int percentage = (int) (source_amount * 100 / expense);
            expenseSourceCard.setPercentage(percentage);
        }
        for (AccountSourceCard incomeSourceCard : incomeSourceCardList) {
            double source_amount = incomeSourceCard.getAmount();
            int percentage = (int) (source_amount * 100 / expense);
            incomeSourceCard.setPercentage(percentage);
        }

        //更新收支来源视图
        expense_adapter.notifyDataSetChanged();
        income_adapter.notifyDataSetChanged();
    }

    /**
     * 判断某种类是否包含于指定的数组中
     *
     * @param typeArray 流水种类数组
     * @param type      流水种类
     * @return 指定的种类是否包含于数组中
     */
    private boolean isContainedInArray(@NonNull RunningAccountType[] typeArray, RunningAccountType type) {
        for (RunningAccountType runningAccountType : typeArray) {
            if (runningAccountType == type) return true;
        }

        return false;
    }

    /**
     * 判断某来源是否在来源列表中
     *
     * @param accountSourceCardList 来源列表
     * @param source_no             该来源的编号
     * @return 该来源对应在列表中的下标（未找到为-1）
     */
    private int isContainedInArray(@NonNull List<AccountSourceCard> accountSourceCardList, long source_no) {
        int index = 0;
        for (AccountSourceCard oneCard : accountSourceCardList) {
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
        //获取已选中的日期的日历对象
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(year, month - 1, day);

        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");

        //显示日期选择器
        MaterialDatePicker<Long> datePicker = dateBuilder
                .setSelection(selectedCalendar.getTimeInMillis())
                .build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected_calendar = Calendar.getInstance();
            selected_calendar.setTimeInMillis(selection);

            //更新日期文本视图
            this.year = selected_calendar.get(Calendar.YEAR);
            this.month = selected_calendar.get(Calendar.MONTH) + 1;
            this.day = selected_calendar.get(Calendar.DAY_OF_MONTH);
            TextView date_textview = findViewById(R.id.report_date_textview);
            date_textview.setText(String.format("%04d年%02d月%02d日", year, month, day));

            //重新加载报表信息
            loadReportData();
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
            boolean itemClicked = false;    //是否点击某个选项
            if (item.getItemId() == R.id.action_today) {
                dateRangeType = DateRangeType.TODAY;
                ((MaterialTextView) view).setText(R.string.today);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_month) {
                dateRangeType = DateRangeType.THIS_MONTH;
                ((MaterialTextView) view).setText(R.string.this_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_recent_3_month) {
                dateRangeType = DateRangeType.RECENT_3_MONTH;
                ((MaterialTextView) view).setText(R.string.recent_3_month);
                itemClicked = true;
            } else if (item.getItemId() == R.id.action_this_year) {
                dateRangeType = DateRangeType.THIS_YEAR;
                ((MaterialTextView) view).setText(R.string.this_year);
                itemClicked = true;
            }

            if (itemClicked) {
                List<ReportRunningAccountData> dataList = loadReportData();
                updateViews(dataList);
            }
            return itemClicked;
        });

        dateRangeSelectMenu.show();
    }
}