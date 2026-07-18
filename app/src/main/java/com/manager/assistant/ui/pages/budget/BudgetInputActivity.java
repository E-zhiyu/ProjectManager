package com.manager.assistant.ui.pages.budget;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.manager.assistant.R;
import com.manager.assistant.auxiliary.classes.CustomDateTimeFormatter;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.BudgetEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.composite.BudgetWithDetailModel;
import com.manager.assistant.data.save.db.services.BudgetService;
import com.manager.assistant.databinding.ActivityBudgetInputBinding;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.generic_enums.TagStrings;
import com.manager.assistant.helpers.time.DateTimePickerHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.ImmHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.helpers.appearence.VisibilityHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.bottom.TagSelectBottomSheet;
import com.manager.assistant.ui.others.viewmodel.TagMultiSelectViewModel;
import com.manager.assistant.ui.pages.main.bookkeeping.AccountTagAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BudgetInputActivity extends AppCompatActivity {
    private ActivityBudgetInputBinding binding;                     //绑定的XML视图
    private ResetFrequency resetFrequency = ResetFrequency.FOREVER; //预算重置频率
    private final CompositeDisposable disposable = new CompositeDisposable();
    @Nullable
    private Bundle initBundle = null;                               //装有初始数据的数据包
    private AccountTagAdapter tagAdapter;                           //标签适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetInputBinding.inflate(getLayoutInflater());
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
    public void onDestroy() {
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

        //标题栏
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_budget);
            long budgetId = initBundle.getLong(KeyStrings.BUDGET_ID.v());

            //显示余额输入框
            binding.leftAmountLayout.setVisibility(View.VISIBLE);

            //加载数据
            BookkeepingDb db = BookkeepingDb.getInstance(this);
            disposable.add(db.budgetDao().getBudgetWithDetailById(budgetId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            optional -> {
                                if (optional.isEmpty()) return;

                                BudgetWithDetailModel model = optional.get();
                                BudgetEntity budget = model.getBudget();
                                List<TagEntity> tagList = model.getTagList();

                                //初始化输入框
                                binding.budgetNameInput.setText(budget.getName());                          //名称
                                binding.initAmountInput.setText(String.valueOf(budget.getInitAmount()));    //初始金额
                                binding.leftAmountInput.setText(String.valueOf(budget.getLeftAmount()));    //余额
                                binding.startDateInput.setText(                                             //起算日期
                                        budget.getStartDate().format(CustomDateTimeFormatter.DATE)
                                );
                                resetFrequency = ResetFrequency.values()[budget.getResetFrequency()];
                                binding.resetFrequencyInput.setText(resetFrequency.getTitle());             //重置频率

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
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }

        //预算名称
        binding.budgetNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.budgetNameLayout.setError(null);
            } else {
                String input = String.valueOf(binding.budgetNameInput.getText()).trim();
                if (input.isEmpty()) {
                    binding.budgetNameLayout.setError("预算名称不能为空");
                }
            }
        });
        ImmHelper.showImm(binding.budgetNameInput);

        //初始金额
        binding.initAmountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.initAmountLayout.setError(null);
            } else {
                String input = String.valueOf(binding.initAmountInput.getText()).trim();
                if (input.isEmpty()) {
                    binding.initAmountLayout.setError("初始金额不能为空");
                } else if (Double.parseDouble(input) == 0) {
                    binding.initAmountLayout.setError("初始金额不能为0");
                }
            }
        });

        //日期和时间
        binding.startDateInput.setText(LocalDate.now().format(CustomDateTimeFormatter.DATE));
        binding.startDateInput.setOnClickListener(v -> showMaterialDatePicker());
        binding.startDateInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showMaterialDatePicker();
                binding.startDateLayout.setError(null);
            }
        });

        //重置频率
        binding.resetFrequencyInput.setText(resetFrequency.getTitle());
        String[] frequencyTitles = Arrays.stream(ResetFrequency.values())
                .map(ResetFrequency::getTitle)
                .toArray(String[]::new);
        NoFilteringArrayAdapter<String> arrayAdapter = new NoFilteringArrayAdapter<>(this, frequencyTitles);
        binding.resetFrequencyInput.setAdapter(arrayAdapter);
        binding.resetFrequencyInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    resetFrequency = ResetFrequency.values()[position];
                    refreshFrequencyHelpText();
                }
        );

        //标签选择按钮
        binding.tagSelectBtn.setOnClickListener(view -> {
            TagSelectBottomSheet bottomSheet = new TagSelectBottomSheet();
            Bundle bundle = new Bundle();
            bundle.putInt(KeyStrings.TAG_SCOPE.v(), 0); //传递标签作用域标识符
            bottomSheet.setArguments(bundle);
            bottomSheet.show(getSupportFragmentManager(), TagStrings.TAG_SELECT_BOTTOM.getTag());
        });

        //完成按钮
        binding.confirmButton.setOnClickListener(v -> {
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
     * 刷新重置频率输入框的 help 文本
     */
    private void refreshFrequencyHelpText() {
        //获取上一次重置日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lastResetDate = LocalDate.parse(
                String.valueOf(binding.startDateInput.getText()),
                formatter
        );

        //计算下一次重置日期
        LocalDate nextResetDate = null;
        switch (resetFrequency) {
            case EVERY_DAY:
                nextResetDate = lastResetDate.plusDays(1);
                break;
            case EVERY_WEEK:
                nextResetDate = lastResetDate.plusDays(7);
                break;
            case EVERY_MONTH:
                nextResetDate = lastResetDate.plusMonths(1).withDayOfMonth(1);
                break;
            default:
                binding.resetFrequencyLayout.setHelperText(
                        ContextCompat.getString(this, R.string.reset_frequency_help)
                );
                break;
        }

        //更新重置频率输入框布局的 help 文本
        if (nextResetDate != null) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String helpText = String.format(
                    Locale.getDefault(),
                    "预算将在%s重置",
                    nextResetDate.atStartOfDay().format(timeFormatter)
            );
            binding.resetFrequencyLayout.setHelperText(helpText);
        }
    }

    /**
     * 弹出日期和时间选择框
     */
    private void showMaterialDatePicker() {
        //解析已输入的日期
        String datetimeStr = String.valueOf(binding.startDateInput.getText()).trim();
        LocalDate inputDate = datetimeStr.isEmpty() ?
                LocalDate.now() :
                LocalDate.parse(datetimeStr, CustomDateTimeFormatter.DATE);

        //显示日期选择对话框
        DateTimePickerHelper.selectDate(
                inputDate,
                getSupportFragmentManager(),
                selection -> {
                    //时间戳转换为LocalDateTime
                    LocalDate selectedDatetime = DateTimePickerHelper.getLocalDateFromTimeMilli(selection);
                    binding.startDateInput.setText(CustomDateTimeFormatter.DATE.format(selectedDatetime));
                }
        );
    }

    /**
     * 校验输入内容
     *
     * @return 错误提示(没有错误则为null)
     */
    @Nullable
    private String verifyInput() {
        String err = null;
        String name = String.valueOf(binding.budgetNameInput.getText()).trim();
        String initAmountStr = String.valueOf(binding.initAmountInput.getText()).trim();
        String leftAmountStr = String.valueOf(binding.leftAmountInput.getText()).trim();


        //提取部分输入数据
        double initAmount, leftAmount;
        try {
            initAmount = Double.parseDouble(initAmountStr);
        } catch (NumberFormatException e) {
            initAmount = 0;
        }
        try {
            leftAmount = Double.parseDouble(leftAmountStr);
        } catch (NumberFormatException e) {
            leftAmount = 0;
        }

        //进行校验
        if (name.isEmpty()) {
            err = "预算名称不能为空";
            binding.budgetNameLayout.setError(err);
        } else if (initAmountStr.isEmpty()) {
            err = "初始金额不能为空";
            binding.initAmountLayout.setError(err);
        } else if (initAmount == 0) {
            err = "初始金额不能为0";
            binding.initAmountLayout.setError(err);
        } else if (leftAmountStr.isEmpty() && initBundle != null) {
            err = "剩余金额不能为空";
            binding.leftAmountLayout.setError(err);
        } else if (initBundle != null && leftAmount > initAmount) {
            err = "剩余金额不能超过初始金额";
            binding.leftAmountLayout.setError(err);
        } else if (binding.tagRecycler.getAdapter() != null && binding.tagRecycler.getAdapter().getItemCount() == 0) {
            err = "请选择至少一个标签";
        }

        return err;
    }

    /**
     * 保存数据
     */
    private void saveData() {
        //获取输入内容
        String name = String.valueOf(binding.budgetNameInput.getText()).trim();
        double initAmount, leftAmount;
        try {
            initAmount = Double.parseDouble(String.valueOf(binding.initAmountInput.getText()).trim());
        } catch (NumberFormatException e) {
            initAmount = 0;
        }
        try {
            leftAmount = Double.parseDouble(String.valueOf(binding.leftAmountInput.getText()).trim());
        } catch (NumberFormatException e) {
            leftAmount = 0;
        }
        LocalDate startDate = LocalDate.parse(String.valueOf(binding.startDateInput.getText()).trim(), CustomDateTimeFormatter.DATE);

        //生成标签 ID 列表
        List<Long> tagIdList = tagAdapter.getCurrentList().stream()
                .map(TagEntity::getTagId)
                .collect(Collectors.toList());

        BookkeepingDb db = BookkeepingDb.getInstance(this);
        BudgetEntity budget = new BudgetEntity(name, initAmount, leftAmount, startDate, resetFrequency.ordinal());
        if (initBundle == null) {
            disposable.add(BudgetService.addBudget(budget, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "预算添加成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        } else {
            long budgetId = initBundle.getLong(KeyStrings.BUDGET_ID.v());
            budget.setBudgetId(budgetId);
            disposable.add(BudgetService.modifyBudget(budget, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "预算修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        }
    }
}