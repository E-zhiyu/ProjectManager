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
import com.project.manager.RequestResultCode;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;

import java.util.List;

public class AnalysisRuleManageActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityResultLauncher<Intent> ruleAddLauncher;     //添加规则界面的启动器
    private ActivityResultLauncher<Intent> ruleModifyLauncher;  //修改规则的启动器
    private AnalysisRuleAdapter rule_adapter;                   //规则列表适配器
    private RecyclerView rule_recycler;                         //规则列表视图

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
            Intent skip2RuleAdd = new Intent(this, RuleAddModifyActivity.class);
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
        rule_adapter = new AnalysisRuleAdapter(ruleList, this::onRuleClicked, this);
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

        ruleModifyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode != Activity.RESULT_CANCELED) {
                        if (data != null) {
                            onAnalysisRuleModified(data, resultCode);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                    }
                }
        );
    }

    //规则点击处理方法
    private void onRuleClicked(int position, @NonNull AnalysisRule rule) {
        Intent skip2RuleModify = new Intent(this, RuleAddModifyActivity.class);
        Bundle dataBundle = new Bundle();

        String rule_name = rule.getRuleName();                          //规则名称
        long rule_no = rule.getRuleNo();                                //规则编号
        String account_type = rule.getType().toString();                //流水记录类型
        long tag_no = rule.getTagNo();                                  //标签编号
        String package_name = rule.getPackageName();                    //目标包名
        String notification_title = rule.getNotificationTitle();        //通知标题
        String notification_content = rule.getNotificationContent();    //通知内容

        dataBundle.putString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue(), rule_name);
        dataBundle.putLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue(), rule_no);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), account_type);
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        dataBundle.putString(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_TITLE.getValue(), notification_title);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_CONTENT.getValue(), notification_content);

        skip2RuleModify.putExtras(dataBundle);
        ruleModifyLauncher.launch(skip2RuleModify);
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

    private void onAnalysisRuleModified(@NonNull Intent resuleIntent, int resultCode) {
        Bundle dataBundle = resuleIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            rule_adapter.modifyRule(dataBundle);
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
            rule_adapter.deleteRule(position);
        }
    }
}