package com.project.manager.ui.bookkeeping.report;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.project.manager.R;
import com.project.manager.database.RunningAccountColumns;
import com.project.manager.database.RunningAccountDatabaseHelper;
import com.project.manager.database.RunningAccountTables;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountTypeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    //余额增加的流水种类
    private final RunningAccountTypeEnum[] money_increase = {RunningAccountTypeEnum.INCOME};
    //余额减少的流水种类
    private final RunningAccountTypeEnum[] money_decrease = {RunningAccountTypeEnum.EXPENSE, RunningAccountTypeEnum.TRANSFER};

    //日期
    private int year, month;                //年和月份
    private boolean isShowYearOnly = true;  //只显示年份

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initViews();

        //加载报表数据
        loadOrRefreshReport();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        Calendar now = Calendar.getInstance();
        this.year = now.get(Calendar.YEAR);
        this.month = now.get(Calendar.MONTH) + 1;
        @SuppressLint("DefaultLocale") String date_str = String.format("%04d年", this.year);

        TextView date_textview = findViewById(R.id.report_date_textview);
        date_textview.setText(date_str);

        //设置日期文本视图的点击监听器
        date_textview.setOnClickListener(this);

        //设置复选框状态改变监听器
        MaterialCheckBox yearOnlyCheckBox = findViewById(R.id.report_year_only_checkbox);
        yearOnlyCheckBox.setChecked(true);
        yearOnlyCheckBox.setOnCheckedChangeListener(this);
    }

    /**
     * 加载流水信息并生成报表数据
     */
    @SuppressLint("DefaultLocale")
    private void loadOrRefreshReport() {
        try (RunningAccountDatabaseHelper db_helper = new RunningAccountDatabaseHelper(this)) {
            double increase, decrease, balance; //总收入、总支出、结余
            balance = increase = decrease = 0;
            List<RunningAccountTypeEnum> money_increase_list = new ArrayList<>(Arrays.asList(money_increase));
            List<RunningAccountTypeEnum> money_decrease_list = new ArrayList<>(Arrays.asList(money_decrease));
            SQLiteDatabase db = db_helper.openReadLink();

            String[] columns = new String[]{RunningAccountColumns.AMOUNT.toString(), RunningAccountColumns.TYPE.toString()};
            String selection = RunningAccountColumns.DATETIME + ">=? AND " + RunningAccountColumns.DATETIME + "<?";
            String[] selectionArgs;
            if (isShowYearOnly) {
                selectionArgs = new String[]{String.format("%04d-01-01", this.year), String.format("%04d-01-01", this.year + 1)};
            } else {
                selectionArgs = new String[]{String.format("%04d-%02d-01", this.year, this.month), String.format("%04d-%02d-01", this.year, this.month + 1)};
            }
            Cursor basic_cursor = db.query(
                    RunningAccountTables.BASIC.toString(),
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            while (basic_cursor.moveToNext()) {
                RunningAccountTypeEnum type = RunningAccountTypeEnum.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.TYPE.toString())));
                double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(RunningAccountColumns.AMOUNT.toString()));
                if (money_increase_list.contains(type)) {
                    balance += amount;
                    increase += amount;
                } else if (money_decrease_list.contains(type)) {
                    balance -= amount;
                    decrease += amount;
                } else {
                    RuntimeException e = new RuntimeException("无法处理未知流水种类：" + type);
                    ExceptionHelper.showExceptionDialog(this, e);
                }
            }

            basic_cursor.close();
            db.close();

            //更新文本视图
            TextView balance_textview = findViewById(R.id.report_balance_textview);
            balance_textview.setText(String.valueOf(balance));
            String expenditure_income = String.format("支出：%s | 收入：%s", decrease, increase);
            TextView expenditure_income_textview = findViewById(R.id.report_expenditure_income_textview);
            expenditure_income_textview.setText(expenditure_income);
        } catch (SQLiteDatabaseLockedException e) {
            ExceptionHelper.showExceptionDialog(this, e);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.report_date_textview) {
            showDatePickerDialog();
        }
    }

    /**
     * 弹出日期选择页
     */
    @SuppressLint("DefaultLocale")
    private void showDatePickerDialog() {
        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("");
        dateBuilder.setTheme(R.style.DatePickerDialogStyle);

        //显示日期选择器
        MaterialDatePicker<Long> datePicker = dateBuilder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected_calendar = Calendar.getInstance();
            selected_calendar.setTimeInMillis(selection);

            //更新日期文本视图
            this.year = selected_calendar.get(Calendar.YEAR);
            this.month = selected_calendar.get(Calendar.MONTH) + 1;
            TextView date_textview = findViewById(R.id.report_date_textview);

            if (isShowYearOnly) {
                date_textview.setText(String.format("%04d年", this.year));
            } else {
                date_textview.setText(String.format("%04d年%02d月", this.year, this.month));
            }

            //重新加载报表信息
            loadOrRefreshReport();
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.report_year_only_checkbox) {
            TextView date_textview = findViewById(R.id.report_date_textview);

            if (isChecked) {
                this.isShowYearOnly = true;
                date_textview.setText(String.format("%04d年", this.year));
            } else {
                this.isShowYearOnly = false;
                date_textview.setText(String.format("%04d年%02d月", this.year, this.month));
            }

            loadOrRefreshReport();
        }
    }
}