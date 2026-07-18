package com.manager.assistant.ui.pages.main.settings.sub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.automation.schedulers.BackupScheduler;
import com.manager.assistant.auxiliary.enums.RadiusStyle;
import com.manager.assistant.data.io.helpers.AnalysisRuleDataHelper;
import com.manager.assistant.data.io.helpers.BudgetDataHelper;
import com.manager.assistant.data.io.helpers.DataHelperBase;
import com.manager.assistant.data.io.helpers.RunningAccountDataHelper;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.preference.AutoBackupPreference;
import com.manager.assistant.data.save.preference.BookKeepingStartDatePreference;
import com.manager.assistant.databinding.ActivityDataManageBinding;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.generic_enums.options.BackupFrequency;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.helpers.file.AutoBackupHelper;
import com.manager.assistant.helpers.file.DataIOHelper;
import com.manager.assistant.helpers.file.UriPathHelper;
import com.manager.assistant.ui.pages.main.settings.components.SettingClickableTextView;
import com.manager.assistant.ui.pages.main.settings.components.SettingSpinnerView;
import com.manager.assistant.ui.pages.main.settings.components.SettingSwitchView;
import com.manager.assistant.ui.sync.account.AccountUpdateReason;
import com.manager.assistant.ui.sync.account.RunningAccountRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DataManageActivity extends AppCompatActivity {
    private ActivityDataManageBinding binding;  //绑定的XML布局
    private final CompositeDisposable disposables = new CompositeDisposable();      //多线程任务列表
    private AutoBackupHelper autoBackupHelper;                                      //自动备份帮助器
    private DataIOHelper dataIOHelper;                                              //SAF文件帮助器
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //活动启动器
    private ActivityResultLauncher<Intent> backupDirectorySetLauncher;              //自动备份文件夹选择的启动器

    //导入和导出的数据种类枚举
    public enum IODataType {
        ACCOUNT_DATA(
                "流水记录数据",
                "RunningAccount.json",
                RunningAccountDataHelper::new
        ),
        RULE_DATA(
                "通知解析规则数据",
                "AnalysisRule.json",
                AnalysisRuleDataHelper::new
        ),
        BUDGET_DATA(
                "预算数据",
                "Budget.json",
                BudgetDataHelper::new
        );
        private final String name;              //选项名称
        private final String defaultFileName;   //默认文件名称
        private final Function<Context, DataHelperBase<BookkeepingDbHelper, ?>> helperFactory;  //数据帮助器的构造方法

        IODataType(
                String name,
                String defaultFileName,
                Function<Context, DataHelperBase<BookkeepingDbHelper, ?>> helperFactory) {
            this.name = name;
            this.defaultFileName = defaultFileName;
            this.helperFactory = helperFactory;
        }

        public String getName() {
            return name;
        }

        public String getDefaultFileName() {
            return defaultFileName;
        }

        public DataHelperBase<BookkeepingDbHelper, ?> getDataHelper(Context context) {
            return helperFactory.apply(context);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDataManageBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.scrollView.setPadding(
                    0,
                    0,
                    0,
                    systemBars.bottom + AppearanceHelper.dpToPx(this, 15)
            );
            return insets;
        });

        dataIOHelper = new DataIOHelper(this);
        autoBackupHelper = new AutoBackupHelper(this);
        initActivityLaunchers();

        initDataManageSettings();
    }

    /**
     * 初始化活动启动器
     */
    private void initActivityLaunchers() {
//        exportDataLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    int resultCode = result.getResultCode();
//                    Intent data = result.getData();
//                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
//                        dataIOHelper.clearTempFile();
//                        return;
//                    }
//
//                    ProgressDialog progressDialog = new ProgressDialog(this, "导出数据", "正在导出数据……");
//                    progressDialog.buildDialog(
//                            null,
//                            () -> {
//                                disposables.clear();
//                                Toast.makeText(this, "已取消数据导出", Toast.LENGTH_SHORT).show();
//                            },
//                            false);
//                    progressDialog.show();
//
//                    disposables.add(
//                            Observable.fromCallable(() -> {
//                                        dataIOHelper.handleExportResult(data.getData());
//                                        return true;
//                                    })
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(b -> Toast.makeText(this, "数据导出成功", Toast.LENGTH_SHORT).show(),
//                                            e -> {
//                                                ExceptionHelper.showExceptionDialog(this, e);
//                                                progressDialog.dismiss();
//                                            },
//                                            progressDialog::dismiss
//                                    )
//                    );
//                }
//        );
//
//        importDataLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    int resultCode = result.getResultCode();
//                    Intent data = result.getData();
//                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
//                        dataIOHelper.clearTempFile();
//                        return;
//                    }
//
//                    ProgressDialog progressDialog = new ProgressDialog(this, "导入数据", "正在扫描备份文件……");
//                    progressDialog.buildDialog(
//                            null,
//                            () -> {
//                                disposables.clear();
//                                Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
//                            },
//                            false);
//                    progressDialog.show();
//
//                    disposables.add(
//                            Observable.fromCallable(() -> {
//                                        dataIOHelper.handleImportResul(data.getData());
//                                        return true;
//                                    })
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(b -> {
//                                            },
//                                            e -> {
//                                                ExceptionHelper.showExceptionDialog(this, e);
//                                                progressDialog.dismiss();
//                                            },
//                                            progressDialog::dismiss
//                                    )
//                    );
//
//                }
//        );
//
//        backupDirectorySetLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    int resultCode = result.getResultCode();
//                    Intent data = result.getData();
//
//                    autoBackupHelper.handleActivityResult(resultCode, data, binding.backupDirectoryOption.descriptionText);
//                }
//        );
    }

    /**
     * 初始化数据管理条目
     */
    private void initDataManageSettings() {
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //导出数据
        SettingClickableTextView exportDataOption = new SettingClickableTextView(
                this,
                binding.exportDataOption,
                R.string.export_data,
                "将应用数据以文件形式保存",
                R.drawable.outline_file_export_24,
                RadiusStyle.TOP
        );
        exportDataOption.setFunctionListener(v -> exportData());

        //导入数据
        SettingClickableTextView importDataOption = new SettingClickableTextView(
                this,
                binding.importDataOption,
                R.string.import_data,
                "从外部文件导入数据",
                R.drawable.outline_download_24,
                RadiusStyle.MIDDLE
        );
        importDataOption.setFunctionListener(v -> importData());

        //清空流水数据
        SettingClickableTextView clearRunningAccountOption = new SettingClickableTextView(
                this,
                binding.clearAccountDataOption,
                R.string.clear_account_data,
                "清除流水相关数据",
                R.drawable.outline_delete_24,
                RadiusStyle.BOTTOM
        );
        clearRunningAccountOption.setFunctionListener(
                v -> new MaterialAlertDialogBuilder(this)
                        .setTitle("清除数据")
                        .setMessage("此操作将清除所有流水记录、标签、标签分组和预算数据，确认继续吗？")
                        .setPositiveButton("确认", (dialog, which) -> {
                            RunningAccountDataHelper.deleteAllData(this);
                            BudgetDataHelper.deleteAllData(this);
                            BookKeepingStartDatePreference.saveStartDate("", this); //清空已保存的开始记账的日期

                            //提醒流水界面刷新数据
                            RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
                            accountRepository.onAccountUpdated(0, "", null, AccountUpdateReason.CLEAR);

                            Toast.makeText(this, "流水数据已清空", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("取消", null)
                        .show()
        );

        //自动备份开关
        SettingSwitchView autoBackupSwitchOption = new SettingSwitchView(
                this,
                binding.autoBackupOption,
                R.string.auto_backup,
                "自动生成备份文件至指定位置",
                R.drawable.outline_settings_backup_restore_24,
                RadiusStyle.TOP
        );
        autoBackupHelper.setSwitchOptionView(autoBackupSwitchOption); //设置帮助器的开关视图，以便控制其状态
        String backupDir = AutoBackupPreference.getBackupDirectoryUri(this);
        boolean switchStat = AutoBackupPreference.getSwitchStat(this);
        autoBackupSwitchOption.setChecked(!backupDir.isEmpty() && switchStat);
        autoBackupSwitchOption.setFunctionListener(
                (buttonView, isChecked) -> onAutoBackupSwitchChanged(autoBackupSwitchOption, isChecked, backupDir)
        );

        //备份频率
        SettingSpinnerView backupFrequencyOption = new SettingSpinnerView(
                this,
                binding.backupFrequencyOption,
                R.string.backup_frequency,
                "自动备份的时间间隔",
                R.drawable.outline_timer_24,
                RadiusStyle.MIDDLE
        );
        int frequencyIndex = AutoBackupPreference.getBackupFrequency(this);
        backupFrequencyOption.setSpinnerText(
                BackupFrequency.values()[frequencyIndex].getTitle()
        );
        backupFrequencyOption.setFunctionListener(v -> {
            PopupMenu frequencyMenu = new PopupMenu(this, backupFrequencyOption.getFunctionComponent());

            //填充选项
            for (BackupFrequency frequency : BackupFrequency.values()) {
                int groupId = frequency.getGroupId();
                int itemId = frequency.getItemId();
                int order = frequency.getOrder();
                String title = frequency.getTitle();
                frequencyMenu.getMenu().add(groupId, itemId, order, title);
            }

            //设置监听
            frequencyMenu.setOnMenuItemClickListener(item -> {
                boolean isItemClicked = false;

                //获取选项编号列表
                List<Integer> itemIdList = Arrays.stream(BackupFrequency.values())
                        .map(BackupFrequency::getItemId)
                        .collect(Collectors.toList());

                //判断是否选中
                if (itemIdList.contains(item.getItemId())) {
                    int index = itemIdList.indexOf(item.getItemId());
                    backupFrequencyOption.setSpinnerText(item.getTitle());
                    isItemClicked = true;

                    int oldIndex = AutoBackupPreference.getBackupFrequency(this);  //获取之前的频率代码防止重复更新工作
                    if (oldIndex != index) {
                        BackupFrequency frequency = BackupFrequency.values()[index];
                        String title = frequency.getTitle();
                        backupFrequencyOption.setSpinnerText(title);
                        AutoBackupPreference.setBackupFrequency(this, index);

                        //只有开关打开时才更新工作内容并安排一次备份
                        if (AutoBackupPreference.getSwitchStat(this)) {
                            //更新工作内容
                            long intervalMillis = frequency.getIntervalMillis();
                            BackupScheduler.schedulePeriodicBackup(this, intervalMillis);

                            //立即备份一次
                            BackupScheduler.executeBackupNow(this);
                        }
                    }
                }

                return isItemClicked;
            });

            frequencyMenu.show();
        });

        //备份目录
        SettingClickableTextView backupDirectoryOption = new SettingClickableTextView(
                this,
                binding.backupDirectoryOption,
                R.string.backup_directory,
                "备份文件存储的位置",
                R.drawable.outline_folder_data_24,
                RadiusStyle.BOTTOM
        );
        backupDirectoryOption.setFunctionListener(
                v -> autoBackupHelper.selectBackupDirectory(backupDirectorySetLauncher)
        );
        String uriStr = AutoBackupPreference.getBackupDirectoryUri(this);
        if (!uriStr.isEmpty()) {
            String path = UriPathHelper.getDisplayPathFromSAFUri(this, Uri.parse(uriStr));
            binding.backupDirectoryOption.descriptionText.setText(path);
        }
    }

    /**
     * 导出数据
     */
    private void exportData() {
        //获取选项名称和状态
        String[] itemNames = Arrays.stream(IODataType.values())
                .map(IODataType::getName)
                .toArray(String[]::new);
        boolean[] choiceStats = new boolean[itemNames.length];
        Arrays.fill(choiceStats, true);

//        //显示多选对话框
//        MultiChoiceDialog multiChoiceDialog = new MultiChoiceDialog(
//                this,
//                "导出数据",
//                choiceStats,
//                itemNames,
//                (position, isChecked) -> choiceStats[position] = isChecked
//        );
//        multiChoiceDialog.buildDialog(() -> {
//            //检测是否一个都没有选择
//            boolean isNonItemChosen = true;
//            for (boolean isChose : choiceStats) {
//                if (isChose) {
//                    isNonItemChosen = false;
//                    break;
//                }
//            }
//
//            if (!isNonItemChosen) {
//                onExportDialogConfirmed(choiceStats);
//                multiChoiceDialog.dismiss();
//            } else {
//                Toast.makeText(this, "请选择至少一个选项", Toast.LENGTH_SHORT).show();
//            }
//        }, () -> {
//        });
//
//        //显示对话框
//        multiChoiceDialog.show();
    }

    /**
     * 导出数据对话框确认回调
     *
     * @param choseItem 各数据被选择的情况
     */
    private void onExportDialogConfirmed(@NonNull boolean[] choseItem) {
        Log.i(LogTags.SETTING_FRAGMENT.n(), "开始导出数据");
        List<String> fileNameList = new ArrayList<>();      //用于导出数据的临时文件名列表
        List<String> fileContentList = new ArrayList<>();   //用于导出数据的临时文件内容列表

        //根据选择的内容创建临时文件
        for (IODataType dataType : IODataType.values()) {
            if (!choseItem[dataType.ordinal()]) continue;

            DataHelperBase<BookkeepingDbHelper, ?> dataHelper = dataType.getDataHelper(this);
            try {
                String fileName = dataType.getDefaultFileName();
                String fileContent = dataHelper.getDataInJSON();

                fileNameList.add(fileName);
                fileContentList.add(fileContent);
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(this, e);
                Toast.makeText(this, "JSON序列化时出错", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //将文件打包至压缩包内
        boolean isAccountDataChosen = choseItem[IODataType.ACCOUNT_DATA.ordinal()];
        dataIOHelper.packDataInZip(
                exportDataLauncher,
                fileNameList,
                fileContentList,
                isAccountDataChosen
        );
    }

    /**
     * 从文件导入数据
     */
    private void importData() {
        Log.i(LogTags.SETTING_FRAGMENT.n(), "开始导入数据……");
        dataIOHelper.openFileViaSAF(
                new DataIOHelper.ImportCallback() {
                    @Override
                    public void onZipScanned(List<String> entryNameList) {
                        String[] dataTypeNames = Arrays.stream(IODataType.values())
                                .map(IODataType::getName)
                                .toArray(String[]::new);
                        boolean[] choiceStats = new boolean[dataTypeNames.length];     //选项的选择状态
                        boolean[] isItemFound = new boolean[dataTypeNames.length];   //是否找到对应名称的文件
                        Arrays.fill(choiceStats, true);
                        Arrays.fill(isItemFound, true);

                        //根据扫描结果禁用缺失的选项
                        boolean isAllDisabled = true;   //标记是否所有选项都被禁用
                        for (IODataType IODataType : IODataType.values()) {
                            boolean isFound = false;
                            for (String entryName : entryNameList) {
                                if (entryName.equals(IODataType.getDefaultFileName())) {
                                    isFound = true;
                                    isAllDisabled = false;
                                    break;
                                }
                            }

                            //标记该项为未包含
                            if (!isFound) {
                                int index = IODataType.ordinal();
                                choiceStats[index] = false;
                                isItemFound[index] = false;
                            }
                        }

                        //判断是否所有选项都被禁用
                        if (isAllDisabled) {
                            Toast.makeText(DataManageActivity.this, "请选择正确的备份文件", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //显示导入数据选择对话框
                        showImportItemChoiceDialog(choiceStats, isItemFound, dataTypeNames);
                    }

                    @Override
                    public void onOneJsonFileRead(File jsonFile) {
                        //读取选择的单个JSON文件
                        StringBuilder content_builder = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                content_builder.append(line).append("\n");
                            }
                        } catch (IOException e) {
                            ExceptionHelper.showExceptionDialog(DataManageActivity.this, e);
                            Toast.makeText(DataManageActivity.this, "临时文件读取失败，请重试", Toast.LENGTH_SHORT).show();
                            Log.e(LogTags.SETTING_FRAGMENT.n(), "临时文件读取失败");
                        }

                        //根据文件内容判断数据类型
                        String contentStr = content_builder.toString();
                        if (contentStr.startsWith("{\"basic_data\"")) {
                            Log.i(LogTags.SETTING_FRAGMENT.n(), "数据类型：流水记录数据");
                            RunningAccountDataHelper dataHelper = new RunningAccountDataHelper(DataManageActivity.this);
                            if (dataHelper.saveJsonDataToDb(contentStr)) {
                                //清空已保存的开始记账的日期
                                BookKeepingStartDatePreference.saveStartDate("", DataManageActivity.this);

                                Toast.makeText(DataManageActivity.this, "流水记录数据导入成功", Toast.LENGTH_SHORT).show();
                                Log.i(LogTags.SETTING_FRAGMENT.n(), "数据导入成功");
                            } else {
                                Toast.makeText(DataManageActivity.this, "无法解析文件内容", Toast.LENGTH_SHORT).show();
                            }
                        } else if (contentStr.startsWith("{\"rule_data\"")) {
                            Log.i(LogTags.SETTING_FRAGMENT.n(), "数据类型：通知解析规则数据");
                            AnalysisRuleDataHelper dataHelper = new AnalysisRuleDataHelper(DataManageActivity.this);
                            if (dataHelper.saveJsonDataToDb(contentStr)) {
                                //清空已保存的开始记账的日期
                                BookKeepingStartDatePreference.saveStartDate("", DataManageActivity.this);

                                Toast.makeText(DataManageActivity.this, "通知解析规则数据导入成功", Toast.LENGTH_SHORT).show();
                                Log.i(LogTags.SETTING_FRAGMENT.n(), "数据导入成功");
                            } else {
                                Toast.makeText(DataManageActivity.this, "无法解析文件内容", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(LogTags.SETTING_FRAGMENT.n(), "数据类型：未知");
                            Toast.makeText(DataManageActivity.this, "无法解析文件内容", Toast.LENGTH_SHORT).show();
                        }

                        //清除临时文件
                        dataIOHelper.clearTempFile();
                    }

                    @Override
                    public void onError(String errMessage) {
                        Toast.makeText(DataManageActivity.this, "导入失败：" + errMessage, Toast.LENGTH_SHORT).show();
                    }
                },
                importDataLauncher
        );
    }

    /**
     * 显示导入数据选择对话框
     *
     * @param choiceStats   可选项的初始状态
     * @param isItemEnabled 可选项是否启用
     * @param choiceItems   选项名称数组
     */
    private void showImportItemChoiceDialog(
            boolean[] choiceStats,
            boolean[] isItemEnabled,
            String[] choiceItems) {
//        MultiChoiceDialog multiChoiceDialog = new MultiChoiceDialog(
//                this,
//                "导入数据",
//                isItemEnabled,
//                choiceStats,
//                choiceItems,
//                (position, isChecked) -> choiceStats[position] = isChecked
//        );
//        multiChoiceDialog.buildDialog(() -> {
//        }, () -> {
//        });
//
//        //设置对话框的显示监听器
//        AlertDialog alertDialog = multiChoiceDialog.getDialog();
//        alertDialog.setOnShowListener(dialog -> {
//            Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            positiveBtn.setOnClickListener(v -> {
//                //检测是否一个都没有选择
//                boolean isNonItemChosen = true;
//                for (boolean isChose : choiceStats) {
//                    if (isChose) {
//                        isNonItemChosen = false;
//                        break;
//                    }
//                }
//
//                if (!isNonItemChosen) {
//                    Log.i(LogTags.SETTING_FRAGMENT.n(), "用户选择需要导入的数据并确认进行下一步");
//                    dialog.dismiss();   //仅当满足要求时才关闭
//
//                    //显示进度条对话框
//                    ProgressDialog progressDialog = new ProgressDialog(this, "导入数据", "正在导入数据……");
//                    progressDialog.buildDialog(
//                            null,
//                            () -> {
//                                Toast.makeText(this, "已取消数据导入", Toast.LENGTH_SHORT).show();
//                                disposables.clear();
//
//                                //清空流水记录和开始记账日期
//                                RunningAccountDataHelper.deleteAllData(this);
//                                BookKeepingStartDatePreference.saveStartDate("", this); //清空已保存的开始记账的日期
//
//                                //重置通知解析数据
//                                AnalysisRuleDataHelper.resetRule(this);
//
//                                RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
//                                accountRepository.onAccountUpdated(0, "", null, AccountUpdateReason.CLEAR);
//                            },
//                            false);
//                    progressDialog.show();
//
//                    disposables.add(
//                            Observable.fromCallable(() -> writeDataIntoDb(choiceStats))
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribeOn(Schedulers.io())
//                                    .subscribe(isSuccessful -> {
//                                        if (isSuccessful) {
//                                            Toast.makeText(this, "数据导入成功", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }, e -> {
//                                        Toast.makeText(this, "数据导入失败", Toast.LENGTH_SHORT).show();
//                                        ExceptionHelper.showExceptionDialog(this, e);
//
//                                        //清空流水记录和开始记账日期
//                                        RunningAccountDataHelper.deleteAllData(this);
//                                        BookKeepingStartDatePreference.saveStartDate("", this); //清空已保存的开始记账的日期
//
//                                        //重置通知解析数据
//                                        AnalysisRuleDataHelper.resetRule(this);
//                                    }, () -> {
//                                        progressDialog.dismiss();
//                                        dataIOHelper.clearTempFile();
//                                    })
//                    );
//                } else {
//                    Toast.makeText(this, "请选择至少一个选项", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//
//        //设置对话框隐藏监听
//        multiChoiceDialog.show();
    }

    /**
     * 将选中的备份文件中的数据写入数据库
     *
     * @param itemChooseStats 多选对话框的选择状况
     * @return 数据是否成功写入
     */
    private boolean writeDataIntoDb(@NonNull boolean[] itemChooseStats) throws NumberFormatException, IOException {
        boolean isImportSuccessfully = false;
        boolean isAccountDataChecked = itemChooseStats[IODataType.ACCOUNT_DATA.ordinal()];   //流水记录文件是否勾选
        boolean isRuleDataChecked = itemChooseStats[IODataType.RULE_DATA.ordinal()];         //通知解析规则文件是否勾选

        //获取解压得到的临时JSON文件
        List<File> tempJsonFileList = dataIOHelper.copyZipToTempAndUnpack();
        if (tempJsonFileList == null) {
            Log.e(LogTags.SETTING_FRAGMENT.n(), "无法获取解压得到的临时JSON文件");
            throw new NullPointerException("无法获取解压得到的临时JSON文件");
        }

        //如果选择了流水记录数据，则将图片解压至图片目录中
        if (isAccountDataChecked) {
            dataIOHelper.unpackPictureZip();
        }

        //将JSON数据写入数据库
        for (IODataType dataType : IODataType.values()) {
            if (!itemChooseStats[dataType.ordinal()]) continue;

            String targetFileName = dataType.getDefaultFileName();

            //获取数据帮助器
            DataHelperBase<BookkeepingDbHelper, ?> dataHelper;
            if (dataType == IODataType.RULE_DATA && isRuleDataChecked && isAccountDataChecked) {
                //当流水数据和规则数据都选中时，获取能够写入标签数据的数据帮助器
                dataHelper = new AnalysisRuleDataHelper(this, true);
            } else {
                dataHelper = dataType.getDataHelper(this);
            }

            for (File file : tempJsonFileList) {
                if (targetFileName.equals(file.getName())) {
                    //将数据保存至数据库
                    Log.i(LogTags.SETTING_FRAGMENT.n(), String.format(Locale.getDefault(), "正在尝试读取临时文件%s", targetFileName));
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        isImportSuccessfully = dataHelper.saveJsonDataToDb(content.toString()) || isImportSuccessfully;
                    }
                }
            }
        }

        //刷新流水视图
        RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
        accountRepository.onAccountUpdated(0, "", null, AccountUpdateReason.REFRESH);

        if (isImportSuccessfully) {
            //清空已保存的开始记账的日期
            BookKeepingStartDatePreference.saveStartDate("", this);

            Log.i(LogTags.SETTING_FRAGMENT.n(), "数据已成功导入");
            return true;
        } else {
            Log.w(LogTags.SETTING_FRAGMENT.n(), "无法解析文件内容");
            throw new RuntimeException("无法解析文件内容");
        }
    }

    /**
     * 自动备份开关状态变更回调
     *
     * @param switchView 变更状态的开关视图
     * @param isChecked  开关最后所在的状态
     * @param backupDir  自动备份的目录
     */
    private void onAutoBackupSwitchChanged(
            SettingSwitchView switchView,
            boolean isChecked,
            @NonNull String backupDir
    ) {
        if (backupDir.isEmpty() && isChecked) {    //备份目录无效则先提示设置
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                    .setTitle("功能启用提示")
                    .setMessage("该功能需要先设置备份文件存储目录，请点击“确定”按钮设置存储目录")
                    .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                    .setPositiveButton("确定",
                            (dialog, which) -> autoBackupHelper
                                    .selectBackupDirectory(backupDirectorySetLauncher)
                    );

            builder.setOnCancelListener(dialog -> {
                switchView.setChecked(false);
                BackupScheduler.cancelPeriodicBackup(this);
            });
            builder.show();
        } else if (isChecked) {
            int frequencyIndex = AutoBackupPreference.getBackupFrequency(this);
            long intervalMillis = BackupFrequency.values()[frequencyIndex].getIntervalMillis();
            BackupScheduler.schedulePeriodicBackup(this, intervalMillis);   //开关打开后立刻安排备份任务
        } else {
            BackupScheduler.cancelPeriodicBackup(this);
        }

        AutoBackupPreference.setSwitchStat(this, isChecked);
    }
}