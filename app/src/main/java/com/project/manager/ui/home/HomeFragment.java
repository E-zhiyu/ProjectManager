package com.project.manager.ui.home;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.data.data_class.running_account.RunningAccountBase;
import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDatabaseHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;
import com.project.manager.data.data_save.preference.BookKeepingStartDatePreference;
import com.project.manager.databinding.FragmentHomeBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.report.ReportActivity;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;            //XML视图绑定引用
    double day_balance, day_expense, day_income;    //日结余、日支出、日收入

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        initViews();
        initBalanceView();

        return binding.getRoot();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置按钮的点击监听器
        binding.reportBtn.setOnClickListener(v -> {
            Intent skip2Report = new Intent(requireContext(), ReportActivity.class);
            startActivity(skip2Report);
        });

        MaterialTextView bookKeepingDaysText = binding.bookkeepingDaysText;     //记账天数文本视图

        //初始化记账日期
        String start_date_str = getBookKeepingStartDate();  //获取开始记账的日期
        long bookkeeping_days;
        if (!start_date_str.isEmpty()) {
            LocalDate startDate = LocalDate.parse(start_date_str);
            LocalDate currentDate = LocalDate.now();

            bookkeeping_days = ChronoUnit.DAYS.between(startDate, currentDate);  //计算相差的天数
        } else {
            bookkeeping_days = 0;   //无法获取则说明是第一天记账
        }
        if (bookkeeping_days != 0) {
            bookKeepingDaysText.setText(String.format(Locale.getDefault(), "您已累计记账%d天", bookkeeping_days));
        } else {
            bookKeepingDaysText.setText("这是您记账的第一天");
        }
    }

    /**
     * 初始化当日结余视图
     */
    private void initBalanceView() {
        try {
            getTodayBalanceInfo();
        } catch (SQLiteException e) {
            day_balance = 0;
            day_income = 0;
            day_expense = 0;
            ExceptionHelper.showExceptionDialog(requireContext(), e);
        }

        MaterialTextView balance_text, expense_income_text;
        balance_text = binding.balanceText;
        expense_income_text = binding.expenseIncomeText;

        balance_text.setText(String.format(Locale.getDefault(), "%.2f", day_balance));
        expense_income_text.setText(String.format(Locale.getDefault(), "支出：%s| 收入：%s", day_expense, day_income));
    }

    /**
     * 加载今日相关的流水数据
     */
    private void getTodayBalanceInfo() throws SQLiteException {
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(requireContext());
        SQLiteDatabase db = db_helper.getReadableDatabase();

        //获取当前日期
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        //创建数据库游标
        String[] columns = new String[]{
                BookKeepingColumns.AMOUNT.toString(),
                BookKeepingColumns.TYPE.toString()
        };
        String selection = BookKeepingColumns.DATETIME + ">=? AND " + BookKeepingColumns.DATETIME + "<?";
        String[] selectionArgs = {
                String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day),
                String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day + 1)
        };
        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //读取数据库内容
        day_balance = 0;
        day_expense = 0;
        day_income = 0;
        while (basic_cursor.moveToNext()) {
            RunningAccountType type = RunningAccountType.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString())));
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.AMOUNT.toString()));

            if (type.isExpenseType()) {
                day_balance -= amount;
                day_expense += amount;
            } else {
                day_balance += amount;
                day_income += amount;
            }
        }

        basic_cursor.close();
        db.close();
    }

    /**
     * 获取开始记账的日期
     *
     * @return 开始记账的日期字符串（无法获取则为空串）
     */
    private String getBookKeepingStartDate() {
        String start_date_str = BookKeepingStartDatePreference.getStartDate(requireContext());
        if (start_date_str.isEmpty()) {
            try {
                start_date_str = RunningAccountBase.getEarliestAccountDate(requireContext());
                if (!start_date_str.isEmpty()) {
                    BookKeepingStartDatePreference.saveStartDate(start_date_str, requireContext());
                }
            } catch (SQLiteException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
            }
        }

        return start_date_str;
    }
}