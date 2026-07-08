package com.manager.assistant.ui.pages.main.bookkeeping;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.manager.assistant.R;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.services.AccountService;
import com.manager.assistant.databinding.ActivityRunningAccountInputBinding;
import com.manager.assistant.generic_enums.DirectoryPaths;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.helpers.BackPressedCallbackHelper;
import com.manager.assistant.helpers.DateTimePickerHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.appearence.VisibilityHelper;
import com.manager.assistant.helpers.file.FileHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.dialogs.ProgressDialogBuilder;
import com.manager.assistant.ui.others.selections.media.MediaIdKeyProvider;
import com.manager.assistant.ui.others.selections.media.MediaLookup;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RunningAccountInputActivity extends AppCompatActivity {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private AccountType type = AccountType.EXPENSE;         //流水种类
    private ActivityRunningAccountInputBinding binding;     //绑定的XML视图引用
    private Bundle initBundle = null;                       //传递初始数据的数据包
    private final CompositeDisposable disposable = new CompositeDisposable();
    private SelectionTracker<Long> selectionTracker;        //图片列表选择追踪器
    private BackPressedCallbackHelper backHelper;           //返回逻辑管理器
    private BackPressedCallbackHelper.BackHandler selectionBackHandler; //媒体多选返回处理器
    private AccountMediaAdapter mediaAdapter;               //媒体适配器
    private AccountTagAdapter tagAdapter;                   //标签适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRunningAccountInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        initBundle = getIntent().getExtras();
        initViews();
        initOnBackPressedHandlers();
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
                    List<TagEntity> removedList = new ArrayList<>(adapter.getCurrentList());
                    removedList.remove(entity);
                    if (removedList.isEmpty()) {
                        VisibilityHelper.toggleViewExpansion(
                                binding.getRoot(),
                                false,
                                () -> adapter.submitList(removedList),
                                binding.tagRecycler
                        );
                    } else {
                        adapter.submitList(removedList);
                    }
                }
        );
        binding.tagRecycler.setAdapter(tagAdapter);

        //媒体 Recycler
        int spanCount = 3;  //显示3列
        int size = binding.getRoot().getWidth() / spanCount;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        binding.mediaRecycler.setLayoutManager(layoutManager);
        mediaAdapter = new AccountMediaAdapter(
                size,
                (entity, anchor) -> {
                    //TODO:显示大图
                }
        );
        binding.mediaRecycler.setAdapter(mediaAdapter);
        selectionTracker = new SelectionTracker.Builder<>(
                TagString.MEDIA_SELECTION.getTag(),
                binding.mediaRecycler,
                new MediaIdKeyProvider(mediaAdapter),
                new MediaLookup(binding.mediaRecycler),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()      //允许多选
        ).build();
        mediaAdapter.setSelectionTracker(selectionTracker);
        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<>() {
            @Override
            public void onSelectionChanged() {  //追踪选择状态
                super.onSelectionChanged();
                if (selectionTracker.hasSelection()) {
                    new Handler(Looper.getMainLooper()).post(   //必须主线程更新 UI
                            () -> setSelectMode(true)
                    );

                    int size = selectionTracker.getSelection().size();
                    Log.d(LogTags.ACCOUNT_INPUT.n(), "已选择：" + size);
                } else {
                    new Handler(Looper.getMainLooper()).post(   //必须主线程更新 UI
                            () -> setSelectMode(false)
                    );
                    Log.d(LogTags.ACCOUNT_INPUT.n(), "选择已清除");
                }
            }
        });

        //工具栏
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (initBundle != null) {
            binding.toolbar.setTitle(R.string.modify_running_account);

            //读取初始数据
            long accountId = initBundle.getLong(KeyStrings.ACCOUNT_ID.v());
            BookkeepingDb db = BookkeepingDb.getInstance(this);
            disposable.add(db.accountDao().getAccountWithTagAndMediaSingleById(accountId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            modelOptional -> {
                                if (modelOptional.isEmpty()) return;

                                //获取数据
                                AccountEntity account = modelOptional.get().getAccount();
                                List<TagEntity> tagList = modelOptional.get().getTagList();
                                List<MediaEntity> mediaList = modelOptional.get().getMediaList();

                                //显示文本框数据
                                binding.amountInput.setText(String.valueOf(account.getAmount()));   //金额
                                binding.remarkInput.setText(account.getRemark());                   //备注
                                type = AccountType.values()[account.getType()];
                                binding.typeInput.setText(type.getTitle());                         //种类
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                binding.datetimeInput.setText(account.getDateTime().format(formatter)); //日期和时间

                                //显示标签
                                if (!tagList.isEmpty()) {
                                    binding.tagRecycler.setVisibility(View.VISIBLE);
                                    tagAdapter.submitList(tagList);
                                } else {
                                    binding.tagRecycler.setVisibility(View.GONE);
                                }

                                //显示媒体
                                if (!mediaList.isEmpty()) {
                                    binding.mediaRecycler.setVisibility(View.VISIBLE);
                                    mediaAdapter.submitList(mediaList);
                                } else {
                                    binding.mediaRecycler.setVisibility(View.GONE);
                                }
                            }
                    )
            );
        }

        //金额
        binding.amountInput.setOnFocusChangeListener((view, b) -> {
            if (b) {
                binding.amountLayout.setError(null);
            } else {
                String input = String.valueOf(binding.amountInput.getText());
                if (input.trim().isEmpty()) {
                    binding.amountLayout.setError("金额不能为空");
                } else if (Double.parseDouble(input) == 0.0) {
                    binding.amountLayout.setError("金额不能为0");
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
                            binding.transferInputLayout
                    );

                    type = AccountType.values()[position];
                }
        );

        //日期和时间
        binding.datetimeInput.setText(LocalDateTime.now().format(FORMATTER));
        binding.datetimeInput.setOnClickListener(v -> showMaterialDatePicker());
        binding.datetimeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showMaterialDatePicker();
                binding.datetimeLayout.setError(null);
            }
        });

        //完成按钮
        binding.confirmButton.setOnClickListener(v -> {
            String err = verifyInput();
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
                return;
            }

            //显示进度条对话框
            ProgressDialogBuilder dialogBuilder = new ProgressDialogBuilder(
                    this,
                    "保存媒体文件",
                    "正在移动媒体文件……"
            );
            AlertDialog dialog = dialogBuilder
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                        disposable.clear();
                        Toast.makeText(this, "已取消媒体文件保存", Toast.LENGTH_SHORT).show();
                    })
                    .create();


            //如果有新媒体，则显示对话框
            List<MediaEntity> currentMediaList = mediaAdapter.getCurrentList();
            if (!currentMediaList.isEmpty()) {
                dialog.show();
            }

            //创建移动任务，并逐个返回移动成功的 File 型 Uri
            Observable<MediaEntity> moveTask = Observable.create(emitter -> {
                File targetDir = DirectoryPaths.MEDIA.getDir(this);

                for (MediaEntity originMedia : currentMediaList) {
                    long originMediaId = originMedia.getMediaId();
                    if (originMediaId == 0) {
                        File originFile = new File(Objects.requireNonNull(originMedia.getFileUri().getPath()));
                        File movedFile = FileHelper.moveFile(originFile, targetDir);
                        emitter.onNext(new MediaEntity(Uri.fromFile(movedFile), originMediaId));    //不论是否成功都返回
                    } else {
                        emitter.onNext(originMedia);
                    }
                }

                emitter.onComplete();
            });

            //多线程执行任务并调用段落添加/更新方法
            List<MediaEntity> resultList = new ArrayList<>();   //移动结果列表（可能包含 null）
            disposable.add(moveTask
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            mediaEntity -> {
                                resultList.add(mediaEntity);
                                dialogBuilder.updateProgress(resultList.size(), currentMediaList.size(), "正在移动媒体文件……");
                            },
                            e -> {
                                ExceptionHelper.showExceptionDialog(this, e);
                                dialog.dismiss();
                            },
                            () -> {
                                dialog.dismiss();

                                //排除移动失败的文件
                                List<MediaEntity> succeedFileUriList = resultList.stream()
                                        .filter(mediaEntity -> mediaEntity.getFileUri() != null)
                                        .collect(Collectors.toList());

                                //调用数据写入方法
                                saveData(succeedFileUriList);
                            }
                    )
            );
        });
    }

    /**
     * 初始化返回拦截逻辑
     */
    private void initOnBackPressedHandlers() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                backHelper.dispatchBackPressed();
            }
        };
        backHelper = new BackPressedCallbackHelper(backPressedCallback);
        getOnBackPressedDispatcher().addCallback(backPressedCallback);

        //媒体多选处理器
        selectionBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                selectionTracker.clearSelection();

                backHelper.unregisterHandler(this);
                return true;
            }

            @Override
            public int getPriority() {
                return 3;
            }
        };
    }

    /**
     * 校验输入内容合法性
     *
     * @return 错误提示，若无错误返回 null
     */
    private String verifyInput() {
        String err = null;
        String amount = String.valueOf(binding.amountInput.getText());
        String dateTimeStr = String.valueOf(binding.datetimeInput.getText());

        if (amount.isEmpty()) {
            err = "金额不能为空";
            binding.amountLayout.setError(err);
        } else if (Double.parseDouble(amount) == 0.0) {
            err = "金额不能为0";
            binding.amountLayout.setError(err);
        } else if (dateTimeStr.isEmpty()) {
            err = "日期和时间不能为空";
            binding.datetimeLayout.setError(err);
        }

        return err;
    }

    /**
     * 将数据保存到数据库
     */
    private void saveData(List<MediaEntity> copiedMediaUriList) {
        //获取输入的数据
        double amount = Double.parseDouble(String.valueOf(binding.amountInput.getText()));
        String remark = String.valueOf(binding.remarkInput.getText());
        LocalDateTime dateTime = LocalDateTime.parse(
                String.valueOf(binding.datetimeInput.getText()),
                FORMATTER
        );
        int typeOrdinal = this.type.ordinal();

        //生成标签 ID 列表
        List<Long> tagIdList = tagAdapter.getCurrentList().stream()
                .map(TagEntity::getTagId)
                .collect(Collectors.toList());

        AccountEntity account = new AccountEntity(amount, remark, typeOrdinal, dateTime);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        if (initBundle == null) {
            disposable.add(AccountService.addNewAccount(account, copiedMediaUriList, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "流水记录添加成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> {
                                for (MediaEntity mediaEntity : copiedMediaUriList) {
                                    //删除新添加的媒体文件
                                    if (mediaEntity.getMediaId() == 0) {
                                        FileHelper.deleteFile(mediaEntity.getFileUri(), this);
                                    }
                                }
                                ExceptionHelper.showExceptionDialog(this, e);
                            }
                    )
            );
        } else {
            long accountId = initBundle.getLong(KeyStrings.ACCOUNT_ID.v());
            account.setAccountId(accountId);
            disposable.add(AccountService.modifyAccount(account, copiedMediaUriList, tagIdList, db)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> {
                                Toast.makeText(this, "流水记录修改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> {
                                for (MediaEntity mediaEntity : copiedMediaUriList) {
                                    //删除新添加的媒体文件
                                    if (mediaEntity.getMediaId() == 0) {
                                        FileHelper.deleteFile(mediaEntity.getFileUri(), this);
                                    }
                                }
                                ExceptionHelper.showExceptionDialog(this, e);
                            }
                    )
            );
        }
    }

    /**
     * 弹出日期和时间选择框
     */
    private void showMaterialDatePicker() {
        //解析已输入的时间
        String datetimeStr = String.valueOf(binding.datetimeInput.getText());
        LocalDateTime inputDatetime = datetimeStr.isEmpty() ?
                LocalDateTime.now() :
                LocalDateTime.parse(datetimeStr, FORMATTER);
        LocalDate date = inputDatetime.toLocalDate();

        //显示日期选择对话框
        DateTimePickerHelper.selectDate(
                date,
                getSupportFragmentManager(),
                selection -> {
                    //时间戳转换为LocalDateTime
                    LocalDateTime selectedDatetime = DateTimePickerHelper.getLocalDateTimeFromTimeMilli(selection);

                    //选择日期后，再弹出时间选择器
                    showMaterialTimePicker(selectedDatetime, inputDatetime);
                }
        );
    }

    /**
     * 显示时间选择对话框
     *
     * @param selectedDatetime 日期选择对话框选择的日期
     * @param initialDatetime  初始化日期
     */
    private void showMaterialTimePicker(@NonNull LocalDateTime selectedDatetime, @NonNull LocalDateTime initialDatetime) {
        DateTimePickerHelper.selectDateTime(
                initialDatetime.toLocalTime(),
                getSupportFragmentManager(),
                timePicker -> {
                    //组合日期和时间
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    LocalDateTime finalDatetime = selectedDatetime.withHour(hour).withMinute(minute);

                    //修改文本框的日期和时间
                    binding.datetimeInput.setText(FORMATTER.format(finalDatetime));
                }
        );
    }

    /**
     * 设置媒体多选模式是否启用
     *
     * @param isSelectMode 是否在媒体多选模式
     */
    private void setSelectMode(boolean isSelectMode) {
        if (isSelectMode && binding.mediaDeleteBtn.getVisibility() == View.VISIBLE ||
                !isSelectMode && binding.mediaDeleteBtn.getVisibility() == View.GONE) {
            return;
        }

        //处理返回拦截
        if (isSelectMode) {
            backHelper.registerHandler(selectionBackHandler);
        } else {
            backHelper.unregisterHandler(selectionBackHandler);
        }

        //定义过渡动画
        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .addTarget(binding.mediaDeleteBtn)
                .addTarget(binding.mediaAddBtn)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(250);

        //通知布局即将发生变化
        TransitionManager.beginDelayedTransition(binding.mediaBtnLayout, set);

        //切换视图可见性
        if (isSelectMode) {
            binding.mediaDeleteBtn.setVisibility(View.VISIBLE);
            binding.mediaAddBtn.setVisibility(View.GONE);
        } else {
            binding.mediaDeleteBtn.setVisibility(View.GONE);
            binding.mediaAddBtn.setVisibility(View.VISIBLE);
        }

        if (binding.mediaRecycler.getAdapter() instanceof AccountMediaAdapter) {
            ((AccountMediaAdapter) binding.mediaRecycler.getAdapter()).setSelectMode(isSelectMode); //切换适配器选择模式
        }
    }
}