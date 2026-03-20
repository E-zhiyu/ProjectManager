package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit;

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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.classes.AnalysisRule;
import com.manager.assistant.databinding.ActivityRuleAddModifyBinding;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.appearence.AnimationHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.animators.ExpandFoldAnimator;
import com.manager.assistant.ui.pages.bookkeeping.notification_analysis.package_name_select.PackageNameSelectActivity;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.ui.others.bottom_sheets.tag.TagSelectBottomSheet;
import com.manager.assistant.ui.sync.tag.TagUpdateReason;
import com.manager.assistant.ui.sync.tag.TagRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.noties.markwon.Markwon;

public class RuleAddModifyActivity extends AppCompatActivity {
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

        receiveInitData();
        initViews();
        initLaunchers();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());

        startObserveTag();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //流水种类
        NoFilteringArrayAdapter<String> typeAdapter = new NoFilteringArrayAdapter<>(
                this,
                Arrays.stream(RunningAccountType.values())
                        .map(RunningAccountType::getTitle)
                        .toArray(String[]::new)
        );
        binding.typeInput.setText(type.getTitle());
        binding.typeInput.setAdapter(typeAdapter);
        binding.typeInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    if (position == RunningAccountType.TRANSFER.ordinal() && type != RunningAccountType.TRANSFER) {
                        ExpandFoldAnimator.expand(binding.transferInputLayout);
                    } else if (position != RunningAccountType.TRANSFER.ordinal() && type == RunningAccountType.TRANSFER) {
                        ExpandFoldAnimator.collapse(binding.transferInputLayout);
                    }

                    type = RunningAccountType.values()[position];
                }
        );

        //转出账户和转入账户添加适配器
        HashSet<String> accountSet = AnalysisRule.getAllExportOrImportAccounts(this);
        NoFilteringArrayAdapter<String> accountAdapter = new NoFilteringArrayAdapter<>(this, new ArrayList<>(accountSet));
        binding.exportAccountInput.setAdapter(accountAdapter);
        binding.importAccountInput.setAdapter(accountAdapter);

        //转出账户
        binding.exportAccountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String exportAccount = String.valueOf(binding.exportAccountInput.getText());
                if (exportAccount.isEmpty()) {
                    binding.exportAccountLayout.setErrorEnabled(true);
                    binding.exportAccountLayout.setError("转出账户不能为空");
                }
            } else {
                binding.exportAccountLayout.setError(null);
            }
        });
        binding.exportAccountInput.setOnClickListener(v -> binding.exportAccountLayout.setError(null));

        //转入账户
        binding.importAccountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String importAccount = String.valueOf(binding.importAccountInput.getText());
                if (importAccount.isEmpty()) {
                    binding.importAccountLayout.setErrorEnabled(true);
                    binding.importAccountLayout.setError("转入账户不能为空");
                }
            } else {
                binding.importAccountLayout.setError(null);
            }
        });
        binding.importAccountInput.setOnClickListener(v -> binding.importAccountLayout.setError(null));

        //标签名称
        binding.tagNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, type);
                tagSheet.show(getSupportFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
            }
        });
        binding.tagNameInput.setOnClickListener(v -> {
            tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, type);
            tagSheet.show(getSupportFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
        });

        //包名
        binding.packageNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.packageNameLayout.setError(null);
                Intent skip2PackageNameSelect = new Intent(this, PackageNameSelectActivity.class);
                packageNameSelectLauncher.launch(skip2PackageNameSelect);
            } else {
                String packageName = String.valueOf(binding.packageNameInput.getText());
                if (packageName.isEmpty()) {
                    binding.packageNameLayout.setErrorEnabled(true);
                    binding.packageNameLayout.setError("包名不能为空");
                }
            }
        });
        binding.packageNameInput.setOnClickListener(v -> {
            Intent skip2PackageNameSelect = new Intent(this, PackageNameSelectActivity.class);
            packageNameSelectLauncher.launch(skip2PackageNameSelect);
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
        binding.ruleNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.ruleNameLayout.setError(null);
            } else {
                String ruleName = String.valueOf(binding.ruleNameInput.getText());
                if (ruleName.isEmpty()) {
                    binding.ruleNameLayout.setErrorEnabled(true);
                    binding.ruleNameLayout.setError("规则名称不能为空");
                }
            }
        });
        binding.notificationTitleInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.notificationTitleLayout.setError(null);
            } else {
                String title = String.valueOf(binding.notificationTitleInput.getText());
                if (title.isEmpty()) {
                    binding.notificationTitleLayout.setErrorEnabled(true);
                    binding.notificationTitleLayout.setError("通知标题不能为空");
                }
            }
        });
        binding.notificationContentInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.notificationContentLayout.setError(null);
            } else {
                String content = String.valueOf(binding.notificationContentInput.getText());
                if (content.isEmpty()) {
                    binding.notificationContentLayout.setErrorEnabled(true);
                    binding.notificationContentLayout.setError("通知内容不能为空");
                }
            }
        });

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

    /**
     * 编辑模式接收初始化数据
     */
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

            binding.toolbar.setTitle(R.string.modify_rule);

            //解析数据
            String ruleName = initData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());                //规则名称
            rule_no = initData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());                            //规则编号
            viewHolderPosition = initData.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());              //视图下标
            type = RunningAccountType.valueOf(initData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));     //流水种类
            Tag ruleTag = Tag.getTagByRuleNo(rule_no, this);                                            //标签
            tag_no = ruleTag.getTno();
            String packageName = initData.getString(KeyValueStrings.PACKAGE_NAME.getValue());                   //包名
            String notificationTitle = initData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());       //通知标题
            String notificationContent = initData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());   //通知内容
            if (type == RunningAccountType.TRANSFER) {                                                          //转账账户信息
                binding.transferInputLayout.setVisibility(View.VISIBLE);
                List<String> transferAccountInfo = AnalysisRule.getTransferAccounts(rule_no, this);
                if (!transferAccountInfo.isEmpty()) {
                    String exportAccount = transferAccountInfo.get(0);
                    String importAccount = transferAccountInfo.get(1);
                    binding.exportAccountInput.setText(exportAccount);
                    binding.importAccountInput.setText(importAccount);
                }
            }

            binding.ruleNameInput.setText(ruleName);
            binding.tagNameInput.setText(ruleTag.getName());
            binding.packageNameInput.setText(packageName);
            binding.notificationTitleInput.setText(notificationTitle);
            binding.notificationContentInput.setText(notificationContent);
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
            if (tagList == null) {
                return;
            }

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
                        default:
                            break;
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
        } else if (String.valueOf(binding.exportAccountInput.getText()).isEmpty() && type == RunningAccountType.TRANSFER) {
            errLayout = binding.exportAccountLayout;
            err = "转出账户不能为空";
        } else if (String.valueOf(binding.importAccountInput.getText()).isEmpty() && type == RunningAccountType.TRANSFER) {
            errLayout = binding.importAccountLayout;
            err = "转入账户不能为空";
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

        String ruleName = String.valueOf(binding.ruleNameInput.getText());
        String packageName = String.valueOf(binding.packageNameInput.getText());
        String notificationTitle = String.valueOf(binding.notificationTitleInput.getText());
        String notificationContent = String.valueOf(binding.notificationContentInput.getText());

        dataBundle.putString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue(), ruleName);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        dataBundle.putString(KeyValueStrings.PACKAGE_NAME.getValue(), packageName);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_TITLE.getValue(), notificationTitle);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_CONTENT.getValue(), notificationContent);

        //写入转账相关的数据
        if (type == RunningAccountType.TRANSFER) {
            String exportAccount = String.valueOf(binding.exportAccountInput.getText());
            String importAccount = String.valueOf(binding.importAccountInput.getText());
            dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
            dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);
        }

        if (isModifyMode) {
            dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), viewHolderPosition);
            dataBundle.putLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue(), rule_no);
        }

        return dataBundle;
    }

    /**
     * 显示输入说明对话框
     */
    private void showInputInstructionDialog() {
        String instruction = "- 规则名称：该通知解析规则的名称  \n" +
                "- 流水类型：通过该规则解析得到的流水记录的类型  \n" +
                "- 转出账户(仅转账类型)：转出财产的金融账户名称  \n" +
                "- 转入账户(仅转账类型)：转入财产的进入账户名称  \n" +
                "- 标签(可选)：通过该规则解析得到的流水记录的标签  \n" +
                "- 应用包名：发送通知的APP包名  \n" +
                "- 通知标题：通知的标题(如“微信支付”)  \n" +
                "- 通知内容(正则表达式)：使用正则表达式匹配通知内容，若成功匹配则按照该规则添加新的流水记录。(注意：第一个捕获组务必为金额信息)  \n\n" +
                "**提示：如果您不会使用正则表达式，请尝试询问AI，跟AI说明哪个数字为金额数据并让其编写使用捕获组捕获该数字的正则表达式，并在[regex101](https://regex101.com/)中测试您得到的正则表达式是否符合要求**";

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