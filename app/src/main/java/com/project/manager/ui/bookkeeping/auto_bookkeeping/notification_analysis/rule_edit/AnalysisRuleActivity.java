package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.rule_edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.project.manager.R;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;

import java.util.List;

public class AnalysisRuleActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityResultLauncher<Intent> ruleAddLauncher; //添加规则界面的启动器
    private AnalysisRuleAdapter rule_adapter;               //规则列表适配器
    private RecyclerView rule_recycler;                     //规则列表视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_analysis);

        initViews();
        initLaunchers();
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.rule_add_btn) {
            Intent skip2RuleAdd = new Intent(this, RuleAddActivity.class);
            ruleAddLauncher.launch(skip2RuleAdd);
        }
    }

    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.rule_add_btn).setOnClickListener(this);

        //设置RecyclerView的适配器
        List<AnalysisRule> ruleList = AnalysisRule.loadAnalysisRule(this);
        rule_adapter = new AnalysisRuleAdapter(ruleList, this);
        rule_recycler = findViewById(R.id.rule_recycler);
        rule_recycler.setAdapter(rule_adapter);
    }

    private void initLaunchers() {
        ruleAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            onAnalysisRuleAdded(data);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                    }
                }
        );
    }

    private void onAnalysisRuleAdded(@NonNull Intent resuleIntent) {
        Bundle dataBundle = resuleIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        rule_adapter.addRule(dataBundle);
        rule_recycler.scrollToPosition(0);
    }
}