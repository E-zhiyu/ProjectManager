package com.project.manager.ui.setting.auto_bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.project.manager.R;

public class NotificationAnalysisActivity extends AppCompatActivity implements View.OnClickListener {
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