package com.sly.coffer.ui.pages.accessibility;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.sly.coffer.R;
import com.sly.coffer.auxiliary.classes.PickResult;
import com.sly.coffer.auxiliary.enums.AccountType;
import com.sly.coffer.auxiliary.enums.KeyStrings;
import com.sly.coffer.auxiliary.enums.TagStrings;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleEntity;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleTransferEntity;
import com.sly.coffer.data.save.db.entities.TagEntity;
import com.sly.coffer.data.save.db.entities.composite.AccessibilityRuleWithDetailModel;
import com.sly.coffer.data.save.db.services.AccessibilityRuleService;
import com.sly.coffer.data.save.preference.TipPreference;
import com.sly.coffer.databinding.ActivityAccessibilityRuleInputBinding;
import com.sly.coffer.helpers.ExceptionHelper;
import com.sly.coffer.helpers.ImmHelper;
import com.sly.coffer.helpers.appearence.AppearanceHelper;
import com.sly.coffer.helpers.appearence.VisibilityHelper;
import com.sly.coffer.ui.others.adapters.NoFilteringArrayAdapter;
import com.sly.coffer.ui.others.bottom.TagSelectBottomSheet;
import com.sly.coffer.ui.others.viewmodel.input.AccessibilityRuleInputViewModel;
import com.sly.coffer.ui.others.viewmodel.TagMultiSelectViewModel;
import com.sly.coffer.ui.pages.main.bookkeeping.AccountTagAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AccessibilityRuleInputActivity extends AppCompatActivity {
    private ActivityAccessibilityRuleInputBinding binding;
    @Nullable
    private Bundle initBundle = null;                               //存有初始数据数据包
    private final CompositeDisposable disposable = new CompositeDisposable();
    private AccountTagAdapter tagAdapter;                           //标签适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccessibilityRuleInputBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
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

        binding.toolbar.setNavigationOnClickListener(view -> finish());
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_rule);

            long ruleId = initBundle.getLong(KeyStrings.ACCESSIBILITY_RULE_ID.v());
            BookkeepingDb db = BookkeepingDb.getInstance(this);
            disposable.add(db.accessibilityRuleDao().getRuleWithDetailById(ruleId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            optional -> {
                                if (optional.isEmpty()) return;

                                //读取数据
                                AccessibilityRuleWithDetailModel model = optional.get();
                                AccessibilityRuleEntity rule = model.getRule();
                                AccessibilityRuleTransferEntity transfer = model.getTransfer();
                                List<TagEntity> tagList = model.getTagList();

                                //填充数据
                                AccessibilityRuleInputViewModel viewModel =
                                        new ViewModelProvider(this).get(AccessibilityRuleInputViewModel.class);
                                binding.nameInput.setText(rule.getName());                  //名称
                                viewModel.setType(AccountType.values()[rule.getType()]);
                                binding.typeInput.setText(viewModel.getType().getTitle());  //种类
                                if (viewModel.getPickResult() == null) {    //仅没有设置拾取时才填充
                                    String pkgStr = String.format(
                                            Locale.getDefault(),
                                            "%s : %s",
                                            getString(R.string.package_name),
                                            rule.getPackageName().isEmpty() ? "<未设置>" : rule.getPackageName()
                                    );
                                    binding.packageNameText.setText(pkgStr);                    //包名
                                    String activityStr = String.format(
                                            Locale.getDefault(),
                                            "%s : %s",
                                            getString(R.string.activity_name),
                                            rule.getTargetActivity().isEmpty() ? "<未设置>" : rule.getTargetActivity()
                                    );
                                    binding.activityNameText.setText(activityStr);              //活动名
                                    String viewIdStr = String.format(
                                            Locale.getDefault(),
                                            "%s : %s",
                                            getString(R.string.view_id),
                                            rule.getViewId().isEmpty() ? "<未设置>" : rule.getViewId()
                                    );
                                    binding.viewIdText.setText(viewIdStr);                      //视图 ID
                                    String originContent = String.format(
                                            Locale.getDefault(),
                                            "%s : %s",
                                            getString(R.string.str_content),
                                            rule.getOriginContent().isEmpty() ? "<未设置>" : rule.getOriginContent()
                                    );
                                    binding.pickContentText.setText(originContent);             //内容文本

                                    //填充拾取结果
                                    PickResult pickResult = new PickResult();
                                    pickResult.activityName = rule.getTargetActivity();
                                    pickResult.packageName = rule.getPackageName();
                                    pickResult.viewId = rule.getViewId();
                                    pickResult.content = rule.getOriginContent();
                                    viewModel.setPickResult(pickResult);

                                    viewModel.setCapturePos(rule.getCapturePos());
                                }
                                if (viewModel.getType() == AccountType.TRANSFER) {
                                    binding.exportAccountLayout.setVisibility(View.VISIBLE);
                                    binding.importAccountLayout.setVisibility(View.VISIBLE);

                                    binding.exportAccountInput.setText(transfer.getExportAccount());    //转出账户
                                    binding.importAccountInput.setText(transfer.getImportAccount());    //转入账户
                                }

                                //显示标签
                                if (!tagList.isEmpty()) {
                                    tagAdapter.submitList(tagList);
                                    binding.tagRecycler.setVisibility(View.VISIBLE);
                                } else {
                                    binding.tagRecycler.setVisibility(View.GONE);
                                }
                                List<Long> tagIdList = tagList.stream()
                                        .map(TagEntity::getTagId)
                                        .collect(Collectors.toList());
                                TagMultiSelectViewModel tagMultiSelectViewModel = new ViewModelProvider(this).get(TagMultiSelectViewModel.class);
                                tagMultiSelectViewModel.getCheckedTagIdSet().clear();
                                tagMultiSelectViewModel.getCheckedTagIdSet().addAll(tagIdList);

                                //显示金额选择文本
                                showAmountSelect();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }

        AccessibilityRuleInputViewModel viewModel =
                new ViewModelProvider(this).get(AccessibilityRuleInputViewModel.class);
        PickResult pickResult = viewModel.getPickResult();

        //拾取的视图
        String pkgStr = String.format(
                Locale.getDefault(),
                "%s : %s",
                getString(R.string.package_name),
                pickResult == null ? "<未设置>" : pickResult.packageName
        );
        binding.packageNameText.setText(pkgStr);
        String activityStr = String.format(
                Locale.getDefault(),
                "%s : %s",
                getString(R.string.activity_name),
                pickResult == null ? "<未设置>" : pickResult.activityName
        );
        binding.activityNameText.setText(activityStr);
        String viewIdStr = String.format(
                Locale.getDefault(),
                "%s : %s",
                getString(R.string.view_id),
                pickResult == null ? "<未设置>" : pickResult.viewId
        );
        binding.viewIdText.setText(viewIdStr);
        String content = String.format(
                Locale.getDefault(),
                "%s : %s",
                getString(R.string.str_content),
                pickResult == null ? "<未设置>" : pickResult.content
        );
        binding.pickContentText.setText(content);

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
        ImmHelper.showImm(binding.nameInput);

        //种类
        NoFilteringArrayAdapter<String> typeAdapter = new NoFilteringArrayAdapter<>(
                this,
                Arrays.stream(AccountType.values())
                        .map(AccountType::getTitle)
                        .toArray(String[]::new)
        );
        binding.typeInput.setText(viewModel.getType().getTitle());
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

                    viewModel.setType(AccountType.values()[position]);
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
                if (viewModel.getType() == AccountType.TRANSFER && input.isEmpty()) {
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
                if (viewModel.getType() == AccountType.TRANSFER && input.isEmpty()) {
                    binding.importAccountLayout.setError("转出账户不能为空");
                }
            }
        });

        //视图拾取按钮
        binding.amountViewSelectBtn.setOnClickListener(view -> {
            //TODO:开始拾取
        });

        //视图拾取说明按钮
        binding.amountViewExplainBtn.setOnClickListener(view -> {
            final String EXPLANATION = "作为金额填充到流水记录的文本的来源";
            TipPreference.showTipWithoutKey(view, Gravity.START, EXPLANATION);
        });

        //标签选择按钮
        binding.tagSelectBtn.setOnClickListener(view -> {
            TagSelectBottomSheet bottomSheet = new TagSelectBottomSheet();
            Bundle bundle = new Bundle();
            bundle.putInt(KeyStrings.TAG_SCOPE.v(), (int) Math.pow(2, viewModel.getType().ordinal()));  //传递标签作用域标识符
            bottomSheet.setArguments(bundle);
            bottomSheet.show(getSupportFragmentManager(), TagStrings.TAG_SELECT_BOTTOM.t());
        });

        //标签说明按钮
        binding.tagExplainBtn.setOnClickListener(view -> {
            final String EXPLANATION = "自动为生成的流水记录添加下列标签";
            TipPreference.showTipWithoutKey(view, Gravity.START, EXPLANATION);
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
     * 校验输入内容的有效性
     *
     * @return 错误提示，无错误则返回 null
     */
    private String verifyInput() {
        String err = null;
        String name = String.valueOf(binding.nameInput.getText()).trim();
        String exportAccount = String.valueOf(binding.exportAccountInput.getText()).trim();
        String importAccount = String.valueOf(binding.importAccountInput.getText()).trim();

        AccessibilityRuleInputViewModel viewModel = new ViewModelProvider(this).get(AccessibilityRuleInputViewModel.class);
        if (name.isEmpty()) {
            err = "名称不能为空";
            binding.nameLayout.setError(err);
        } else if (viewModel.getType() == AccountType.TRANSFER && exportAccount.isEmpty()) {
            err = "转出账户不能为空";
            binding.exportAccountLayout.setError(err);
        } else if (viewModel.getType() == AccountType.TRANSFER && importAccount.isEmpty()) {
            err = "转入账户不能为空";
            binding.importAccountLayout.setError(err);
        } else if (viewModel.getPickResult() == null) {
            err = "必须设置拾取视图";
        }

        return err;
    }

    /**
     * 保存数据
     */
    private void saveData() {
        String name = String.valueOf(binding.nameInput.getText()).trim();
        AccessibilityRuleInputViewModel viewModel = new ViewModelProvider(this).get(AccessibilityRuleInputViewModel.class);
        int typeOrdinal = viewModel.getType().ordinal();
        int capturePos = viewModel.getCapturePos();
        PickResult pickResult = viewModel.getPickResult();
        String packageName = pickResult.packageName;
        String activityName = pickResult.activityName;
        String viewId = pickResult.viewId;
        String originContent = pickResult.content;
        String exportAccount = String.valueOf(binding.exportAccountInput.getText()).trim();
        String importAccount = String.valueOf(binding.importAccountInput.getText()).trim();

        //生成标签 ID 列表
        List<Long> tagIdList = tagAdapter.getCurrentList().stream()
                .map(TagEntity::getTagId)
                .collect(Collectors.toList());

        //生成内容正则表达式
        final String REGEX = "\\d+\\.?\\d{0,2}";
        final String REPLACEMENT = "(\\\\d+\\\\.?\\\\d{0,2})";
        String contentRegex = originContent.replaceAll(REGEX, REPLACEMENT);

        //保存数据
        AccessibilityRuleEntity rule = new AccessibilityRuleEntity(
                name,
                typeOrdinal,
                packageName,
                activityName,
                viewId,
                originContent,
                contentRegex,
                capturePos
        );
        AccessibilityRuleTransferEntity transfer = new AccessibilityRuleTransferEntity(exportAccount, importAccount);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        if (initBundle == null) {
            disposable.add(AccessibilityRuleService.addNewAccessibilityRule(rule, transfer, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "规则添加成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            long ruleId = initBundle.getLong(KeyStrings.ACCESSIBILITY_RULE_ID.v());
            rule.setRuleId(ruleId);
            disposable.add(AccessibilityRuleService.modifyAccessibilityRule(rule, transfer, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "规则修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
    }

    /**
     * 显示金额选择视图
     */
    private void showAmountSelect() {
        AccessibilityRuleInputViewModel viewModel = new ViewModelProvider(this).get(AccessibilityRuleInputViewModel.class);
        int capturePos = viewModel.getCapturePos();
        PickResult pickResult = viewModel.getPickResult();

        //填充 Chip
        Pattern amountPattern = Pattern.compile("\\d+\\.?\\d{0,2}");
        Matcher matcher = amountPattern.matcher(pickResult.content);
        int i = 1;
        while (matcher.find()) {
            String amountText = matcher.group();

            int finalPosition = i;
            Chip amountChip = new Chip(this);
            amountChip.setCheckable(true);
            if (i == capturePos) {
                amountChip.setChecked(true);
            }
            amountChip.setText(amountText);
            amountChip.setCheckedIconVisible(true);
            amountChip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    viewModel.setCapturePos(finalPosition);
                }
            });
            binding.amountSelectChipGroup.addView(amountChip);

            i++;
        }

        VisibilityHelper.toggleViewExpansion(
                binding.scrollLayout,
                true,
                null,
                binding.amountSelectLayout
        );
    }
}