package com.project.manager.ui.setting;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
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

import java.util.Calendar;
import java.util.Locale;


public class SettingFragment extends Fragment implements View.OnClickListener {
    private FragmentSettingBinding binding;
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //活动启动器
    private SAFFileHelper safFileHelper;    //SAF文件帮助器

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews();
        initActivityLaunchers();

        safFileHelper = new SAFFileHelper(requireContext());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v == binding.settingClearRunningAccount) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("清除数据")
                    .setMessage("此操作将清除所有流水账数据，确认继续吗？")
                    .setPositiveButton("确认", ((dialog, which) -> {
                        dialog.dismiss();
                        RunningAccountDataHelper.deleteAllData(requireContext());
                        BookKeepingStartDatePreference.saveStartDate("", requireContext()); //清空已保存的开始记账的日期
                    }))
                    .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                    .show();
        } else if (v == binding.ruleReset) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("重置规则")
                    .setMessage("此操作将重置通知解析规则为默认规则，确认继续吗？")
                    .setPositiveButton("确认", ((dialog, which) -> {
                        dialog.dismiss();
                        AnalysisRuleDataHelper.resetRule(requireContext());
                    }))
                    .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                    .show();
        }
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
     *
     * @param json_str          需要写入文件的JSON字符串
     * @param default_file_head 默认的文件名开头
     */
    private void exportData(String json_str, String default_file_head) {
        //获取当前日期和时间并生成默认文件名
        Calendar calendar = Calendar.getInstance();
        String now_date = String.format(
                Locale.getDefault(),
                "%04d%02d%02d(%02d%02d%02d)",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
        );
        String default_filename = String.format("%s_%s.json", default_file_head, now_date);

        //启动系统文件选择器(SAF)
        safFileHelper.createFileWithContent(
                new SAFFileHelper.SAFFileWriteCallback() {
                    @Override
                    public void onFileWrote() {
                        Toast.makeText(requireContext(), "导出成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errMessage) {
                        Toast.makeText(requireContext(), "导出失败：" + errMessage, Toast.LENGTH_SHORT).show();
                    }
                },
                "application/json",
                default_filename,
                json_str,
                exportDataLauncher
        );
    }

    /**
     * 从文件导入数据
     *
     * @param dataHelper 数据帮助器
     */
    private void importData(DataHelperBase<? extends SQLiteOpenHelper, ?> dataHelper) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("导入数据")
                .setMessage("导入的数据将覆盖现有的数据，确定继续吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    dialog.dismiss();
                    safFileHelper.openFileAndReadContent(
                            new SAFFileHelper.SAFFileReadCallback() {
                                @Override
                                public void onFileRead(String content) {
                                    dataHelper.saveJsonDataToDb(content);
                                }

                                @Override
                                public void onError(String errMessage) {
                                    Toast.makeText(requireContext(), "导入失败：" + errMessage, Toast.LENGTH_SHORT).show();
                                }
                            },
                            "application/json",
                            importDataLauncher
                    );
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //初始化视图
    private void initViews() {
        binding.settingAbout.setOnClickListener(v -> AboutHelper.showAboutDialog(requireContext()));
        binding.settingThemeMode.setOnClickListener(v -> showThemeModeSelectDialog());
        binding.settingExportRunningAccount.setOnClickListener(v -> {
            DataHelperBase<BookKeepingDatabaseHelper, TotalAccountDataMap> dataHelper = new RunningAccountDataHelper(requireContext());
            try {
                String json_str = dataHelper.getDataInJSON();
                exportData(json_str, "RunningAccount");
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "JSON序列化时出错", Toast.LENGTH_SHORT).show();
            }
        });
        binding.settingImportRunningAccount.setOnClickListener(v -> {
            DataHelperBase<BookKeepingDatabaseHelper, TotalAccountDataMap> dataHelper = new RunningAccountDataHelper(requireContext());
            importData(dataHelper);
        });
        binding.settingClearRunningAccount.setOnClickListener(this);
        binding.settingUpdateLog.setOnClickListener(v -> UpdateLogHelper.showUpdateLogDialog(requireContext()));
        binding.settingNotificationAnalysisRules.setOnClickListener(v -> {
            Intent skip2NotificationRulesActivity = new Intent(requireContext(), AnalysisRuleManageActivity.class);
            startActivity(skip2NotificationRulesActivity);
        });
        binding.autoStartPermission.setOnClickListener(v -> PermissionHelper.requestAutoStartPermission(requireContext()));
        binding.batteryOptimization.setOnClickListener(v -> PermissionHelper.openBatteryOptimizations(requireContext()));
        binding.ruleExport.setOnClickListener(v -> {
            DataHelperBase<BookKeepingDatabaseHelper, TotalRuleDataMap> dataHelper = new AnalysisRuleDataHelper(requireContext());
            try {
                String json_str = dataHelper.getDataInJSON();
                exportData(json_str, "AnalysisRule");
            } catch (JsonProcessingException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                Toast.makeText(requireContext(), "JSON序列化时出错", Toast.LENGTH_SHORT).show();
            }
        });
        binding.ruleImport.setOnClickListener(v -> {
            DataHelperBase<BookKeepingDatabaseHelper, TotalRuleDataMap> dataHelper = new AnalysisRuleDataHelper(requireContext());
            importData(dataHelper);
        });
        binding.ruleReset.setOnClickListener(this);

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

        //完成最近任务隐藏开关的初始化
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

    //显示主题模式选择对话框
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
}