package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.manager.ManagerAssistant;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.TagString;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.select_sheet.TagSelectBottomSheet;
import com.project.manager.ui.view_model.AccountTagModifyID;
import com.project.manager.ui.view_model.AccountTagViewModel;
import com.project.manager.ui.view_model.TagWithModifyID;

public class RuleAddActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText rule_name_input;                      //规则名称输入框
    private TextInputLayout rule_name_layout;                       //规则名称输入框布局管理器
    private TextInputEditText type_input;                           //种类输入框
    private TextInputEditText tag_input;                            //标签名称输入框
    private TextInputEditText package_name_input;                   //包名输入框
    private TextInputLayout package_name_layout;                    //包名输入框布局管理器
    private TextInputEditText notification_title_input;             //通知标题输入框
    private TextInputLayout notification_title_layout;              //通知标题输入框布局管理器
    private TextInputEditText notification_content_input;           //通知内容输入框
    private TextInputLayout notification_content_layout;            //通知内容输入框布局管理器
    private long tag_no = 0;                                        //标签编号
    private RunningAccountType type = RunningAccountType.EXPENSE;   //流水种类
    private AccountTagViewModel tagViewModel;                       //用于更新标签名称的ViewModel
    private TagSelectBottomSheet tag_sheet;                         //标签选择弹出菜单

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_add);

        initViews();

        //获取更新Tag名称的ViewModel
        ManagerAssistant app = (ManagerAssistant) getApplication();
        tagViewModel = app.getAccountTagViewModel();
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.input_introduce_btn) {
            //TODO: 完善输入内容介绍按钮点击逻辑
        } else if (v.getId() == R.id.type_input) {
            String[] types = {RunningAccountType.EXPENSE.getTitle(),
                    RunningAccountType.INCOME.getTitle()
            };

            new MaterialAlertDialogBuilder(this)
                    .setTitle("流水种类选择")
                    .setSingleChoiceItems(types, type.ordinal(), (dialog, which) -> {
                        int index = 0;
                        for (RunningAccountType selected_type : RunningAccountType.values()) {
                            if (index == which) {
                                type = selected_type;
                                type_input.setText(type.getTitle());
                                break;
                            }
                            index++;
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton("关闭", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if (v.getId() == R.id.tag_name_input) {
            tag_sheet = new TagSelectBottomSheet(this::onTagBtnClicked, this::startObserveTag);
            tag_sheet.show(getSupportFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
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
        tag_input = findViewById(R.id.tag_name_input);
        package_name_input = findViewById(R.id.package_name_input);
        notification_title_input = findViewById(R.id.notification_title_input);
        notification_content_input = findViewById(R.id.notification_content_input);

        rule_name_layout = findViewById(R.id.rule_name_layout);
        package_name_layout = findViewById(R.id.package_name_layout);
        notification_title_layout = findViewById(R.id.notification_title_layout);
        notification_content_layout = findViewById(R.id.notification_content_layout);

        type_input.setText(RunningAccountType.EXPENSE.getTitle());
        type_input.setOnClickListener(this);
        tag_input.setOnClickListener(this);
        findViewById(R.id.input_introduce_btn).setOnClickListener(this);
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
    }

    //处理标签按钮点击事件
    public void onTagBtnClicked(long tag_no, String tag_name) {
        this.tag_no = tag_no;
        tag_input.setText(tag_name);
        tag_sheet.dismiss();
    }

    //观察标签数据变化
    private void startObserveTag() {
        tagViewModel.getTag().observe(this, tagList -> {
            if (tagList != null) {  //判断是否为调用resetTagValue()方法后传入的null值
                for (TagWithModifyID tag : tagList) {
                    String tag_name = tag.getTag_name();
                    long tag_no = tag.getTag_no();
                    AccountTagModifyID modifyID = tag.getModifyID();

                    if (tag_no == this.tag_no) {    //只有找到匹配的标签编号才修改
                        switch (modifyID) {
                            case MODIFY:
                                tag_input.setText(tag_name);
                                break;
                            case DELETE:
                                this.tag_no = 0;
                                tag_input.setText("");
                                break;
                            case MERGE:
                                this.tag_no = Tag.nameTransToTno(tag_name, this);
                                tag_input.setText(tag_name);
                                break;
                        }
                    }
                }
                tagViewModel.resetTagValue();
            }
        });
    }

    /**
     * 检测输入内容的有效性
     *
     * @return 错误提示字符串（无错误为null）
     */
    private String verifyInput() {
        String err = null;
        TextInputLayout errLayout = null;

        if (String.valueOf(rule_name_input.getText()).isEmpty()) {
            errLayout = rule_name_layout;
            err = "规则名称不能为空";
        } else if (String.valueOf(package_name_input.getText()).isEmpty()) {
            errLayout = package_name_layout;
            err = "包名不能为空";
        } else if (String.valueOf(notification_title_input.getText()).isEmpty()) {
            errLayout = notification_title_layout;
            err = "通知标题不能为空";
        } else if (String.valueOf(notification_content_input.getText()).isEmpty()) {
            errLayout = notification_content_layout;
            err = "通知内容不能为空";
        }

        if (errLayout != null) {
            errLayout.setErrorEnabled(true);
            errLayout.setError(err);
        }

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
        String package_name = String.valueOf(package_name_input.getText());
        String notification_title = String.valueOf(notification_title_input.getText());
        String notification_content = String.valueOf(notification_content_input.getText());

        dataBundle.putString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue(), rule_name);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        dataBundle.putString(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_TITLE.getValue(), notification_title);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_CONTENT.getValue(), notification_content);

        return dataBundle;
    }
}