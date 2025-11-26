package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.edit;

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
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;

import java.util.List;

public class AnalysisRuleActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityResultLauncher<Intent> ruleAddLauncher; //添加规则界面的启动器

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
        AnalysisRuleAdapter adapter = new AnalysisRuleAdapter(ruleList);
        RecyclerView rule_recycler = findViewById(R.id.rule_recycler);
        rule_recycler.setAdapter(adapter);
    }

    private void initLaunchers() {
        ruleAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    //TODO: 完善规则添加回调
                }
        );
    }
}