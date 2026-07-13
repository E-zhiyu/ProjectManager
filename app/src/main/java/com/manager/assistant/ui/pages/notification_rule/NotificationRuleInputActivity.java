package com.manager.assistant.ui.pages.notification_rule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.manager.assistant.R;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleTransferEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.services.RuleService;
import com.manager.assistant.databinding.ActivityRuleInputBinding;
import com.manager.assistant.generic_enums.TagStrings;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.helpers.appearence.VisibilityHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.bottom.TagSelectBottomSheet;
import com.manager.assistant.ui.others.viewmodel.TagMultiSelectViewModel;
import com.manager.assistant.ui.pages.main.bookkeeping.AccountTagAdapter;
import com.manager.assistant.ui.pages.package_name_select.PackageNameSelectActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotificationRuleInputActivity extends AppCompatActivity {
    @Nullable
    private Bundle initBundle = null;                               //存有初始数据数据包
    private AccountType type = AccountType.EXPENSE;                 //流水种类
    private ActivityResultLauncher<Intent> packageNameSelectLauncher;   //包名选择启动器
    private ActivityRuleInputBinding binding;                       //绑定的XML视图引用
    private final CompositeDisposable disposable = new CompositeDisposable();
    private AccountTagAdapter tagAdapter;                           //标签适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRuleInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);

            //滚动视图的内部布局
            binding.scrollLayout.setPadding(
                    AppearanceHelper.dpToPx(this, 10),
                    AppearanceHelper.dpToPx(this, 10),
                    AppearanceHelper.dpToPx(this, 10),
                    AppearanceHelper.dpToPx(this, 10) + Math.max(ime.bottom, systemBars.bottom)
            );

            return insets;
        });

        initBundle = getIntent().getExtras();
        initViews();
        observeLiveData();
        initLaunchers();
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
        //标签 Recycler
        tagAdapter = new AccountTagAdapter(
                (entity, anchor, adapter) -> {
                    //切换视图可见性
                    List<TagEntity> removedList = new ArrayList<>(adapter.getCurrentList());
                    removedList.remove(entity);
                    if (!removedList.isEmpty()) {
                        tagAdapter.submitList(
                                removedList,
                                () -> VisibilityHelper.toggleViewExpansion(
                                        binding.scrollLayout,
                                        true,
                                        null,
                                        binding.tagRecycler
                                )
                        );
                    } else {
                        VisibilityHelper.toggleViewExpansion(
                                binding.scrollLayout,
                                false,
                                () -> tagAdapter.submitList(removedList),
                                binding.tagRecycler
                        );
                    }

                    //移除ViewModel集合中的数据
                    TagMultiSelectViewModel viewModel = new ViewModelProvider(this).get(TagMultiSelectViewModel.class);
                    viewModel.getCheckedTagIdSet().remove(entity.getTagId());
                }
        );
        binding.tagRecycler.setAdapter(tagAdapter);

        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_rule);

            long ruleId = initBundle.getLong(KeyStrings.NOTIFICATION_RULE_ID.v());

        }

        //名称
        binding.nameInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.nameLayout.setError(null);
            } else {
                String input = String.valueOf(binding.nameInput.getText()).trim();
                if (input.isEmpty()) {
                    binding.nameLayout.setError("名称不能为空");
                }
            }
        });

        //种类
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
                    boolean transferVisible = position == AccountType.TRANSFER.ordinal();
                    VisibilityHelper.toggleViewExpansion(
                            binding.scrollLayout,
                            transferVisible,
                            null,
                            binding.exportAccountLayout,
                            binding.importAccountLayout
                    );

                    type = AccountType.values()[position];
                }
        );

        //转出和转入账户的自动填充适配器
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(db.accountDao().getTransferAccountsSingle()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        transferAccountList -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    this,
                                    R.layout.exposed_dropdown_popup_item,
                                    transferAccountList
                            );

                            binding.exportAccountInput.setAdapter(adapter);
                            binding.importAccountInput.setAdapter(adapter);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );

        //转出账户
        binding.exportAccountInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.exportAccountLayout.setError(null);
            } else {
                String input = String.valueOf(binding.exportAccountInput.getText()).trim();
                if (type == AccountType.TRANSFER && input.isEmpty()) {
                    binding.exportAccountLayout.setError("转出账户不能为空");
                }
            }
        });

        //转入账户
        binding.importAccountInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.importAccountLayout.setError(null);
            } else {
                String input = String.valueOf(binding.importAccountInput.getText()).trim();
                if (type == AccountType.TRANSFER && input.isEmpty()) {
                    binding.importAccountLayout.setError("转出账户不能为空");
                }
            }
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

        //通知标题
        binding.notificationTitleInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.notificationTitleLayout.setError(null);
            } else {
                String input = String.valueOf(binding.notificationTitleInput.getText()).trim();
                if (input.isEmpty()) {
                    binding.notificationTitleLayout.setError("通知标题不能为空");
                }
            }
        });

        //内容正则表达式
        binding.regexInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.regexLayout.setError(null);
            } else {
                String input = String.valueOf(binding.regexInput.getText()).trim();
                if (input.isEmpty()) {
                    binding.regexLayout.setError("内容正则表达式不能为空");
                }
            }
        });

        binding.captureGroupPositionInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.captureGroupPositionLayout.setError(null);
            } else {
                String input = String.valueOf(binding.regexInput.getText()).trim();
                if (input.isEmpty()) {
                    binding.captureGroupPositionLayout.setError("捕获组位置必须为非负数");
                }
            }
        });

        //标签选择按钮
        binding.tagSelectBtn.setOnClickListener(view -> {
            TagSelectBottomSheet bottomSheet = new TagSelectBottomSheet();
            Bundle bundle = new Bundle();
            bundle.putInt(KeyStrings.TAG_SCOPE.v(), (int) Math.pow(2, type.ordinal())); //传递标签作用域标识符
            bottomSheet.setArguments(bundle);
            bottomSheet.show(getSupportFragmentManager(), TagStrings.TAG_SELECT_SHEET.getTag());
        });

        //确认按钮
        binding.confirmButton.setOnClickListener(view -> {
            String err = verifyInput();
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                return;
            }

            saveData();
        });
    }

    /**
     * 观察 ViewModel 的 LiveData
     */
    private void observeLiveData() {
        //标签选择
        TagMultiSelectViewModel tagMultiSelectViewModel = new ViewModelProvider(this).get(TagMultiSelectViewModel.class);
        tagMultiSelectViewModel.getNeedExecute().observe(this, b -> {
            if (b) {
                BookkeepingDb db = BookkeepingDb.getInstance(this);
                disposable.add(db.tagDao().getTagSingleById(tagMultiSelectViewModel.getCheckedTagIdSet())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                tagList -> {
                                    if (!tagList.isEmpty()) {
                                        tagAdapter.submitList(
                                                tagList,
                                                () -> VisibilityHelper.toggleViewExpansion(
                                                        binding.scrollLayout,
                                                        true,
                                                        null,
                                                        binding.tagRecycler
                                                )
                                        );
                                    } else {
                                        VisibilityHelper.toggleViewExpansion(
                                                binding.scrollLayout,
                                                false,
                                                () -> tagAdapter.submitList(tagList),
                                                binding.tagRecycler
                                        );
                                    }
                                },
                                e -> ExceptionHelper.showExceptionDialog(this, e)
                        )
                );
            }
        });
    }

    /**
     * 初始化启动器
     */
    private void initLaunchers() {
        packageNameSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            String packageName = data.getStringExtra(KeyStrings.PACKAGE_NAME.v());
                            binding.packageNameInput.setText(packageName);
                            binding.packageNameLayout.setError(null);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                    }
                }
        );
    }

    /**
     * 检测输入内容的有效性
     *
     * @return 错误提示字符串（无错误为null）
     */
    @Nullable
    private String verifyInput() {
        String err = null;
        String name = String.valueOf(binding.nameInput.getText()).trim();
        String packageName = String.valueOf(binding.packageNameInput.getText()).trim();
        String notificationTitle = String.valueOf(binding.notificationTitleInput.getText()).trim();
        String contentRegexStr = String.valueOf(binding.regexInput.getText()).trim();
        String matchGroupPositionStr = String.valueOf(binding.captureGroupPositionInput.getText()).trim();

        if (name.isEmpty()) {
            err = "名称不能为空";
            binding.nameLayout.setError(err);
        } else if (packageName.isEmpty()) {
            err = "包名不能为空";
            binding.packageNameLayout.setError(err);
        } else if (notificationTitle.isEmpty()) {
            err = "通知标题不能为空";
            binding.notificationTitleLayout.setError(err);
        } else if (contentRegexStr.isEmpty()) {
            err = "内容正则表达式不能为空";
            binding.regexLayout.setError(err);
        } else {
            try {
                Pattern pattern = Pattern.compile(contentRegexStr);
                int groupPosition = Integer.parseInt(matchGroupPositionStr);
                int groupCount = pattern.matcher("").groupCount();
                if (groupPosition <= 0) {
                    err = "捕获组位置必须为非负数";
                    binding.captureGroupPositionLayout.setError(err);
                    return err;
                } else if (groupPosition > groupCount) {
                    err = "捕获组位置不能超出捕获组总数";
                    binding.captureGroupPositionLayout.setError(err);
                    return err;
                }
            } catch (PatternSyntaxException e) {
                err = "内容正则表达式存在语法错误";
                binding.regexLayout.setError(err);
            }
        }

        return err;
    }

    /**
     * 将数据保存到数据库
     */
    private void saveData() {
        //获取输入内容
        String name = String.valueOf(binding.nameInput.getText()).trim();
        String packageName = String.valueOf(binding.packageNameInput.getText()).trim();
        String targetTitle = String.valueOf(binding.notificationTitleInput.getText()).trim();
        String contentRegexStr = String.valueOf(binding.regexInput.getText()).trim();
        String captureGroupPosStr = String.valueOf(binding.captureGroupPositionInput.getText()).trim();
        int captureGroupPos;
        try {
            captureGroupPos = Integer.parseInt(captureGroupPosStr);
        } catch (NumberFormatException e) {
            captureGroupPos = 1;
        }
        int typeOrdinal = this.type.ordinal();
        String exportAccount = String.valueOf(binding.exportAccountInput.getText()).trim();
        String importAccount = String.valueOf(binding.importAccountInput.getText()).trim();

        //生成标签 ID 列表
        List<Long> tagIdList = tagAdapter.getCurrentList().stream()
                .map(TagEntity::getTagId)
                .collect(Collectors.toList());

        //保存数据
        NotificationRuleEntity rule = new NotificationRuleEntity(
                name,
                typeOrdinal,
                packageName,
                targetTitle,
                contentRegexStr,
                captureGroupPos
        );
        NotificationRuleTransferEntity ruleTransfer = new NotificationRuleTransferEntity(exportAccount, importAccount);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        if (initBundle == null) {
            disposable.add(RuleService.addNewNotificationRule(rule, ruleTransfer, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> Toast.makeText(this, "规则添加成功", Toast.LENGTH_SHORT).show(),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            long ruleId = initBundle.getLong(KeyStrings.NOTIFICATION_RULE_ID.v());
            rule.setRuleId(ruleId);
            disposable.add(RuleService.modifyNotificationRule(rule, ruleTransfer, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> Toast.makeText(this, "规则修改成功", Toast.LENGTH_SHORT).show(),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
    }
}