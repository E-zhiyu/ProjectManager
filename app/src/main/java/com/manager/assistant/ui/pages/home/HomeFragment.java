package com.manager.assistant.ui.pages.home;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.data.data_class.running_account.RunningAccountBase;
import com.manager.assistant.data.data_save.database.BookKeepingColumns;
import com.manager.assistant.data.data_save.database.BookKeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookKeepingTables;
import com.manager.assistant.data.data_save.preference.BookKeepingStartDatePreference;
import com.manager.assistant.databinding.FragmentHomeBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.pages.home.report.ReportActivity;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;                    //XML视图绑定引用
    private double day_balance, day_expense, day_income;    //日结余、日支出、日收入
    private final CompositeDisposable disposables = new CompositeDisposable();

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

    @Override
    public void onResume() {
        super.onResume();

        //每次Fragment变为可见时刷新数据
        refreshUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        disposables.dispose();
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

        //初始化记账日期
        String start_date_str = getBookKeepingStartDate();  //获取开始记账的日期
        long bookkeeping_day_num;
        if (!start_date_str.isEmpty()) {
            LocalDate startDate = LocalDate.parse(start_date_str);
            LocalDate currentDate = LocalDate.now();

            bookkeeping_day_num = ChronoUnit.DAYS.between(startDate, currentDate);  //计算相差的天数
        } else {
            bookkeeping_day_num = 0;   //无法获取则说明是第一天记账
        }
        if (bookkeeping_day_num != 0) {
            binding.bookkeepingDaysText.setText(
                    String.format(
                            Locale.getDefault(),
                            "您已累计记账%d天",
                            bookkeeping_day_num
                    )
            );
        } else {
            binding.bookkeepingDaysText.setText("这是您记账的第一天");
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

        binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", day_balance));
        binding.expenseIncomeText.setText(
                String.format(
                        Locale.getDefault(),
                        "支出：%.2f | 收入：%.2f",
                        day_expense, day_income
                )
        );
    }

    /**
     * 加载今日相关的流水数据
     */
    private void getTodayBalanceInfo() throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(requireContext());
        SQLiteDatabase db = db_helper.openReadLink();

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

    /**
     * 刷新UI的方法
     */
    private void refreshUI() {
        disposables.add(
                Observable.fromCallable(() -> {
                            getTodayBalanceInfo();
                            return getBookKeepingStartDate();
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(startDateStr -> {
                            //更新今日结余统计
                            binding.balanceText.setText(String.format(Locale.getDefault(), "%.2f", day_balance));
                            binding.expenseIncomeText.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "支出：%.2f | 收入：%.2f",
                                            day_expense, day_income
                                    )
                            );

                            //更新记账累计日期
                            long bookkeeping_day_num;
                            if (!startDateStr.isEmpty()) {
                                LocalDate startDate = LocalDate.parse(startDateStr);
                                LocalDate currentDate = LocalDate.now();

                                bookkeeping_day_num = ChronoUnit.DAYS.between(startDate, currentDate);  //计算相差的天数
                            } else {
                                bookkeeping_day_num = 0;   //无法获取则说明是第一天记账
                            }
                            if (bookkeeping_day_num != 0) {
                                binding.bookkeepingDaysText.setText(
                                        String.format(
                                                Locale.getDefault(),
                                                "您已累计记账%d天",
                                                bookkeeping_day_num
                                        )
                                );
                            } else {
                                binding.bookkeepingDaysText.setText("这是您记账的第一天");
                            }
                        }, e -> {
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                            Toast.makeText(requireContext(), "界面刷新出错", Toast.LENGTH_SHORT).show();
                        })
        );
    }
}