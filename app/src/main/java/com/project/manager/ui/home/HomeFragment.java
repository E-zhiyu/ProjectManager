package com.project.manager.ui.home;

import android.content.Intent;
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
import com.project.manager.data.data_save.preference.BookKeepingStartDatePreference;
import com.project.manager.databinding.FragmentHomeBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.report.ReportActivity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

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