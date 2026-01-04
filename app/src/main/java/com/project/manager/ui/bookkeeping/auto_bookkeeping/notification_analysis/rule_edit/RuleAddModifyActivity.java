package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.rule_edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.databinding.ActivityRuleAddModifyBinding;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.TagString;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select.PackageNameSelectActivity;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.data.data_class.Tag;
import com.project.manager.ui.bookkeeping.tag.select_sheet.TagSelectBottomSheet;
import com.project.manager.ui.data_communication.tag_modify.TagUpdateReason;
import com.project.manager.ui.data_communication.tag_modify.TagRepository;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.noties.markwon.Markwon;

public class RuleAddModifyActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private boolean isModifyMode = false;                           //是否为规则编辑模式
    private int viewHolderPosition;                                 //规则ViewHolder下标
    private long rule_no;                                           //规则编号
    private long tag_no = 0;                                        //标签编号
    private TextInputEditText ruleNameInput;                        //规则名称输入框
    private TextInputLayout ruleNameLayout;                         //规则名称输入框布局管理器
    private TextInputEditText typeInput;                            //种类输入框
    private TextInputEditText tagInput;                             //标签名称输入框
    private TextInputEditText packageNameInput;                     //包名输入框
    private TextInputLayout packageNameLayout;                      //包名输入框布局管理器
    private TextInputEditText titleInput;                           //通知标题输入框
    private TextInputLayout titleLayout;                            //通知标题输入框布局管理器
    private TextInputEditText contentInput;                         //通知内容输入框
    private TextInputLayout contentLayout;                          //通知内容输入框布局管理器
    private RunningAccountType type = RunningAccountType.EXPENSE;   //流水种类
    private TagSelectBottomSheet tagSheet;                          //标签选择弹出菜单
    private ActivityResultLauncher<Intent> packageNameSelectLauncher;   //包名选择启动器
    private ActivityRuleAddModifyBinding binding;                   //绑定的XML视图引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRuleAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        receiveInitData();
        initLaunchers();

        startObserveTag();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.input_instruction_btn) {
            showInputInstructionDialog();
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
                                typeInput.setText(type.getTitle());
                                break;
                            }
                            index++;
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton("关闭", (dialog, which) -> dialog.dismiss())
                    .show();
        } else if (v.getId() == R.id.package_name_input) {
            Intent skip2PackageNameSelect = new Intent(this, PackageNameSelectActivity.class);
            packageNameSelectLauncher.launch(skip2PackageNameSelect);
        } else if (v.getId() == R.id.tag_name_input) {
            tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked);
            tagSheet.show(getSupportFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
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
        } else if (v.getId() == R.id.delete_btn) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除规则")
                    .setMessage("确定要删除这条规则吗？")
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("确定", (dialog, which) -> {
                        Intent result2AnalysisRuleActivity = new Intent();
                        Bundle dataBundle = new Bundle();
                        dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), viewHolderPosition);
                        result2AnalysisRuleActivity.putExtras(dataBundle);

                        setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2AnalysisRuleActivity);
                        dialog.dismiss();
                        finish();
                    })
                    .show();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String err = null;
            if (v == ruleNameInput && String.valueOf(ruleNameInput.getText()).isEmpty()) {
                err = "规则名称不能为空";
                ruleNameLayout.setErrorEnabled(true);
                ruleNameLayout.setError(err);
            } else if (v == packageNameInput && String.valueOf(packageNameInput.getText()).isEmpty()) {
                err = "包名不能为空";
                packageNameLayout.setErrorEnabled(true);
                packageNameLayout.setError(err);
            } else if (v == titleInput && String.valueOf(titleInput.getText()).isEmpty()) {
                err = "通知标题不能为空";
                titleLayout.setErrorEnabled(true);
                titleLayout.setError(err);
            } else if (v == contentInput && String.valueOf(contentInput.getText()).isEmpty()) {
                err = "通知内容不能为空";
                contentLayout.setErrorEnabled(true);
                contentLayout.setError(err);
            } else if (v == contentInput) {
                try {
                    String content_pattern = String.valueOf(contentInput.getText());
                    Pattern.compile(content_pattern);
                } catch (PatternSyntaxException e) {
                    err = "通知内容正则表达式存在语法错误";
                    contentLayout.setErrorEnabled(true);
                    contentLayout.setError(err);
                }
            }

            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (v == ruleNameInput) {
                ruleNameLayout.setErrorEnabled(false);
                ruleNameLayout.setError(null);
            } else if (v == packageNameInput) {
                packageNameLayout.setErrorEnabled(false);
                packageNameLayout.setError(null);
            } else if (v == titleInput) {
                titleLayout.setErrorEnabled(false);
                titleLayout.setError(null);
            } else if (v == contentInput) {
                contentLayout.setErrorEnabled(false);
                contentLayout.setError(null);
            }
        }
    }

    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        //获取输入框引用
        ruleNameInput = binding.ruleNameInput;
        typeInput = binding.typeInput;
        tagInput = binding.tagNameInput;
        packageNameInput = binding.packageNameInput;
        titleInput = binding.notificationTitleInput;
        contentInput = binding.notificationContentInput;

        //获取输入框布局管理器引用
        ruleNameLayout = binding.ruleNameLayout;
        packageNameLayout = binding.packageNameLayout;
        titleLayout = binding.notificationTitleLayout;
        contentLayout = binding.notificationContentLayout;

        //设置点击监听器以及文本内容
        typeInput.setText(RunningAccountType.EXPENSE.getTitle());
        typeInput.setOnClickListener(this);
        tagInput.setOnClickListener(this);
        packageNameInput.setOnClickListener(this);
        binding.inputInstructionBtn.setOnClickListener(this);
        binding.finishBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);

        //设置焦点变更监听器
        ruleNameInput.setOnFocusChangeListener(this);
        packageNameInput.setOnFocusChangeListener(this);
        titleInput.setOnFocusChangeListener(this);
        contentInput.setOnFocusChangeListener(this);

        //设置正则表达式输入框右侧按钮功能
        contentLayout.setEndIconOnClickListener(v -> {
            int cursorPosition = contentInput.getSelectionStart();
            Editable editable = contentInput.getText();
            String textToInsert = "(\\d+\\.?\\d{0,2})";

            //在光标位置插入文本
            if (editable != null) {
                editable.insert(cursorPosition, textToInsert);

                //移动光标到插入文本之后
                contentInput.setSelection(cursorPosition + textToInsert.length());
            }
        });
    }

    //接收编辑模式下的初始化数据
    private void receiveInitData() {
        Bundle initData = getIntent().getExtras();
        isModifyMode = getIntent().getBooleanExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), false);
        if (initData != null && isModifyMode) {
            MaterialButton deleteBtn = binding.deleteBtn;
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setOnClickListener(this);

            MaterialToolbar toolbar = binding.toolbar;
            toolbar.setTitle(R.string.modify_rule);

            //解析数据
            String rule_name = initData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());               //规则名称
            rule_no = initData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());                            //规则编号
            viewHolderPosition = initData.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());              //视图下标
            type = RunningAccountType.valueOf(initData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));     //流水种类
            Tag rule_tag = Tag.getTagOfAnalysisRule(rule_no, this);                                     //标签
            tag_no = rule_tag.getTno();
            String package_name = initData.getString(KeyValueStrings.PACKAGE_NAME.getValue());                  //包名
            String notification_title = initData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());      //通知标题
            String notification_content = initData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());  //通知内容

            ruleNameInput.setText(rule_name);
            typeInput.setText(type.getTitle());
            tagInput.setText(rule_tag.getName());
            packageNameInput.setText(package_name);
            titleInput.setText(notification_title);
            contentInput.setText(notification_content);
        }
    }

    private void initLaunchers() {
        packageNameSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            onPackageNameSelected(data);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                    }
                }
        );
    }

    //处理包名选择方法
    private void onPackageNameSelected(@NonNull Intent data) {
        String package_name = data.getStringExtra(KeyValueStrings.PACKAGE_NAME.getValue());
        packageNameInput.setText(package_name);
        packageNameLayout.setError(null);
        packageNameLayout.setErrorEnabled(false);
    }

    //处理标签按钮点击事件
    public void onTagBtnClicked(long tag_no, String tag_name) {
        this.tag_no = tag_no;
        tagInput.setText(tag_name);
        tagSheet.dismiss();
    }

    //观察标签数据变化
    private void startObserveTag() {
        TagRepository repository = TagRepository.getInstance();
        repository.getChangedTagList().observe(this, tagList -> {
            if (tagList != null) {
                TagUpdateReason updateReason = repository.getUpdateReason();
                for (Tag tag : tagList) {
                    String tag_name = tag.getName();
                    long tag_no = tag.getTno();

                    if (tag_no == this.tag_no) {    //只有找到匹配的标签编号才修改
                        switch (updateReason) {
                            case RENAME:
                                tagInput.setText(tag_name);
                                break;
                            case DELETE:
                                this.tag_no = 0;
                                tagInput.setText("");
                                break;
                            case MERGE:
                                this.tag_no = Tag.nameTransToTno(tag_name, this);
                                tagInput.setText(tag_name);
                                break;
                        }
                    }
                }
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

        if (String.valueOf(ruleNameInput.getText()).isEmpty()) {
            errLayout = ruleNameLayout;
            err = "规则名称不能为空";
        } else if (String.valueOf(packageNameInput.getText()).isEmpty()) {
            errLayout = packageNameLayout;
            err = "包名不能为空";
        } else if (String.valueOf(titleInput.getText()).isEmpty()) {
            errLayout = titleLayout;
            err = "通知标题不能为空";
        } else if (String.valueOf(contentInput.getText()).isEmpty()) {
            errLayout = contentLayout;
            err = "通知内容不能为空";
        } else {
            try {
                String content_pattern = String.valueOf(contentInput.getText());
                Pattern pattern = Pattern.compile(content_pattern);
                int group_num = pattern.matcher("").groupCount();

                if (group_num < 1) {
                    errLayout = contentLayout;
                    err = "必须设置一个金额捕获组";
                }
            } catch (PatternSyntaxException e) {
                errLayout = contentLayout;
                err = "通知内容正则表达式存在语法错误";
            }
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

        String rule_name = String.valueOf(ruleNameInput.getText());
        String package_name = String.valueOf(packageNameInput.getText());
        String notification_title = String.valueOf(titleInput.getText());
        String notification_content = String.valueOf(contentInput.getText());

        dataBundle.putString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue(), rule_name);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        dataBundle.putString(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_TITLE.getValue(), notification_title);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_CONTENT.getValue(), notification_content);

        if (isModifyMode) {
            dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), viewHolderPosition);
            dataBundle.putLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue(), rule_no);
        }

        return dataBundle;
    }

    //显示输入说明对话框
    private void showInputInstructionDialog() {
        String instruction = "- 规则名称：该通知解析规则的名称  \n" +
                "- 流水类型：通过该规则解析得到的流水记录的类型  \n" +
                "- 标签(可选)：通过该规则解析得到的流水记录的标签  \n" +
                "- 应用包名：发送通知的APP包名  \n" +
                "- 通知标题：通知的标题(如“微信支付”)  \n" +
                "- 通知内容(正则表达式)：使用正则表达式匹配通知内容，若成功匹配则按照该规则添加新的流水记录。(注意：第一个捕获组务必为金额信息)  \n\n" +
                "**提示：如果您不会使用正则表达式，请尝试询问AI，并在[regex101](https://regex101.com/)中测试您的正则表达式**";

        View update_dialog_view = LayoutInflater.from(this)
                .inflate(R.layout.view_markdown_text, null);
        MaterialTextView text_view = update_dialog_view.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(text_view, instruction);

        new MaterialAlertDialogBuilder(this)
                .setTitle("输入说明")
                .setView(update_dialog_view)
                .setNegativeButton("关闭", (dialog, which) -> dialog.dismiss())
                .show();
    }
}