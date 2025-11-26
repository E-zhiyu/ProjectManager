package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.KeyValueStrings;

public class RuleAddActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText rule_name_input;              //规则名称输入框
    private TextInputEditText type_input;                   //种类输入框
    private TextInputEditText package_name_input;           //包名输入框
    private TextInputEditText notification_title_input;     //通知标题输入框
    private TextInputEditText notification_content_input;   //通知内容输入框
    private long tag_no = 0;                                //标签编号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_add);

        initViews();
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.input_introduce_btn) {
            //TODO: 完善输入内容介绍按钮点击逻辑
        } else if (v.getId() == R.id.finish_btn) {
            String err = verifyInput();
            if (err == null) {
                Intent result2AnalysisRuleActivity = new Intent();
                Bundle dataBundle = getInputData();
                result2AnalysisRuleActivity.putExtras(dataBundle);
                setResult(Activity.RESULT_OK, result2AnalysisRuleActivity);
                finish();
            } else {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            finish();
        }
    }

    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rule_name_input = findViewById(R.id.rule_name_input);
        type_input = findViewById(R.id.type_input);
        package_name_input = findViewById(R.id.package_name_input);
        notification_title_input = findViewById(R.id.notification_title_input);
        notification_content_input = findViewById(R.id.notification_content_input);

        findViewById(R.id.input_introduce_btn).setOnClickListener(this);
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
    }

    /**
     * 检测输入内容的有效性
     *
     * @return 错误提示字符串（无错误为null）
     */
    private String verifyInput() {
        String err = null;

        //TODO: 完善输入内容检测方法

        return err;
    }

    /**
     * 获取输入的内容
     *
     * @return 包含输入内容的Bundle包裹
     */
    @NonNull
    private Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        String rule_name = String.valueOf(rule_name_input.getText());
        String type = String.valueOf(type_input.getText());
        String package_name = String.valueOf(package_name_input.getText());
        String notification_title = String.valueOf(notification_title_input.getText());
        String notification_content = String.valueOf(notification_content_input.getText());

        dataBundle.putString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue(), rule_name);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type);
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        dataBundle.putString(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_TITLE.getValue(), notification_title);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_CONTENT.getValue(), notification_content);

        return dataBundle;
    }
}