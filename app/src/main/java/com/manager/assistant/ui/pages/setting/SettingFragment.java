package com.manager.assistant.ui.pages.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.enums.LogTags;
import com.manager.assistant.ManagerAssistant;
import com.manager.assistant.R;
import com.manager.assistant.broadcast.BroadcastConstants;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.preference.AutoBackupPreference;
import com.manager.assistant.data.data_save.preference.KeepAlivePreference;
import com.manager.assistant.databinding.FragmentSettingBinding;
import com.manager.assistant.helpers.AutoBackupHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.data.data_save.preference.AutoBookKeepingPreference;
import com.manager.assistant.data.data_save.preference.BookKeepingStartDatePreference;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.DataIOHelper;
import com.manager.assistant.helpers.UpdateHelper;
import com.manager.assistant.ui.others.dialogs.MultiChoiceDialog;
import com.manager.assistant.ui.others.dialogs.ProgressDialog;
import com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit.AnalysisRuleManageActivity;
import com.manager.assistant.helpers.AboutHelper;
import com.manager.assistant.helpers.ThemeModeHelper;
import com.manager.assistant.helpers.UpdateLogHelper;
import com.manager.assistant.ui.pages.setting.data_io.data_helpers.AnalysisRuleDataHelper;
import com.manager.assistant.ui.pages.setting.data_io.data_helpers.DataHelperBase;
import com.manager.assistant.ui.pages.setting.data_io.data_helpers.RunningAccountDataHelper;
import com.manager.assistant.data.data_save.preference.AppSettingsPreference;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingClickableTextView;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingSpinnerView;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingSwitchView;
import com.manager.assistant.ui.data_communication.account_recycler.AccountRecyclerViewModel;
import com.manager.assistant.workers.BackupScheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;                                         //绑定的XML视图
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //活动启动器
    private ActivityResultLauncher<Intent> backupDirectorySetLauncher;              //自动备份文件夹选择的启动器
    private DataIOHelper dataIOHelper;                                              //SAF文件帮助器
    private AutoBackupHelper autoBackupHelper;                                      //自动备份帮助器
    private final CompositeDisposable disposables = new CompositeDisposable();      //多线程任务列表

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);

        dataIOHelper = new DataIOHelper(requireContext());
        autoBackupHelper = new AutoBackupHelper(requireContext());

        initViews();
        initActivityLaunchers();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        initAppSettings();
        initDataManageSettings();
        initAutoBookkeepingSettings();
        initBackgroundSettings();
        initAboutSettings();
    }

    /**
     * 初始化应用设置部分
     */
    private void initAppSettings() {
        //主题模式
        SettingClickableTextView themeModeOption = new SettingClickableTextView(
                requireContext(),
                binding.themeModeOption,
                R.string.theme_mode,
                null,
                R.drawable.baseline_dark_mode_24
        );
        themeModeOption.setFunctionListener(v -> showThemeModeSelectDialog());

        //动态配色
        SettingSwitchView dynamicColorOption = new SettingSwitchView(
                requireContext(),
                binding.dynamicColorOption,
                R.string.dynamic_color,
                "将壁纸颜色作为APP主题色",
                R.drawable.baseline_color_lens_24
        );
        dynamicColorOption.setChecked(AppSettingsPreference.getDynamicColorStat(requireContext()));
        dynamicColorOption.setFunctionListener(
                (buttonView, isChecked) -> {
                    AppSettingsPreference.setDynamicColorStat(requireContext(), isChecked);

                    ManagerAssistant app = (ManagerAssistant) requireActivity().getApplication();
                    if (isChecked) {
                        DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                                .setThemeOverlay(R.style.Theme_ManagerAssistant_Dynamic)
                                .build();
                        DynamicColors.applyToActivitiesIfAvailable(app, options);
                    } else {
                        DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                                .setThemeOverlay(R.style.Theme_ManagerAssistant_Static)
                                .build();
                        DynamicColors.applyToActivitiesIfAvailable(app, options);
                    }
                    requireActivity().recreate();
                }
        );

        //首页选项
        SettingSpinnerView firstScreenOption = new SettingSpinnerView(
                requireContext(),
                binding.firstScreenOption,
                R.string.select_first_screen,
                "选择启动的第一屏",
                R.drawable.baseline_add_to_home_screen_24
        );
        String[] firstScreenTitles = {
                requireContext().getString(R.string.title_bookkeeping),
                requireContext().getString(R.string.title_home)
        };
        int screen_code = AppSettingsPreference.getFirstScreen(requireContext());
        firstScreenOption.setSpinnerText(firstScreenTitles[screen_code]);
        firstScreenOption.setFunctionListener(v -> {
            PopupMenu firstScreenMenu = new PopupMenu(requireContext(), firstScreenOption.getFunctionComponent());
            firstScreenMenu.getMenuInflater().inflate(R.menu.popup_menu_first_screen, firstScreenMenu.getMenu());

            firstScreenMenu.setOnMenuItemClickListener(item -> {
                boolean isItemClicked = false;

                int item_index = -1;
                int old_screen_code = AppSettingsPreference.getFirstScreen(requireContext());
                if (item.getItemId() == R.id.bookkeeping) {
                    item_index = 0;
                    isItemClicked = true;
                } else if (item.getItemId() == R.id.home) {
                    item_index = 1;
                    isItemClicked = true;
                }

                if (isItemClicked && item_index != old_screen_code) {
                    AppSettingsPreference.setFirstScreen(requireContext(), item_index);
                    firstScreenOption.setSpinnerText(firstScreenTitles[item_index]);
                }

                return isItemClicked;
            });

            firstScreenMenu.show();
        });

        SettingSwitchView homeLinksSwitch = new SettingSwitchView(
                requireContext(),
                binding.homeLinksOption,
                R.string.purchase_bulletin,
                "控制主页采购公告是否显示",
                R.drawable.baseline_link_24
        );
        if (!AppSettingsPreference.getLinkSwitchHide(requireContext())) {
            homeLinksSwitch.setChecked(AppSettingsPreference.getHomeLinks(requireContext()));
            homeLinksSwitch.setFunctionListener((buttonView, isChecked) -> {
                AppSettingsPreference.setHomeLinksShow(requireContext(), isChecked);

                if (!isChecked) {
                    Toast.makeText(requireContext(), "长按左侧文本可隐藏该设置项", Toast.LENGTH_SHORT).show();
                }
            });
            homeLinksSwitch.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("隐藏主页链接设置项")
                        .setMessage("隐藏该设置项将永久关闭主页采购公告的显示，确认要隐藏吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            homeLinksSwitch.setVisibility(View.GONE);
                            AppSettingsPreference.setLinkSwitchHide(requireContext(), true);
                            AppSettingsPreference.setHomeLinksShow(requireContext(), false);
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            });
        } else {
            homeLinksSwitch.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化数据管理条目
     */
    private void initDataManageSettings() {
        //导出数据
        SettingClickableTextView exportDataOption = new SettingClickableTextView(
                requireContext(),
                binding.exportDataOption,
                R.string.export_data,
                "将应用数据以文件形式保存",
                R.drawable.round_file_uploade_24
        );
        exportDataOption.setFunctionListener(v -> onExportDataClicked());

        //导入数据
        SettingClickableTextView importDataOption = new SettingClickableTextView(
                requireContext(),
                binding.importDataOption,
                R.string.import_data,
                "从外部文件导入数据",
                R.drawable.baseline_file_download_24
        );
        importDataOption.setFunctionListener(v -> importData());

        //清空流水数据
        SettingClickableTextView clearRunningAccountOption = new SettingClickableTextView(
                requireContext(),
                binding.clearAccountDataOption,
                R.string.clear_account_data,
                "清除流水相关数据",
                R.drawable.baseline_delete_forever_24
        );
        clearRunningAccountOption.setFunctionListener(
                v -> new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("清除数据")
                        .setMessage("此操作将清除所有流水记录、标签和标签分组数据，确认继续吗？")
                        .setPositiveButton("确认", ((dialog, which) -> {
                            dialog.dismiss();
                            RunningAccountDataHelper.deleteAllData(requireContext());
                            BookKeepingStartDatePreference.saveStartDate("", requireContext()); //清空已保存的开始记账的日期

                            //通过ViewModel提醒流水界面刷新数据
                            AccountRecyclerViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountRecyclerViewModel.class);
                            viewModel.triggerDataUpdate();
                        }))
                        .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                        .show()
        );

        //自动备份开关
        SettingSwitchView autoBackupSwitch = new SettingSwitchView(
                requireContext(),
                binding.autoBackupOption,
                R.string.auto_backup,
                null,
                R.drawable.baseline_settings_backup_restore_24
        );
        autoBackupHelper.setSwitchOptionView(autoBackupSwitch); //设置帮助器的开关视图，以便控制其状态
        String backupDir = AutoBackupPreference.getBackupDirectoryUri(requireContext());
        boolean switchStat = AutoBackupPreference.getSwitchStat(requireContext());
        if (!backupDir.isEmpty() && switchStat) {
            autoBackupSwitch.setChecked(true);
            binding.autoBackupLayout.setVisibility(View.VISIBLE);
        } else {
            autoBackupSwitch.setChecked(false);
            binding.autoBackupLayout.setVisibility(View.GONE);
        }
        autoBackupSwitch.setFunctionListener(
                (buttonView, isChecked) -> {
                    if (backupDir.isEmpty() && isChecked) {    //备份目录无效则先提示设置
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("功能启用提示")
                                .setMessage("该功能需要先设置备份文件存储目录，请点击“确定”按钮设置存储目录")
                                .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                                .setPositiveButton("确定",
                                        (dialog, which) -> autoBackupHelper.selectBackupDirectory(
                                                backupDirectorySetLauncher
                                        )
                                );

                        builder.setOnCancelListener(dialog -> {
                            buttonView.setChecked(false);
                            BackupScheduler.cancelPeriodicBackup(requireContext());
                        });
                        builder.show();
                    } else if (isChecked) {
                        int frequency_index = AutoBackupPreference.getBackupFrequency(requireContext());
                        long intervalMillis = AutoBackupHelper.BackupFrequency.values()[frequency_index].getIntervalMillis();
                        BackupScheduler.schedulePeriodicBackup(requireContext(), intervalMillis);
                    } else {
                        BackupScheduler.cancelPeriodicBackup(requireContext());
                    }
                    AnimationHelper.switchViewFoldOrExpanded(isChecked, binding.autoBackupLayout);
                    AutoBackupPreference.setSwitchStat(requireContext(), isChecked);
                }
        );

        //备份频率
        SettingSpinnerView backupFrequencyOption = new SettingSpinnerView(
                requireContext(),
                binding.backupFrequencyOption,
                R.string.backup_frequency,
                "自动备份的时间间隔",
                R.drawable.baseline_timer_24
        );
        int frequency_index = AutoBackupPreference.getBackupFrequency(requireContext());
        String frequencyName = AutoBackupHelper.BackupFrequency.values()[frequency_index].getName();
        backupFrequencyOption.setSpinnerText(frequencyName);
        backupFrequencyOption.setFunctionListener(v -> {
            PopupMenu frequencyMenu = new PopupMenu(requireContext(), backupFrequencyOption.getFunctionComponent());
            frequencyMenu.getMenuInflater().inflate(R.menu.popup_menu_backup_frequency, frequencyMenu.getMenu());

            frequencyMenu.setOnMenuItemClickListener(item -> {
                boolean isItemClicked = false;

                int old_index = AutoBackupPreference.getBackupFrequency(requireContext());  //获取之前的频率代码防止重复更新工作
                int item_index = -1;
                if (item.getItemId() == R.id.every_15_min) {
                    isItemClicked = true;
                    item_index = 0;
                } else if (item.getItemId() == R.id.every_day) {
                    isItemClicked = true;
                    item_index = 1;
                } else if (item.getItemId() == R.id.every_week) {
                    isItemClicked = true;
                    item_index = 2;
                } else if (item.getItemId() == R.id.every_month) {
                    isItemClicked = true;
                    item_index = 3;
                }

                if (isItemClicked && old_index != item_index) {
                    AutoBackupHelper.BackupFrequency frequency = AutoBackupHelper.BackupFrequency.values()[item_index];
                    String title = frequency.getName();
                    backupFrequencyOption.setSpinnerText(title);

                    AutoBackupPreference.setBackupFrequency(requireContext(), item_index);
                    long intervalMillis = frequency.getIntervalMillis();
                    BackupScheduler.schedulePeriodicBackup(requireContext(), intervalMillis);   //更新工作内容

                    //立即备份一次
                    BackupScheduler.executeBackupNow(requireContext());
                }

                return isItemClicked;
            });

            frequencyMenu.show();
        });

        //备份目录
        SettingClickableTextView backupDirectoryOption = new SettingClickableTextView(
                requireContext(),
                binding.backupDirectoryOption,
                R.string.backup_directory,
                "备份文件存储的位置",
                R.drawable.baseline_folder_zip_24
        );
        backupDirectoryOption.setFunctionListener(
                v -> autoBackupHelper.selectBackupDirectory(backupDirectorySetLauncher)
        );
        String uriStr = Uri.decode(AutoBackupPreference.getBackupDirectoryUri(requireContext()));
        if (!uriStr.isEmpty()) {
            uriStr = uriStr.substring(61);
            binding.backupDirectoryOption.descriptionText.setText(uriStr);
        }
    }

    /**
     * 初始化自动记账设置项
     */
    private void initAutoBookkeepingSettings() {
        //自动记账
        SettingSwitchView notificationAnalysisSwitchOption = new SettingSwitchView(
                requireContext(),
                binding.notificationAnalysisSwitchOption,
                R.string.notification_analysis_mode,
                "解析通知实现自动记账",
                R.drawable.baseline_notifications_24
        );
        boolean isNotificationAnalysisOpened = AutoBookKeepingPreference.getSwitchStat(requireContext());
        if (isNotificationAnalysisOpened && PermissionHelper.isNotificationServiceEnabled(requireContext())) {
            binding.ruleManageLayout.setVisibility(View.VISIBLE);
            notificationAnalysisSwitchOption.setChecked(true);
        } else {
            binding.ruleManageLayout.setVisibility(View.GONE);
            notificationAnalysisSwitchOption.setChecked(false);

            //考虑到无授权情况下自动关闭通知解析功能
            AutoBookKeepingPreference.setSwitchStat(false, requireContext());
        }
        notificationAnalysisSwitchOption.setFunctionListener(
                (buttonView, isChecked) -> onNotificationAnalysisSwitchChanged(notificationAnalysisSwitchOption, isChecked)
        );

        //开关左侧文本长按功能
        notificationAnalysisSwitchOption.setOnLongClickListener(v -> {
            PermissionHelper.requestNotificationPermission(requireContext());
            return true;
        });

        //通知解析规则管理
        SettingClickableTextView ruleManageOption = new SettingClickableTextView(
                requireContext(),
                binding.ruleManageOption,
                R.string.notification_analysis_rules_manage,
                "点击进入规则管理界面",
                R.drawable.baseline_rule_24
        );
        ruleManageOption.setFunctionListener(
                v -> {
                    Intent skip2NotificationRulesActivity = new Intent(requireContext(), AnalysisRuleManageActivity.class);
                    startActivity(skip2NotificationRulesActivity);
                }
        );

        //规则重置
        SettingClickableTextView resetRuleOption = new SettingClickableTextView(
                requireContext(),
                binding.resetRuleOption,
                R.string.reset_rule,
                "将现有规则重置为默认状态",
                R.drawable.baseline_restart_alt_24
        );
        resetRuleOption.setFunctionListener(
                v -> new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("重置规则")
                        .setMessage("此操作将删除现有的规则并替换为默认规则，确认继续吗？")
                        .setPositiveButton("确认", (dialog, which) -> {
                            dialog.dismiss();
                            AnalysisRuleDataHelper.resetRule(requireContext());
                        })
                        .setNegativeButton("取消", null)
                        .show()
        );
    }

    /**
     * 初始化后台设置选项
     */
    private void initBackgroundSettings() {
        //后台隐藏(最近任务隐藏)
        SettingSwitchView hideBackgroundOption = new SettingSwitchView(
                requireContext(),
                binding.hideBackgroundOption,
                R.string.hide_background,
                "在最近任务列表隐藏",
                R.drawable.baseline_recent_task_24
        );
        hideBackgroundOption.setChecked(KeepAlivePreference.getHideRecents(requireContext()));
        hideBackgroundOption.setFunctionListener(
                (buttonView, isChecked) -> KeepAlivePreference.setHideRecents(
                        isChecked,
                        requireContext()
                )
        );

        //自启动
        SettingClickableTextView autoStartOption = new SettingClickableTextView(
                requireContext(),
                binding.autoStartOption,
                R.string.auto_start_permission,
                "点击跳转自启动设置界面",
                R.drawable.baseline_autorenew_24
        );
        autoStartOption.setFunctionListener(
                v -> PermissionHelper.requestAutoStartPermission(requireContext())
        );

        //电池优化
        SettingClickableTextView batteryOptimizationOption = new SettingClickableTextView(
                requireContext(),
                binding.batteryOptimizationOption,
                R.string.battery_optimization,
                "跳转至安卓原生电池优化界面",
                R.drawable.baseline_battery_5_bar_24
        );
        batteryOptimizationOption.setFunctionListener(
                v -> PermissionHelper.openBatteryOptimizations(requireContext())
        );
    }

    /**
     * 初始化关于设置项
     */
    private void initAboutSettings() {
        //关于软件
        SettingClickableTextView aboutOption = new SettingClickableTextView(
                requireContext(),
                binding.aboutOption,
                R.string.about_software,
                null,
                R.drawable.baseline_info_24
        );
        aboutOption.setFunctionListener(
                v -> AboutHelper.showAboutDialog(requireContext())
        );

        //更新日志
        SettingClickableTextView updateLogOption = new SettingClickableTextView(
                requireContext(),
                binding.updateLogOption,
                R.string.update_log,
                null,
                R.drawable.baseline_insert_drive_file_24
        );
        updateLogOption.setFunctionListener(
                v -> UpdateLogHelper.showUpdateLogDialog(requireContext())
        );

        //更新检测
        SettingClickableTextView updateCheckOption = new SettingClickableTextView(
                requireContext(),
                binding.updateCheckOption,
                R.string.update_check,
                null,
                R.drawable.baseline_update_24
        );
        updateCheckOption.setFunctionListener(
                v -> {
                    Toast.makeText(requireContext(), "正在检查更新……", Toast.LENGTH_SHORT).show();
                    UpdateHelper.checkUpdate(requireContext(), true);
                }
        );
    }

    /**
     * 初始化活动启动器
     */
    private void initActivityLaunchers() {
        exportDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                        dataIOHelper.clearTempFile();
                        return;
                    }

                    ProgressDialog progressDialog = new ProgressDialog(requireContext(), "导出数据", "正在导出数据……");
                    progressDialog.buildDialog(
                            null,
                            () -> {
                                disposables.clear();
                                Toast.makeText(requireContext(), "已取消数据导出", Toast.LENGTH_SHORT).show();
                            },
                            false);
                    progressDialog.show();

                    disposables.add(
                            Observable.fromCallable(() -> {
                                        dataIOHelper.handleExportResult(data.getData());
                                        return true;
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(b -> Toast.makeText(requireContext(), "数据导出成功", Toast.LENGTH_SHORT).show(),
                                            e -> {
                                                ExceptionHelper.showExceptionDialog(requireContext(), e);
                                                progressDialog.dismiss();
                                            },
                                            progressDialog::dismiss
                                    )
                    );
                }
        );

        importDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();
                    if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                        dataIOHelper.clearTempFile();
                        return;
                    }

                    ProgressDialog progressDialog = new ProgressDialog(requireContext(), "导入数据", "正在扫描备份文件……");
                    progressDialog.buildDialog(
                            null,
                            () -> {
                                disposables.clear();
                                Toast.makeText(requireContext(), "已取消数据导入", Toast.LENGTH_SHORT).show();
                            },
                            false);
                    progressDialog.show();

                    disposables.add(
                            Observable.fromCallable(() -> {
                                        dataIOHelper.handleImportResul(data.getData());
                                        return true;
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(b -> {
                                            },
                                            e -> {
                                                ExceptionHelper.showExceptionDialog(requireContext(), e);
                                                progressDialog.dismiss();
                                            },
                                            progressDialog::dismiss
                                    )
                    );

                }
        );

        backupDirectorySetLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    autoBackupHelper.handleActivityResult(resultCode, data, binding.backupDirectoryOption.descriptionText);
                }
        );
    }

    /**
     * 导出数据并创建文件
     */
    private void exportData(@NonNull boolean[] choseItem) {
        Log.i(LogTags.SETTING_FRAGMENT.getV(), "开始导出数据");
        List<String> fileNameList = new ArrayList<>();      //用于导出数据的临时文件名列表
        List<String> fileContentList = new ArrayList<>();   //用于导出数据的临时文件内容列表

        //根据选择的内容创建临时文件
        for (IODataType dataType : IODataType.values()) {
            if (!choseItem[dataType.ordinal()]) continue;

            DataHelperBase<BookkeepingDbHelper, ?> dataHelper = dataType.getDataHelper(requireContext());
            try {
                String fileName = dataType.getDefaultFileName();
                String fileContent = dataHelper.getDataInJSON();

                fileNameList.add(fileName);
                fileContentList.add(fileContent);
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "JSON序列化时出错", Toast.LENGTH_SHORT).show();
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
        Log.i(LogTags.SETTING_FRAGMENT.getV(), "开始导入数据……");
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

                            if (!isFound) {
                                int index = IODataType.ordinal();
                                String dataTypeName = IODataType.getName();
                                String disabledName = String.format(Locale.getDefault(), "%s(未包含)", dataTypeName);
                                dataTypeNames[index] = disabledName;
                                choiceStats[index] = false;
                                isItemFound[index] = false;
                            }
                        }

                        //判断是否所有选项都被禁用
                        if (isAllDisabled) {
                            Toast.makeText(requireContext(), "请选择正确的备份文件", Toast.LENGTH_SHORT).show();
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
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                            Toast.makeText(requireContext(), "临时文件读取失败，请重试", Toast.LENGTH_SHORT).show();
                            Log.e(LogTags.SETTING_FRAGMENT.getV(), "临时文件读取失败");
                        }

                        //根据文件内容判断数据类型
                        String content_str = content_builder.toString();
                        if (content_str.startsWith("{\"basic_data\"")) {
                            Log.i(LogTags.SETTING_FRAGMENT.getV(), "数据类型：流水记录数据");
                            RunningAccountDataHelper dataHelper = new RunningAccountDataHelper(requireContext());
                            if (dataHelper.saveJsonDataToDb(content_str)) {
                                Toast.makeText(requireContext(), "流水记录数据导入成功", Toast.LENGTH_SHORT).show();
                                Log.i(LogTags.SETTING_FRAGMENT.getV(), "数据导入成功");
                            } else {
                                Toast.makeText(requireContext(), "无法解析文件内容", Toast.LENGTH_SHORT).show();
                            }
                        } else if (content_str.startsWith("{\"rule_data\"")) {
                            Log.i(LogTags.SETTING_FRAGMENT.getV(), "数据类型：通知解析规则数据");
                            AnalysisRuleDataHelper dataHelper = new AnalysisRuleDataHelper(requireContext());
                            if (dataHelper.saveJsonDataToDb(content_str)) {
                                Toast.makeText(requireContext(), "通知解析规则数据导入成功", Toast.LENGTH_SHORT).show();
                                Log.i(LogTags.SETTING_FRAGMENT.getV(), "数据导入成功");
                            } else {
                                Toast.makeText(requireContext(), "无法解析文件内容", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(LogTags.SETTING_FRAGMENT.getV(), "数据类型：未知");
                            Toast.makeText(requireContext(), "无法解析文件内容", Toast.LENGTH_SHORT).show();
                        }

                        //清除临时文件
                        dataIOHelper.clearTempFile();
                    }


                    @Override
                    public void onError(String errMessage) {
                        Toast.makeText(requireContext(), "导入失败：" + errMessage, Toast.LENGTH_SHORT).show();
                    }
                },
                importDataLauncher
        );
    }

    /**
     * 处理导出数据选项点击的方法
     */
    private void onExportDataClicked() {
        //获取选项名称和状态
        String[] itemNames = Arrays.stream(IODataType.values())
                .map(IODataType::getName)
                .toArray(String[]::new);
        boolean[] choiceStats = new boolean[itemNames.length];
        Arrays.fill(choiceStats, true);

        //显示多选对话框
        MultiChoiceDialog multiChoiceDialog = new MultiChoiceDialog(
                requireContext(),
                "导出数据",
                choiceStats,
                itemNames,
                (position, isChecked) -> choiceStats[position] = isChecked
        );

        //设置显示监听器后无需再次设置按钮点击回调
        multiChoiceDialog.buildDialog(() -> {
        }, () -> {
        });

        //设置对话框显示监听
        AlertDialog alertDialog = multiChoiceDialog.getDialog();
        alertDialog.setOnShowListener(
                dialogInterface -> {
                    Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveBtn.setOnClickListener(view -> {
                        //检测是否一个都没有选择
                        boolean isNonItemChosen = true;
                        for (boolean isChose : choiceStats) {
                            if (isChose) {
                                isNonItemChosen = false;
                                break;
                            }
                        }

                        if (!isNonItemChosen) {
                            exportData(choiceStats);
                            multiChoiceDialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );

        //显示对话框
        multiChoiceDialog.show();
    }

    /**
     * 通知解析开关状态变更调用的方法
     *
     * @param switchView 开关视图
     * @param isChecked  开关状态
     */
    private void onNotificationAnalysisSwitchChanged(SettingSwitchView switchView, boolean isChecked) {
        AutoBookKeepingPreference.setSwitchStat(isChecked, requireContext());   //将打开状态写入文件

        //开启开关时检测是否没有权限，如果没有则提示用户授权
        if (!PermissionHelper.isNotificationServiceEnabled(requireContext()) && isChecked) {
            switchView.setChecked(false);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("权限申请说明")
                    .setMessage("此功能需要使用“通知使用权”权限，该权限允许应用读取其他软件发送的通知内容。本应用不会也无法使用该权限获取用户隐私信息，仅用于解析通知中可能出现的流水账信息，请您放心使用。\n\n是否为本应用授权？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        //申请通知监听权限
                        PermissionHelper.requestNotificationPermission(requireContext());
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            //发送功能开关变更广播
            Intent functionSwitched = new Intent(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString());
            requireContext().sendBroadcast(functionSwitched);
            AnimationHelper.switchViewFoldOrExpanded(isChecked, binding.ruleManageLayout);  //切换通知解析选项布局的可见性
        }
    }

    /**
     * 显示主题模式选择对话框
     */
    private void showThemeModeSelectDialog() {
        String[] themeModeStr = {"浅色模式", "深色模式", "跟随系统"};
        int theme_mode = AppSettingsPreference.getThemeMode(requireContext());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("主题模式")
                .setSingleChoiceItems(themeModeStr, theme_mode, ((dialog, which) -> {
                    AppSettingsPreference.setThemeMode(requireContext(), which);
                    ThemeModeHelper.applyTheme(which);
                    dialog.dismiss();
                }))
                .setNegativeButton("关闭", null)
                .show();
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
        MultiChoiceDialog multiChoiceDialog = new MultiChoiceDialog(
                requireContext(),
                "导入数据",
                isItemEnabled,
                choiceStats,
                choiceItems,
                (position, isChecked) -> choiceStats[position] = isChecked
        );
        multiChoiceDialog.buildDialog(() -> {
        }, () -> {
        });

        //设置对话框的显示监听器
        AlertDialog alertDialog = multiChoiceDialog.getDialog();
        alertDialog.setOnShowListener(dialog -> {
            Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                //检测是否一个都没有选择
                boolean isNonItemChosen = true;
                for (boolean isChose : choiceStats) {
                    if (isChose) {
                        isNonItemChosen = false;
                        break;
                    }
                }

                if (!isNonItemChosen) {
                    Log.i(LogTags.SETTING_FRAGMENT.getV(), "用户选择需要导入的数据并确认进行下一步");
                    dialog.dismiss();   //仅当满足要求时才关闭

                    //显示进度条对话框
                    ProgressDialog progressDialog = new ProgressDialog(requireContext(), "导入数据", "正在导入数据……");
                    progressDialog.buildDialog(
                            null,
                            () -> {
                                Toast.makeText(requireContext(), "已取消数据导入", Toast.LENGTH_SHORT).show();
                                disposables.clear();

                                //清空流水记录和开始记账日期
                                RunningAccountDataHelper.deleteAllData(requireContext());
                                BookKeepingStartDatePreference.saveStartDate("", requireContext()); //清空已保存的开始记账的日期

                                //重置通知解析数据
                                AnalysisRuleDataHelper.resetRule(requireContext());

                                //通过ViewModel提醒流水界面刷新数据
                                AccountRecyclerViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountRecyclerViewModel.class);
                                viewModel.triggerDataUpdate();
                            },
                            false);
                    progressDialog.show();

                    disposables.add(
                            Observable.fromCallable(() -> writeDataIntoDb(choiceStats))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(isSuccessful -> {
                                        if (isSuccessful) {
                                            Toast.makeText(requireContext(), "数据导入成功", Toast.LENGTH_SHORT).show();
                                        }
                                    }, e -> {
                                        Toast.makeText(requireContext(), "数据导入失败", Toast.LENGTH_SHORT).show();
                                        ExceptionHelper.showExceptionDialog(requireContext(), e);

                                        //清空流水记录和开始记账日期
                                        RunningAccountDataHelper.deleteAllData(requireContext());
                                        BookKeepingStartDatePreference.saveStartDate("", requireContext()); //清空已保存的开始记账的日期

                                        //重置通知解析数据
                                        AnalysisRuleDataHelper.resetRule(requireContext());
                                    }, () -> {
                                        progressDialog.dismiss();
                                        dataIOHelper.clearTempFile();

                                        //通过ViewModel提醒流水界面刷新数据
                                        AccountRecyclerViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountRecyclerViewModel.class);
                                        viewModel.triggerDataUpdate();
                                    })
                    );
                } else {
                    Toast.makeText(requireContext(), "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //设置对话框隐藏监听
        multiChoiceDialog.show();
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
            Log.e(LogTags.SETTING_FRAGMENT.getV(), "无法获取解压得到的临时JSON文件");
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
                dataHelper = new AnalysisRuleDataHelper(requireContext(), true);
            } else {
                dataHelper = dataType.getDataHelper(requireContext());
            }

            for (File file : tempJsonFileList) {
                if (targetFileName.equals(file.getName())) {
                    //将数据保存至数据库
                    Log.i(LogTags.SETTING_FRAGMENT.getV(), String.format(Locale.getDefault(), "正在尝试读取临时文件%s", targetFileName));
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

        if (isImportSuccessfully) {
            Log.i(LogTags.SETTING_FRAGMENT.getV(), "数据已成功导入");
            return true;
        } else {
            Log.w(LogTags.SETTING_FRAGMENT.getV(), "无法解析文件内容");
            throw new RuntimeException("无法解析文件内容");
        }
    }
}