package com.manager.assistant.ui.pages.notification_rule;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.automation.broadcast.BroadcastActions;
import com.manager.assistant.data.controllers.RuleDataController;
import com.manager.assistant.data.controllers.TagDataController;
import com.manager.assistant.databinding.ActivityRuleAddModifyBinding;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.animators.ExpandFoldAnimator;
import com.manager.assistant.ui.pages.package_name_select.PackageNameSelectActivity;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.data.classes.Tag;
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
    private long ruleNo;                                            //规则编号
    private long tagNo = 0;                                         //标签编号
    private AccountType type = AccountType.EXPENSE;   //流水种类
    private ActivityResultLauncher<Intent> packageNameSelectLauncher;   //包名选择启动器
    private ActivityRuleAddModifyBinding binding;                   //绑定的XML视图引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRuleAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
            );
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        receiveInitData();
        initViews();
        initLaunchers();

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
                Arrays.stream(AccountType.values())
                        .map(AccountType::getTitle)
                        .toArray(String[]::new)
        );
        binding.typeInput.setText(type.getTitle());
        binding.typeInput.setAdapter(typeAdapter);
        binding.typeInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    if (position == AccountType.TRANSFER.ordinal() && type != AccountType.TRANSFER) {
                        ExpandFoldAnimator.expand(binding.transferInputLayout);
                    } else if (position != AccountType.TRANSFER.ordinal() && type == AccountType.TRANSFER) {
                        ExpandFoldAnimator.collapse(binding.transferInputLayout);
                    }

                    type = AccountType.values()[position];
                }
        );

        //转出账户和转入账户添加适配器
        HashSet<String> accountSet = RuleDataController.getAllExportOrImportAccounts(this);
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
//        binding.tagInput.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, type);
//                tagSheet.show(getSupportFragmentManager(), TagStrings.TAG_SELECT_SHEET.getTag());
//            }
//        });
//        binding.tagInput.setOnClickListener(v -> {
//            tagSheet = new TagSelectBottomSheet(this::onTagBtnClicked, type);
//            tagSheet.show(getSupportFragmentManager(), TagStrings.TAG_SELECT_SHEET.getTag());
//        });

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

        //输入说明按钮
        binding.inputInstructionBtn.setOnClickListener(v -> showInputInstructionDialog());

        //完成按钮
        binding.finishBtn.setOnClickListener(v -> {
            String err = verifyInput();
            if (err == null) {
                Bundle dataBundle = getInputData();
                if (!isModifyMode) {
                    try {
                        ruleNo = RuleDataController.saveNewRule(dataBundle, this);
                        Toast.makeText(this, "解析规则添加成功", Toast.LENGTH_SHORT).show();
                        dataBundle.putLong(KeyStrings.ANALYSIS_RULE_NO.v(), ruleNo);

                        //发送规则更新广播
                        Intent ruleUpdated = new Intent(BroadcastActions.ACTION_RULES_UPDATED.toString());
                        sendBroadcast(ruleUpdated);
                    } catch (SQLiteException e) {
                        ExceptionHelper.showExceptionDialog(this, e);
                        Toast.makeText(this, "解析规则保存失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    try {
                        RuleDataController.modifyRule(dataBundle, this);
                        Toast.makeText(this, "解析规则修改成功", Toast.LENGTH_SHORT).show();

                        //发送规则更新广播
                        Intent ruleUpdated = new Intent(BroadcastActions.ACTION_RULES_UPDATED.toString());
                        sendBroadcast(ruleUpdated);
                    } catch (SQLiteException e) {
                        ExceptionHelper.showExceptionDialog(this, e);
                        Toast.makeText(this, "规则数据保存失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent result2AnalysisRuleActivity = new Intent();
                result2AnalysisRuleActivity.putExtras(dataBundle);
                setResult(Activity.RESULT_OK, result2AnalysisRuleActivity);
                finish();
            } else {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });

        //取消按钮
        binding.cancelBtn.setOnClickListener(v -> finish());

        //删除按钮
        binding.deleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("删除规则")
                .setMessage("确定要删除这条规则吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    try {
                        RuleDataController.deleteRule(ruleNo, this);
                        Toast.makeText(this, "规则删除成功", Toast.LENGTH_SHORT).show();

                        //发送广播
                        Intent ruleUpdated = new Intent(BroadcastActions.ACTION_RULES_UPDATED.toString());
                        sendBroadcast(ruleUpdated);
                    } catch (SQLiteException e) {
                        ExceptionHelper.showExceptionDialog(this, e);
                        Toast.makeText(this, "规则删除失败", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent result2AnalysisRuleActivity = new Intent();
                    Bundle dataBundle = getInputData();
                    result2AnalysisRuleActivity.putExtras(dataBundle);
                    setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2AnalysisRuleActivity);
                    finish();
                })
                .show()
        );

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
        isModifyMode = getIntent().getBooleanExtra(KeyStrings.IS_MODIFY_MODE.v(), false);
        if (initData != null && isModifyMode) {
            MaterialButton deleteBtn = binding.deleteBtn;
            deleteBtn.setVisibility(View.VISIBLE);

            binding.toolbar.setTitle(R.string.modify_rule);

            //解析数据
            String ruleName = initData.getString(KeyStrings.ANALYSIS_RULE_NAME.v());                //规则名称
            ruleNo = initData.getLong(KeyStrings.ANALYSIS_RULE_NO.v());                            //规则编号
            viewHolderPosition = initData.getInt(KeyStrings.VIEW_HOLDER_POSITION.v());              //视图下标
            type = AccountType.valueOf(initData.getString(KeyStrings.ACCOUNT_TYPE.v()));     //流水种类
            Tag ruleTag = TagDataController.getTagByRuleNo(ruleNo, this);                              //标签
            tagNo = ruleTag.getTno();
            String packageName = initData.getString(KeyStrings.PACKAGE_NAME.v());                   //包名
            String notificationTitle = initData.getString(KeyStrings.NOTIFICATION_TITLE.v());       //通知标题
            String notificationContent = initData.getString(KeyStrings.NOTIFICATION_CONTENT.v());   //通知内容
            if (type == AccountType.TRANSFER) {                                                          //转账账户信息
                binding.transferInputLayout.setVisibility(View.VISIBLE);
                List<String> transferAccountInfo = RuleDataController.getTransferAccounts(ruleNo, this);
                if (!transferAccountInfo.isEmpty()) {
                    String exportAccount = transferAccountInfo.get(0);
                    String importAccount = transferAccountInfo.get(1);
                    binding.exportAccountInput.setText(exportAccount);
                    binding.importAccountInput.setText(importAccount);
                }
            }

            binding.ruleNameInput.setText(ruleName);
            binding.tagInput.setText(ruleTag.getName());
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
        String packageName = data.getStringExtra(KeyStrings.PACKAGE_NAME.v());
        binding.packageNameInput.setText(packageName);
        binding.packageNameLayout.setError(null);
    }

    //处理标签按钮点击事件
//    public void onTagBtnClicked(long tagNo, String tag_name) {
//        this.tagNo = tagNo;
//        binding.tagInput.setText(tag_name);
//        tagSheet.dismiss();
//    }

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

                if (tag_no == this.tagNo) {    //只有找到匹配的标签编号才修改
                    switch (updateReason) {
                        case RENAME:
                            binding.tagInput.setText(tag_name);
                            break;
                        case DELETE:
                            this.tagNo = 0;
                            binding.tagInput.setText("");
                            break;
                        case MERGE:
                            this.tagNo = TagDataController.nameTransToTno(tag_name, this);
                            binding.tagInput.setText(tag_name);
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
        } else if (String.valueOf(binding.exportAccountInput.getText()).isEmpty() && type == AccountType.TRANSFER) {
            errLayout = binding.exportAccountLayout;
            err = "转出账户不能为空";
        } else if (String.valueOf(binding.importAccountInput.getText()).isEmpty() && type == AccountType.TRANSFER) {
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

        dataBundle.putString(KeyStrings.ANALYSIS_RULE_NAME.v(), ruleName);
        dataBundle.putString(KeyStrings.ACCOUNT_TYPE.v(), type.toString());
        dataBundle.putLong(KeyStrings.TAG_ID.v(), tagNo);
        dataBundle.putString(KeyStrings.PACKAGE_NAME.v(), packageName);
        dataBundle.putString(KeyStrings.NOTIFICATION_TITLE.v(), notificationTitle);
        dataBundle.putString(KeyStrings.NOTIFICATION_CONTENT.v(), notificationContent);

        //写入转账相关的数据
        if (type == AccountType.TRANSFER) {
            String exportAccount = String.valueOf(binding.exportAccountInput.getText());
            String importAccount = String.valueOf(binding.importAccountInput.getText());
            dataBundle.putString(KeyStrings.ACCOUNT_EXPORT.v(), exportAccount);
            dataBundle.putString(KeyStrings.ACCOUNT_IMPORT.v(), importAccount);
        }

        if (isModifyMode) {
            dataBundle.putInt(KeyStrings.VIEW_HOLDER_POSITION.v(), viewHolderPosition);
            dataBundle.putLong(KeyStrings.ANALYSIS_RULE_NO.v(), ruleNo);
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

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.view_markdown_text, null);
        MaterialTextView textView = dialogView.findViewById(R.id.md_textview_in_dialog);

        //使用Markown渲染Markdown文本
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(textView, instruction);

        new MaterialAlertDialogBuilder(this)
                .setTitle("输入说明")
                .setView(dialogView)
                .setNegativeButton("关闭", null)
                .show();
    }
}