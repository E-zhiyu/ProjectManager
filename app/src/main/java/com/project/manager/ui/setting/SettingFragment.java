package com.project.manager.ui.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
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
import com.project.manager.ui.setting.running_account_data.data_helpers.AnalysisRuleDataHelper;
import com.project.manager.ui.setting.running_account_data.data_helpers.DataHelperBase;
import com.project.manager.ui.setting.running_account_data.data_helpers.RunningAccountDataHelper;
import com.project.manager.data.data_save.preference.ThemeModePreference;
import com.project.manager.ui.setting.running_account_data.maps.TotalAccountDataMap;
import com.project.manager.ui.setting.running_account_data.maps.TotalRuleDataMap;

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
    enum EIDataType {
        ACCOUNT_DATA("流水记录数据", "RunningAccount.json"),
        RULE_DATA("通知解析规则数据", "AnalysisRule.json");
        private final String name;              //选项名称
        private final String default_file_name; //默认文件名称

        EIDataType(String name, String default_file_name) {
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
                    .map(EIDataType::getDefault_file_name)
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
        List<String> fileNameList = new ArrayList<>();      //用于导出数据的临时文件名列表
        List<String> fileContentList = new ArrayList<>();   //用于导出数据的临时文件内容列表

        //根据选择的内容创建临时文件
        if (choseItem[EIDataType.ACCOUNT_DATA.ordinal()]) {
            DataHelperBase<BookKeepingDatabaseHelper, TotalAccountDataMap> dataHelper = new RunningAccountDataHelper(requireContext());
            try {
                String json_str = dataHelper.getDataInJSON();
                fileNameList.add(EIDataType.ACCOUNT_DATA.getDefault_file_name());
                fileContentList.add(json_str);
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "JSON序列化时出错", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (choseItem[EIDataType.RULE_DATA.ordinal()]) {
            DataHelperBase<BookKeepingDatabaseHelper, TotalRuleDataMap> dataHelper = new AnalysisRuleDataHelper(requireContext());
            try {
                String json_str = dataHelper.getDataInJSON();
                fileNameList.add(EIDataType.RULE_DATA.getDefault_file_name());
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
        safFileHelper.openZipBySAF(
                new SAFFileHelper.ReadCallback() {
                    @Override
                    public void onZipUnpacked(List<File> tempJsonFileList) {
                        List<File> effectiveFileList = getEffectiveFileList(tempJsonFileList);

                        String[] type_names = Arrays.stream(EIDataType.values())
                                .map(EIDataType::getName)
                                .toArray(String[]::new);
                        boolean[] itemStats = {true, true};     //选项的选择状态
                        boolean[] isItemFound = {true, true};   //是否找到对应名称的文件

                        //根据解压的临时JSON文件决定应该禁用哪些选项
                        for (EIDataType eiDataType : EIDataType.values()) {
                            boolean isFound = false;
                            for (File tempFile : tempJsonFileList) {
                                if (tempFile.getName().equals(eiDataType.getDefault_file_name())) {
                                    isFound = true;
                                    break;
                                }
                            }

                            if (!isFound) {
                                int index = eiDataType.ordinal();
                                String type_name = eiDataType.getName();
                                String disabled_name = String.format(Locale.getDefault(), "%s(未包含)", type_name);
                                type_names[index] = disabled_name;
                                itemStats[index] = false;
                                isItemFound[index] = false;
                            }
                        }

                        if (!isItemFound[0] && isItemFound[1]) {
                            Toast.makeText(requireContext(), "请选择正确的备份文件", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //实例化自定义对话框视图
                        @SuppressLint("InflateParams") View mutiChoiceDialogView = getLayoutInflater().inflate(R.layout.view_multichoice, null);

                        //构建对话框实例
                        AlertDialog alertDialog = new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("选择需要导入的数据")
                                .setView(mutiChoiceDialogView)
                                .setNegativeButton("取消", (dialog, which) -> {
                                    dialog.dismiss();
                                    safFileHelper.clearTempFile();
                                })
                                .setPositiveButton("确定", (dialog, which) -> {

                                })
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
                                    onImportConfirmed(itemStats, effectiveFileList);
                                    dialog.dismiss();   //仅当满足要求时才关闭
                                } else {
                                    Toast.makeText(requireContext(), "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                                }

                                safFileHelper.clearTempFile();  //数据处理完成后清空临时文件
                            });
                        });
                        alertDialog.show();

                        //设置适配器
                        RecyclerView recyclerView = mutiChoiceDialogView.findViewById(R.id.item_recycler);
                        MultiChoiceDialogAdapter adapter = new MultiChoiceDialogAdapter(
                                requireContext(),
                                isItemFound,
                                itemStats,
                                type_names,
                                (position, isChecked) -> itemStats[position] = isChecked
                        );
                        recyclerView.setAdapter(adapter);
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
        List<String> effectiveFileNameList = EIDataType.getEffectiveFileNameList();
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
        //关于软件
        binding.settingAbout.setOnClickListener(v -> AboutHelper.showAboutDialog(requireContext()));

        //主题模式
        binding.settingThemeMode.setOnClickListener(v -> showThemeModeSelectDialog());

        //导出数据
        binding.settingExportRunningAccount.setOnClickListener(v -> {
            String[] type_names = Arrays.stream(EIDataType.values())
                    .map(EIDataType::getName)
                    .toArray(String[]::new);
            boolean[] choseItem = {true, true};

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("选择导出的数据")
                    .setMultiChoiceItems(
                            type_names,
                            choseItem,
                            (dialog, which, isChecked) -> choseItem[which] = isChecked)
                    .setPositiveButton("确定", (dialog, which) -> {
                        //检测是否一个都没有选择
                        boolean isNonItemChosen = true;
                        for (boolean isChose : choseItem) {
                            if (isChose) {
                                isNonItemChosen = false;
                                break;
                            }
                        }

                        if (!isNonItemChosen) {
                            exportData(choseItem);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "请选择至少一个选项", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        //导入数据
        binding.settingImportRunningAccount.setOnClickListener(v -> importData());

        //清空流水数据
        binding.settingClearRunningAccount.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
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

        //更新日志
        binding.settingUpdateLog.setOnClickListener(v -> UpdateLogHelper.showUpdateLogDialog(requireContext()));

        //管理通知解析规则
        binding.settingNotificationAnalysisRules.setOnClickListener(v -> {
            Intent skip2NotificationRulesActivity = new Intent(requireContext(), AnalysisRuleManageActivity.class);
            startActivity(skip2NotificationRulesActivity);
        });

        //自启动
        binding.autoStartPermission.setOnClickListener(v -> PermissionHelper.requestAutoStartPermission(requireContext()));

        //电池优化
        binding.batteryOptimization.setOnClickListener(v -> PermissionHelper.openBatteryOptimizations(requireContext()));

        //规则重置
        binding.ruleReset.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("重置规则")
                .setMessage("此操作将重置通知解析规则为默认规则，确认继续吗？")
                .setPositiveButton("确认", ((dialog, which) -> {
                    dialog.dismiss();
                    AnalysisRuleDataHelper.resetRule(requireContext());
                }))
                .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                .show()
        );

        //完成通知解析开关状态初始化
        MaterialSwitch notification_analysis_switch = binding.notificationAnalysisSwitch;
        LinearLayout notification_analysis_layout = binding.notificationAnalysisOptionLayout;
        boolean isNotificationAnalysisOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(requireContext());
        if (isNotificationAnalysisOpened && PermissionHelper.isNotificationServiceEnabled(requireContext())) {
            notification_analysis_layout.setVisibility(View.VISIBLE);
            notification_analysis_switch.setChecked(true);
        } else {
            notification_analysis_layout.setVisibility(View.GONE);
            notification_analysis_switch.setChecked(false);

            //考虑到无授权情况下自动关闭通知解析功能
            AutoBookKeepingPreference.setNotificationAnalysisOpened(false, requireContext());
        }

        //设置通知解析开关按钮的监听器
        notification_analysis_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AutoBookKeepingPreference.setNotificationAnalysisOpened(isChecked, requireActivity());  //将打开状态写入文件

            //开启开关时检测是否没有权限，如果没有则提示用户授权
            if (!PermissionHelper.isNotificationServiceEnabled(requireContext()) && isChecked) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("权限申请说明")
                        .setMessage("此功能需要使用“通知使用权”权限，该权限允许应用读取其他软件发送的通知内容。本应用不会也无法使用该权限获取用户隐私信息，仅用于解析通知中可能出现的流水账信息，请您放心使用。\n是否为本应用授权？")
                        .setPositiveButton("确认", (dialog, which) -> {
                            dialog.dismiss();
                            PermissionHelper.requestNotificationPermission(requireContext());
                            AnimationHelper.switchViewFoldOrExpanded(true, notification_analysis_layout);  //切换通知解析选项布局的可见性

                            //发送功能开关变更广播
                            Intent functionSwitched = new Intent(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString());
                            requireContext().sendBroadcast(functionSwitched);
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            notification_analysis_switch.setChecked(false);
                            dialog.dismiss();
                        })
                        .show();
            } else {
                //发送功能开关变更广播
                Intent functionSwitched = new Intent(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString());
                requireContext().sendBroadcast(functionSwitched);
                AnimationHelper.switchViewFoldOrExpanded(isChecked, notification_analysis_layout);  //切换通知解析选项布局的可见性
            }
        });

        //最近任务隐藏开关的初始化
        MaterialSwitch hide_recents_switch = binding.hideRecentsSwitch;
        hide_recents_switch.setChecked(KeepAlivePreference.getHideRecents(requireContext()));

        //设置最近任务隐藏开关的监听器
        hide_recents_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            KeepAlivePreference.setHideRecents(isChecked, requireContext());

            if (isChecked) {
                Toast.makeText(requireContext(), "建议额外在最近任务中锁定本应用", Toast.LENGTH_SHORT).show();
            }
        });
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
     * 数据导入确认后触发的方法
     *
     * @param chosenItem        多选对话框的选择状况
     * @param effectiveFileList 能够解析内容的有效文件列表
     */
    private void onImportConfirmed(boolean[] chosenItem, @NonNull List<File> effectiveFileList) {
        boolean isImportSuccessfully = false;

        for (File file : effectiveFileList) {
            String file_name = file.getName();

            DataHelperBase<BookKeepingDatabaseHelper, ?> dataHelperBase;
            if (file_name.equals(EIDataType.ACCOUNT_DATA.getDefault_file_name()) && chosenItem[0]) {
                dataHelperBase = new RunningAccountDataHelper(requireContext());
            } else if (file_name.equals(EIDataType.RULE_DATA.getDefault_file_name()) && chosenItem[1]) {
                dataHelperBase = new AnalysisRuleDataHelper(requireContext());
            } else {
                continue;
            }

            //将数据保存至数据库
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                isImportSuccessfully = dataHelperBase.saveJsonDataToDb(content.toString()) || isImportSuccessfully;
            } catch (IOException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                return;
            }
        }

        if (isImportSuccessfully) {
            Toast.makeText(requireContext(), "数据导入成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "数据导入失败：无法解析文件内容", Toast.LENGTH_SHORT).show();
        }
    }
}