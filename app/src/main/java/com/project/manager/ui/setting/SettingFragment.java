package com.project.manager.ui.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.project.manager.LogTags;
import com.project.manager.R;
import com.project.manager.broadcast.BroadcastConstants;
import com.project.manager.data.data_save.database.BookKeepingDatabaseHelper;
import com.project.manager.data.data_save.preference.KeepAlivePreference;
import com.project.manager.databinding.FragmentSettingBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.helpers.PermissionHelper;
import com.project.manager.data.data_save.preference.AutoBookKeepingPreference;
import com.project.manager.data.data_save.preference.BookKeepingStartDatePreference;
import com.project.manager.helpers.AnimationHelper;
import com.project.manager.helpers.SAFFileHelper;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.rule_edit.AnalysisRuleManageActivity;
import com.project.manager.helpers.AboutHelper;
import com.project.manager.helpers.ThemeModeHelper;
import com.project.manager.helpers.UpdateLogHelper;
import com.project.manager.ui.setting.data_io.MultiChoiceDialogAdapter;
import com.project.manager.ui.setting.data_io.data_helpers.AnalysisRuleDataHelper;
import com.project.manager.ui.setting.data_io.data_helpers.DataHelperBase;
import com.project.manager.ui.setting.data_io.data_helpers.RunningAccountDataHelper;
import com.project.manager.data.data_save.preference.ThemeModePreference;
import com.project.manager.ui.setting.data_io.maps.TotalAccountDataMap;
import com.project.manager.ui.setting.data_io.maps.TotalRuleDataMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //活动启动器
    private SAFFileHelper safFileHelper;    //SAF文件帮助器

    //导入和导出的数据种类枚举
    enum IODataType {
        ACCOUNT_DATA("流水记录数据", "RunningAccount.json"),
        RULE_DATA("通知解析规则数据", "AnalysisRule.json");
        private final String name;              //选项名称
        private final String default_file_name; //默认文件名称

        IODataType(String name, String default_file_name) {
            this.name = name;
            this.default_file_name = default_file_name;
        }

        public String getName() {
            return name;
        }

        public String getDefault_file_name() {
            return default_file_name;
        }

        /**
         * 获取有效文件名列表
         *
         * @return 有效文件的文件名列表
         */
        public static List<String> getEffectiveFileNameList() {
            return Stream.of(values())
                    .map(IODataType::getDefault_file_name)
                    .collect(Collectors.toList());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);

        initViews();
        initActivityLaunchers();

        safFileHelper = new SAFFileHelper(requireContext());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //初始化活动启动器
    private void initActivityLaunchers() {
        exportDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    safFileHelper.handleActivityResult(resultCode, data, true);
                }
        );

        importDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    safFileHelper.handleActivityResult(resultCode, data, false);
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
        if (choseItem[IODataType.ACCOUNT_DATA.ordinal()]) {
            DataHelperBase<BookKeepingDatabaseHelper, TotalAccountDataMap> dataHelper = new RunningAccountDataHelper(requireContext());
            try {
                String json_str = dataHelper.getDataInJSON();
                fileNameList.add(IODataType.ACCOUNT_DATA.getDefault_file_name());
                fileContentList.add(json_str);
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "JSON序列化时出错", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (choseItem[IODataType.RULE_DATA.ordinal()]) {
            DataHelperBase<BookKeepingDatabaseHelper, TotalRuleDataMap> dataHelper = new AnalysisRuleDataHelper(requireContext());
            try {
                String json_str = dataHelper.getDataInJSON();
                fileNameList.add(IODataType.RULE_DATA.getDefault_file_name());
                fileContentList.add(json_str);
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "JSON序列化时出错", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //将文件打包至压缩包内
        safFileHelper.packFileInZip(
                new SAFFileHelper.WriteCallback() {
                    @Override
                    public void onFileWrote() {
                        Toast.makeText(requireContext(), "导出成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errMessage) {
                        Toast.makeText(requireContext(), "导出失败：" + errMessage, Toast.LENGTH_SHORT).show();
                    }
                },
                exportDataLauncher,
                fileNameList,
                fileContentList
        );
    }

    /**
     * 从文件导入数据
     */
    private void importData() {
        Log.i(LogTags.SETTING_FRAGMENT.getV(), "开始导入数据……");
        safFileHelper.openFileBySAF(
                new SAFFileHelper.ReadCallback() {
                    @Override
                    public void onZipUnpacked(List<File> fileList) {
                        List<File> effectiveFileList = getEffectiveFileList(fileList);

                        String[] type_names = Arrays.stream(IODataType.values())
                                .map(IODataType::getName)
                                .toArray(String[]::new);
                        boolean[] itemStats = {true, true};     //选项的选择状态
                        boolean[] isItemFound = {true, true};   //是否找到对应名称的文件

                        //根据解压的临时JSON文件决定应该禁用哪些选项
                        for (IODataType IODataType : IODataType.values()) {
                            boolean isFound = false;
                            for (File tempFile : fileList) {
                                if (tempFile.getName().equals(IODataType.getDefault_file_name())) {
                                    isFound = true;
                                    break;
                                }
                            }

                            if (!isFound) {
                                int index = IODataType.ordinal();
                                String type_name = IODataType.getName();
                                String disabled_name = String.format(Locale.getDefault(), "%s(未包含)", type_name);
                                type_names[index] = disabled_name;
                                itemStats[index] = false;
                                isItemFound[index] = false;
                            }
                        }

                        //判断是否所有选项都被禁用
                        boolean isAllFalse = true;
                        for (boolean isFound : isItemFound) {
                            if (isFound) {
                                isAllFalse = false;
                                break;
                            }
                        }
                        if (isAllFalse) {
                            Toast.makeText(requireContext(), "请选择正确的备份文件", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //显示导入数据选择对话框
                        showImportItemChoiceDialog(itemStats, isItemFound, type_names, effectiveFileList);
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
                        safFileHelper.clearTempFile();
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
     * 从临时JSON文件列表中过滤有效的文件
     *
     * @param tempJsonFileList 临时JSON文件列表
     * @return 包含有效文件的列表
     */
    @NonNull
    private static List<File> getEffectiveFileList(@NonNull List<File> tempJsonFileList) {
        List<String> effectiveFileNameList = IODataType.getEffectiveFileNameList();
        List<File> effectiveFileList = new ArrayList<>();   //能够正确解析内容的文件列表

        //根据文件名筛选有效文件
        for (File tempJsonFile : tempJsonFileList) {
            //通过文件名称筛选文件
            String file_name = tempJsonFile.getName();
            if (!effectiveFileNameList.contains(file_name)) continue;

            effectiveFileList.add(tempJsonFile);
        }
        return effectiveFileList;
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //主题模式
        SettingClickableTextView themeModeOption = new SettingClickableTextView(requireContext());
        themeModeOption.setActions(
                R.string.theme_mode,
                null,
                R.drawable.baseline_dark_mode_24,
                v -> showThemeModeSelectDialog());
        binding.appSettingsLayout.addView(themeModeOption);

        //导出数据
        SettingClickableTextView exportDataOption = new SettingClickableTextView(requireContext());
        exportDataOption.setActions(
                R.string.export_data,
                "将应用数据以文件形式保存",
                R.drawable.round_export_data_24,
                v -> onExportDataClicked()
        );
        binding.dataManageLayout.addView(exportDataOption);

        //导入数据
        SettingClickableTextView importDataOption = new SettingClickableTextView(requireContext());
        importDataOption.setActions(
                R.string.import_data,
                "从外部文件导入数据",
                R.drawable.baseline_import_data_24,
                v -> importData()
        );
        binding.dataManageLayout.addView(importDataOption);

        //清空流水数据
        SettingClickableTextView clearRunningAccountOption = new SettingClickableTextView(requireContext());
        clearRunningAccountOption.setActions(
                R.string.clear_account_data,
                "清除流水记录、标签和标签分组数据",
                R.drawable.baseline_delete_forever_24,
                v -> new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("清除数据")
                        .setMessage("此操作将清除所有流水账数据，确认继续吗？")
                        .setPositiveButton("确认", ((dialog, which) -> {
                            dialog.dismiss();
                            RunningAccountDataHelper.deleteAllData(requireContext());
                            BookKeepingStartDatePreference.saveStartDate("", requireContext()); //清空已保存的开始记账的日期
                        }))
                        .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                        .show()
        );
        binding.dataManageLayout.addView(clearRunningAccountOption);

        //自动记账
        SettingSwitchView notificationAnalysisSwitchOption = new SettingSwitchView(requireContext());
        //完成通知解析开关状态初始化
        boolean isNotificationAnalysisOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(requireContext());
        if (isNotificationAnalysisOpened && PermissionHelper.isNotificationServiceEnabled(requireContext())) {
            binding.ruleManageLayout.setVisibility(View.VISIBLE);
            notificationAnalysisSwitchOption.setChecked(true);
        } else {
            binding.ruleManageLayout.setVisibility(View.GONE);
            notificationAnalysisSwitchOption.setChecked(false);

            //考虑到无授权情况下自动关闭通知解析功能
            AutoBookKeepingPreference.setNotificationAnalysisOpened(false, requireContext());
        }
        notificationAnalysisSwitchOption.setActions(
                R.string.notification_analysis_mode,
                "通知解析功能的开关",
                R.drawable.baseline_notifications_24,
                (buttonView, isChecked) -> {
                    AutoBookKeepingPreference.setNotificationAnalysisOpened(isChecked, requireActivity());  //将打开状态写入文件

                    //开启开关时检测是否没有权限，如果没有则提示用户授权
                    if (!PermissionHelper.isNotificationServiceEnabled(requireContext()) && isChecked) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("权限申请说明")
                                .setMessage("此功能需要使用“通知使用权”权限，该权限允许应用读取其他软件发送的通知内容。本应用不会也无法使用该权限获取用户隐私信息，仅用于解析通知中可能出现的流水账信息，请您放心使用。\n是否为本应用授权？")
                                .setPositiveButton("确认", (dialog, which) -> {
                                    dialog.dismiss();
                                    PermissionHelper.requestNotificationPermission(requireContext());
                                    AnimationHelper.switchViewFoldOrExpanded(true, binding.ruleManageLayout);  //切换通知解析选项布局的可见性

                                    //发送功能开关变更广播
                                    Intent functionSwitched = new Intent(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString());
                                    requireContext().sendBroadcast(functionSwitched);
                                })
                                .setNegativeButton("取消", (dialog, which) -> {
                                    notificationAnalysisSwitchOption.setChecked(false);
                                    dialog.dismiss();
                                })
                                .show();
                    } else {
                        //发送功能开关变更广播
                        Intent functionSwitched = new Intent(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString());
                        requireContext().sendBroadcast(functionSwitched);
                        AnimationHelper.switchViewFoldOrExpanded(isChecked, binding.ruleManageLayout);  //切换通知解析选项布局的可见性
                    }
                }
        );
        binding.autoBookkeepingLayout.addView(notificationAnalysisSwitchOption, 1);

        //通知解析规则管理
        SettingClickableTextView analysisRuleManageOption = new SettingClickableTextView(requireContext());
        analysisRuleManageOption.setActions(
                R.string.notification_analysis_rules_manage,
                "点击进入通知解析规则管理界面",
                R.drawable.baseline_rule_24,
                v -> {
                    Intent skip2NotificationRulesActivity = new Intent(requireContext(), AnalysisRuleManageActivity.class);
                    startActivity(skip2NotificationRulesActivity);
                }
        );
        binding.ruleManageLayout.addView(analysisRuleManageOption);

        //规则重置
        SettingClickableTextView resetRuleOption = new SettingClickableTextView(requireContext());
        resetRuleOption.setActions(
                R.string.reset_rule,
                "将通知解析规则重置为默认规则",
                R.drawable.baseline_restart_alt_24,
                v -> new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("重置规则")
                        .setMessage("此操作将重置通知解析规则为默认规则，确认继续吗？")
                        .setPositiveButton("确认", ((dialog, which) -> {
                            dialog.dismiss();
                            AnalysisRuleDataHelper.resetRule(requireContext());
                        }))
                        .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                        .show()
        );
        binding.ruleManageLayout.addView(resetRuleOption);

        //后台隐藏(最近任务隐藏)
        SettingSwitchView hideBackgroundOption = new SettingSwitchView(requireContext());
        hideBackgroundOption.setChecked(KeepAlivePreference.getHideRecents(requireContext()));
        hideBackgroundOption.setActions(
                R.string.hide_background,
                "从主页退出后在最近任务列表隐藏本应用",
                R.drawable.baseline_recent_task_24,
                (buttonView, isChecked) -> {
                    KeepAlivePreference.setHideRecents(isChecked, requireContext());

                    if (isChecked) {
                        Toast.makeText(requireContext(), "建议额外在最近任务中锁定本应用", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        binding.backgroundSettingsLayout.addView(hideBackgroundOption);

        //自启动
        SettingClickableTextView autoStartOption = new SettingClickableTextView(requireContext());
        autoStartOption.setActions(
                R.string.auto_start_permission,
                "点击跳转自启动设置界面",
                R.drawable.baseline_autorenew_24,
                v -> PermissionHelper.requestAutoStartPermission(requireContext())
        );
        binding.backgroundSettingsLayout.addView(autoStartOption);

        //电池优化
        SettingClickableTextView batteryOptimizationOption = new SettingClickableTextView(requireContext());
        batteryOptimizationOption.setActions(
                R.string.battery_optimization,
                "点击跳转电池优化设置界面",
                R.drawable.baseline_battery_5_bar_24,
                v -> PermissionHelper.openBatteryOptimizations(requireContext())
        );
        binding.backgroundSettingsLayout.addView(batteryOptimizationOption);

        //关于软件
        SettingClickableTextView aboutOption = new SettingClickableTextView(requireContext());
        aboutOption.setActions(
                R.string.about_software,
                null,
                R.drawable.baseline_info_24,
                v -> AboutHelper.showAboutDialog(requireContext())
        );
        binding.aboutLayout.addView(aboutOption);

        //更新日志
        SettingClickableTextView updateLogOption = new SettingClickableTextView(requireContext());
        updateLogOption.setActions(
                R.string.update_log,
                null,
                R.drawable.baseline_update_24,
                v -> UpdateLogHelper.showUpdateLogDialog(requireContext())
        );
        binding.aboutLayout.addView(updateLogOption);
    }

    /**
     * 处理导出数据选项点击的方法
     */
    private void onExportDataClicked() {
        //获取选项名称和状态
        String[] type_names = Arrays.stream(IODataType.values())
                .map(IODataType::getName)
                .toArray(String[]::new);
        boolean[] itemStats = {true, true};

        //实例化自定义多选视图
        @SuppressLint("InflateParams") View mutiChoiceDialogView = getLayoutInflater().inflate(R.layout.view_multichoice, null);

        //设置适配器
        RecyclerView recyclerView = mutiChoiceDialogView.findViewById(R.id.item_recycler);
        MultiChoiceDialogAdapter adapter = new MultiChoiceDialogAdapter(
                itemStats,
                type_names,
                (position, isChecked) -> itemStats[position] = isChecked
        );
        recyclerView.setAdapter(adapter);

        //构建多选对话框
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择导出的数据")
                .setView(mutiChoiceDialogView)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();

        //设置对话框的显示监听器
        alertDialog.setOnShowListener(dialog -> {
            Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(view -> {
                //检测是否一个都没有选择
                boolean isNonItemChosen = true;
                for (boolean isChose : itemStats) {
                    if (isChose) {
                        isNonItemChosen = false;
                        break;
                    }
                }

                if (!isNonItemChosen) {
                    exportData(itemStats);
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                }
            });
        });
        alertDialog.show();
    }

    /**
     * 显示主题模式选择对话框
     */
    private void showThemeModeSelectDialog() {
        String[] themeModeStr = {"浅色模式", "深色模式", "跟随系统"};
        int theme_mode = ThemeModePreference.getThemeMode(requireContext());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("主题模式")
                .setSingleChoiceItems(themeModeStr, theme_mode, ((dialog, which) -> {
                    ThemeModeHelper.applyTheme(which);
                    ThemeModePreference.saveThemeMode(requireContext(), which);
                    dialog.dismiss();
                }))
                .setNegativeButton("关闭", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 显示导入数据选择对话框
     *
     * @param itemStats         可选项的初始状态
     * @param isItemEnabled     可选项是否启用
     * @param choiceItems       选项名称数组
     * @param effectiveFileList 能够解析的JSON文件列表
     */
    private void showImportItemChoiceDialog(
            boolean[] itemStats,
            boolean[] isItemEnabled,
            String[] choiceItems,
            List<File> effectiveFileList) {
        //实例化自定义对话框视图
        @SuppressLint("InflateParams") View mutiChoiceDialogView = getLayoutInflater().inflate(R.layout.view_multichoice, null);

        //设置适配器
        RecyclerView recyclerView = mutiChoiceDialogView.findViewById(R.id.item_recycler);
        MultiChoiceDialogAdapter adapter = new MultiChoiceDialogAdapter(
                isItemEnabled,
                itemStats,
                choiceItems,
                (position, isChecked) -> itemStats[position] = isChecked
        );
        recyclerView.setAdapter(adapter);

        //构建对话框实例
        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择需要导入的数据")
                .setView(mutiChoiceDialogView)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", null)
                .create();

        //设置对话框的显示监听器
        alertDialog.setOnShowListener(dialog -> {
            Button positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                //检测是否一个都没有选择
                boolean isNonItemChosen = true;
                for (boolean isChose : itemStats) {
                    if (isChose) {
                        isNonItemChosen = false;
                        break;
                    }
                }

                if (!isNonItemChosen) {
                    Log.i(LogTags.SETTING_FRAGMENT.getV(), "用户选择需要导入的数据并确认进行下一步");
                    onImportConfirmed(itemStats, effectiveFileList);
                    dialog.dismiss();   //仅当满足要求时才关闭
                } else {
                    Toast.makeText(requireContext(), "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                }
            });
        });

        //设置对话框隐藏监听
        alertDialog.setOnDismissListener(dialog -> {
            Log.i(LogTags.SETTING_FRAGMENT.getV(), "对话框关闭");
            safFileHelper.clearTempFile();
        });
        alertDialog.show();
    }

    /**
     * 数据导入确认后触发的方法
     *
     * @param itemStats         多选对话框的选择状况
     * @param effectiveFileList 能够解析内容的有效文件列表
     */
    private void onImportConfirmed(@NonNull boolean[] itemStats, @NonNull List<File> effectiveFileList) {
        boolean isImportSuccessfully = false;

        boolean isAccountDataChecked = itemStats[IODataType.ACCOUNT_DATA.ordinal()];   //流水记录文件是否勾选
        boolean isRuleDataChecked = itemStats[IODataType.RULE_DATA.ordinal()];         //通知解析规则文件是否勾选
        for (File file : effectiveFileList) {
            String file_name = file.getName();

            DataHelperBase<BookKeepingDatabaseHelper, ?> dataHelperBase;
            if (file_name.equals(IODataType.ACCOUNT_DATA.getDefault_file_name()) && isAccountDataChecked) {
                dataHelperBase = new RunningAccountDataHelper(requireContext());
            } else if (file_name.equals(IODataType.RULE_DATA.getDefault_file_name()) && isRuleDataChecked) {
                //isAccountDataChecked：当同时导入了流水账数据时才写入tag_no属性
                dataHelperBase = new AnalysisRuleDataHelper(requireContext(), isAccountDataChecked);
            } else {
                continue;
            }

            //将数据保存至数据库
            Log.i(LogTags.SETTING_FRAGMENT.getV(), String.format(Locale.getDefault(), "正在尝试读取临时文件%s", file_name));
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                isImportSuccessfully = dataHelperBase.saveJsonDataToDb(content.toString()) || isImportSuccessfully;
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "临时文件读取失败，请重试", Toast.LENGTH_SHORT).show();
                Log.e(LogTags.SETTING_FRAGMENT.getV(), "临时文件读取失败");
                return;
            }
        }

        if (isImportSuccessfully) {
            Log.i(LogTags.SETTING_FRAGMENT.getV(), "该文件的数据已成功导入");
            Toast.makeText(requireContext(), "数据导入成功", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(LogTags.SETTING_FRAGMENT.getV(), "该文件的数据导入失败");
            Toast.makeText(requireContext(), "数据导入失败：无法解析文件内容", Toast.LENGTH_SHORT).show();
        }
    }
}