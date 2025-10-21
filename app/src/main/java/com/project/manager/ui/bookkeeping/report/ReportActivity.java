package com.project.manager.ui.bookkeeping.report;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.manager.R;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.FlowTypeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {
    //余额增加的流水种类
    private final FlowTypeEnum[] money_increase = {FlowTypeEnum.INCOME};
    //余额减少的流水种类
    private final FlowTypeEnum[] money_decrease = {FlowTypeEnum.EXPENSE, FlowTypeEnum.TRANSFER};
    private double balance = 0;     //结余
    private double increase = 0;    //总共收入的金额
    private double decrease = 0;    //总共支出的金额

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //设置日期文本视图的点击监听器
        TextView date_textview = findViewById(R.id.report_date_textview);
        date_textview.setOnClickListener(this);

        //加载报表数据
        loadBalance();
        TextView balance_textview = findViewById(R.id.report_balance_textview);
        balance_textview.setText(String.valueOf(balance));
        @SuppressLint("DefaultLocale") String expenditure_income = String.format("支出：%s | 收入：%s", increase, decrease);
        TextView expenditure_income_textview = findViewById(R.id.report_expenditure_income_textview);
        expenditure_income_textview.setText(expenditure_income);
    }

    /**
     * 加载流水信息并生成结余数据
     */
    private void loadBalance() {
        try (FlowDatabaseHelper db_helper = new FlowDatabaseHelper(this);) {
            List<FlowTypeEnum> money_increase_list = new ArrayList<>(Arrays.asList(money_increase));
            List<FlowTypeEnum> money_decrease_list = new ArrayList<>(Arrays.asList(money_decrease));
            SQLiteDatabase db = db_helper.openReadLink();

            Cursor basic_cursor = db.query(
                    FlowDatabaseHelper.TABLE_BASIC,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            while (basic_cursor.moveToNext()) {
                FlowTypeEnum type = FlowTypeEnum.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_TYPE)));
                double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_AMOUNT));
                if (money_increase_list.contains(type)) {
                    balance += amount;
                    increase += amount;
                } else if (money_decrease_list.contains(type)) {
                    balance -= amount;
                    decrease += amount;
                } else {
                    throw new RuntimeException("无法处理未知流水种类：" + type);
                }
            }

            basic_cursor.close();
        } catch (SQLiteDatabaseLockedException e) {
            throw new RuntimeException("报表错误：数据库被其他进程占用");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.report_date_textview) {
            showDateSelectSheet();
        }
    }

    /**
     * 弹出日期选择页
     */
    private void showDateSelectSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        View defaultView = getLayoutInflater().inflate(R.layout.bottomsheet_date_select, null);
        bottomSheetDialog.setContentView(defaultView);

        bottomSheetDialog.show();
    }
}