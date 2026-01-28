package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.databinding.ActivityRuleAddModifyBinding;
import com.manager.assistant.enums.RequestResultCode;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.enums.TagString;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.pages.bookkeeping.notification_analysis.package_name_select.PackageNameSelectActivity;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.manager.assistant.ui.data_communication.tag_modify.TagUpdateReason;
import com.manager.assistant.ui.data_communication.tag_modify.TagRepository;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.noties.markwon.Markwon;

public class RuleAddModifyActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    private boolean isModifyMode = false;                           //是否为规则编辑模式
    private int viewHolderPosition;                                 //规则ViewHolder下标
    private long rule_no;                                           //规则编号
    private long tag_no = 0;                                        //标签编号
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
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
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
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String err = null;
            if (v == binding.ruleNameInput && String.valueOf(binding.ruleNameInput.getText()).isEmpty()) {
                err = "规则名称不能为空";
                binding.ruleNameLayout.setErrorEnabled(true);
                binding.ruleNameLayout.setError(err);
            } else if (v == binding.packageNameInput && String.valueOf(binding.packageNameInput.getText()).isEmpty()) {
                err = "包名不能为空";
                binding.packageNameLayout.setErrorEnabled(true);
                binding.packageNameLayout.setError(err);
            } else if (v == binding.notificationTitleInput && String.valueOf(binding.notificationTitleInput.getText()).isEmpty()) {
                err = "通知标题不能为空";
                binding.notificationTitleLayout.setErrorEnabled(true);
                binding.notificationTitleLayout.setError(err);
            } else if (v == binding.notificationContentInput && String.valueOf(binding.notificationContentInput.getText()).isEmpty()) {
                err = "通知内容不能为空";
                binding.notificationContentLayout.setErrorEnabled(true);
                binding.notificationContentLayout.setError(err);
            } else if (v == binding.notificationContentInput) {
                try {
                    String content_pattern = String.valueOf(binding.notificationContentInput.getText());
                    Pattern.compile(content_pattern);
                } catch (PatternSyntaxException e) {
                    err = "通知内容正则表达式存在语法错误";
                    binding.notificationContentLayout.setErrorEnabled(true);
                    binding.notificationContentLayout.setError(err);
                }
            }

            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (v == binding.ruleNameInput) {
                binding.ruleNameLayout.setError(null);
            } else if (v == binding.packageNameInput) {
                binding.packageNameLayout.setError(null);
            } else if (v == binding.notificationTitleInput) {
                binding.notificationTitleLayout.setError(null);
            } else if (v == binding.notificationContentInput) {
                binding.notificationContentLayout.setError(null);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        //流水种类
        NoFilteringArrayAdapter<String> adapter = new NoFilteringArrayAdapter<>(
                this,
                new String[]{
                        RunningAccountType.EXPENSE.getTitle(),
                        RunningAccountType.INCOME.getTitle()
                }
        );
        binding.typeInput.setAdapter(adapter);
        binding.typeInput.setText(RunningAccountType.EXPENSE.getTitle());
        binding.typeInput.setOnItemClickListener(
                (parent, view, position, id) -> type = RunningAccountType.values()[position]
        );

        //标签名称
        binding.tagNameInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, type);
                tagSheet.show(getSupportFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
            }
            return false;
        });

        //包名
        binding.packageNameInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Intent skip2PackageNameSelect = new Intent(this, PackageNameSelectActivity.class);
                packageNameSelectLauncher.launch(skip2PackageNameSelect);
            }
            return false;
        });

        //其他按钮
        binding.inputInstructionBtn.setOnClickListener(v -> showInputInstructionDialog());
        binding.finishBtn.setOnClickListener(v -> {
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
        });
        binding.cancelBtn.setOnClickListener(v -> finish());

        //设置焦点变更监听器
        binding.ruleNameInput.setOnFocusChangeListener(this);
        binding.packageNameInput.setOnFocusChangeListener(this);
        binding.notificationTitleInput.setOnFocusChangeListener(this);
        binding.notificationContentInput.setOnFocusChangeListener(this);

        //设置正则表达式输入框右侧按钮功能
        binding.notificationContentLayout.setEndIconOnClickListener(v -> {
            int cursorPosition = binding.notificationContentInput.getSelectionStart();
            Editable editable = binding.notificationContentInput.getText();
            String textToInsert = "(\\d+\\.?\\d{0,2})";

            //在光标位置插入文本
            if (editable != null) {
                editable.insert(cursorPosition, textToInsert);

                //移动光标到插入文本之后
                binding.notificationContentInput.setSelection(cursorPosition + textToInsert.length());
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
            deleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
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
                    .show());

            MaterialToolbar toolbar = binding.toolbar;
            toolbar.setTitle(R.string.modify_rule);

            //解析数据
            String rule_name = initData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());               //规则名称
            rule_no = initData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());                            //规则编号
            viewHolderPosition = initData.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());              //视图下标
            type = RunningAccountType.valueOf(initData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));     //流水种类
            Tag rule_tag = Tag.getTagByRuleNo(rule_no, this);                                     //标签
            tag_no = rule_tag.getTno();
            String package_name = initData.getString(KeyValueStrings.PACKAGE_NAME.getValue());                  //包名
            String notification_title = initData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());      //通知标题
            String notification_content = initData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());  //通知内容

            binding.ruleNameInput.setText(rule_name);
            binding.typeInput.setText(type.getTitle());
            binding.tagNameInput.setText(rule_tag.getName());
            binding.packageNameInput.setText(package_name);
            binding.notificationTitleInput.setText(notification_title);
            binding.notificationContentInput.setText(notification_content);
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
        binding.packageNameInput.setText(package_name);
        binding.packageNameLayout.setError(null);
    }

    //处理标签按钮点击事件
    public void onTagBtnClicked(long tag_no, String tag_name) {
        this.tag_no = tag_no;
        binding.tagNameInput.setText(tag_name);
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
                                binding.tagNameInput.setText(tag_name);
                                break;
                            case DELETE:
                                this.tag_no = 0;
                                binding.tagNameInput.setText("");
                                break;
                            case MERGE:
                                this.tag_no = Tag.nameTransToTno(tag_name, this);
                                binding.tagNameInput.setText(tag_name);
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

        if (String.valueOf(binding.ruleNameInput.getText()).isEmpty()) {
            errLayout = binding.ruleNameLayout;
            err = "规则名称不能为空";
        } else if (String.valueOf(binding.packageNameInput.getText()).isEmpty()) {
            errLayout = binding.packageNameLayout;
            err = "包名不能为空";
        } else if (String.valueOf(binding.notificationTitleInput.getText()).isEmpty()) {
            errLayout = binding.notificationTitleLayout;
            err = "通知标题不能为空";
        } else if (String.valueOf(binding.notificationContentInput.getText()).isEmpty()) {
            errLayout = binding.notificationContentLayout;
            err = "通知内容不能为空";
        } else {
            try {
                String content_pattern = String.valueOf(binding.notificationContentInput.getText());
                Pattern pattern = Pattern.compile(content_pattern);
                int group_num = pattern.matcher("").groupCount();

                if (group_num < 1) {
                    errLayout = binding.notificationContentLayout;
                    err = "必须设置一个金额捕获组";
                }
            } catch (PatternSyntaxException e) {
                errLayout = binding.notificationContentLayout;
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

        String rule_name = String.valueOf(binding.ruleNameInput.getText());
        String package_name = String.valueOf(binding.packageNameInput.getText());
        String notification_title = String.valueOf(binding.notificationTitleInput.getText());
        String notification_content = String.valueOf(binding.notificationContentInput.getText());

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