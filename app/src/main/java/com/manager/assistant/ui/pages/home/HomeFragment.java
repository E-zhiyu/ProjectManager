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
import com.manager.assistant.data.data_save.preference.AppSettingsPreference;
import com.manager.assistant.data.data_save.preference.BookKeepingStartDatePreference;
import com.manager.assistant.databinding.FragmentHomeBinding;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.WebsiteLinkFetchHelper;
import com.manager.assistant.ui.pages.home.report.ReportActivity;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
    private LinkAdapter linkAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        initViews();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
        initBalanceView();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        //每次Fragment变为可见时刷新数据
        refreshUI();

        if (!AppSettingsPreference.getHomeLinks(requireContext())) {
            binding.webLinkCard.setVisibility(View.GONE);
        } else {
            if (binding.webLinkCard.getVisibility() == View.GONE) {
                binding.linkLoadingIndicator.setVisibility(View.VISIBLE);
            }
            fetchLinks(false);
        }
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

        linkAdapter = new LinkAdapter(requireContext());
        binding.webLinkRecycler.setAdapter(linkAdapter);
        if (AppSettingsPreference.getHomeLinks(requireContext())) {
            fetchLinks(true);
        } else {
            binding.linkLoadingIndicator.setVisibility(View.GONE);
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

    /**
     * 从网页爬取超链接并显示在界面中
     *
     * @param isToastNeed 是否需要弹出吐司提示
     */
    private void fetchLinks(boolean isToastNeed) {
        disposables.add(
                Observable.fromCallable(() -> WebsiteLinkFetchHelper.getUrlJson("https://www.ccgp-shaanxi.gov.cn/freecms/rest/v1/notice/selectInfoForIndex.do?&siteId=a7a15d60-de5b-42f2-b35a-7e3efc34e54f&channel=1eb454a2-7ff7-4a3b-b12c-12acc2685bd1&currPage=1&pageSize=11&regionCode=610001&noticeType=001011,001012,001013,001014,001016,001019&cityOrArea=&selectTimeName=noticeTime"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(linkList -> {
                                    if (!linkList.isEmpty()) {
                                        linkAdapter.refreshLink(linkList);
                                        binding.webLinkCard.setVisibility(View.VISIBLE);
                                        if (isToastNeed) {
                                            Toast.makeText(requireContext(), "成功加载采购公告（可在设置中关闭）", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (isToastNeed) {
                                            Toast.makeText(requireContext(), "未获取到任何有效链接", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, e -> {
                                    if (e instanceof ProtocolException) {
                                        if (isToastNeed) {
                                            Toast.makeText(requireContext(), "无法获取公告", Toast.LENGTH_SHORT).show();
                                        }
                                    } else if (e instanceof SocketTimeoutException) {
                                        if (isToastNeed) {
                                            Toast.makeText(requireContext(), "连接超时，无法获取公告", Toast.LENGTH_SHORT).show();
                                        }
                                    } else if (e instanceof ConnectException || e instanceof UnknownHostException) {
                                        if (isToastNeed) {
                                            Toast.makeText(requireContext(), "无法获取公告，请检查网络连接", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    binding.linkLoadingIndicator.setVisibility(View.GONE);
                                },
                                () -> binding.linkLoadingIndicator.setVisibility(View.GONE)
                        )
        );
    }
}