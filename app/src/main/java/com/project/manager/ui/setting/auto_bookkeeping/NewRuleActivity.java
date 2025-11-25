package com.project.manager.ui.setting.auto_bookkeeping;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.project.manager.R;

public class NewRuleActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_rule);

        initViews();
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.input_introduce_btn) {
            //TODO: 完善输入内容介绍按钮点击逻辑
        } else if (v.getId() == R.id.finish_btn) {
            //TODO: 完善完成按钮点击逻辑
        } else if (v.getId() == R.id.cancel_btn) {
            finish();
        }
    }

    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.input_introduce_btn).setOnClickListener(this);
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
    }
}