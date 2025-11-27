package com.project.manager.ui.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.project.manager.R;
import com.project.manager.databinding.FragmentSettingBinding;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.preference.AutoBookKeepingPreference;
import com.project.manager.preference.BookKeepingStartDatePreference;
import com.project.manager.ui.animation.AnimationHelper;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.rule_edit.AnalysisRuleActivity;
import com.project.manager.ui.setting.helpers.AboutHelper;
import com.project.manager.ui.setting.helpers.ThemeModeHelper;
import com.project.manager.ui.setting.helpers.UpdateLogHelper;
import com.project.manager.ui.setting.running_account_data.RunningAccountDataHelper;
import com.project.manager.ui.setting.running_account_data.pojo.TotalDataMap;
import com.project.manager.preference.ThemeModePreference;

import java.util.Calendar;

public class SettingFragment extends Fragment implements View.OnClickListener {
    private FragmentSettingBinding binding;
    private String json_str; //导出数据时序列化的JSON字符串
    private ActivityResultLauncher<Intent> importDataLauncher, exportDataLauncher;  //活动启动器

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initActivityLauncher();
        initViews();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.setting_about) {
            AboutHelper.showAboutDialog(requireContext());
        } else if (v.getId() == R.id.setting_theme_mode) {
            showThemeModeSelectDialog();
        } else if (v.getId() == R.id.setting_export_running_account) {
            exportRunningAccountData();
        } else if (v.getId() == R.id.setting_import_running_account) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("导入数据")
                    .setMessage("新数据将覆盖原有的数据，确认继续吗？")
                    .setPositiveButton("确认", ((dialog, which) -> {
                        Intent startSAF = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        startSAF.addCategory(Intent.CATEGORY_OPENABLE);
                        startSAF.setType("application/json"); //允许json文件类型
                        importDataLauncher.launch(startSAF);
                        dialog.dismiss();
                    }))
                    .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                    .show();
        } else if (v.getId() == R.id.setting_clear_running_account) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("清除数据")
                    .setMessage("此操作将清除所有流水账数据，确认执行吗？")
                    .setPositiveButton("确认", ((dialog, which) -> {
                        RunningAccountDataHelper.deleteAllData(requireContext());
                        dialog.dismiss();
                    }))
                    .setNegativeButton("取消", ((dialog, which) -> dialog.dismiss()))
                    .show();
        } else if (v.getId() == R.id.setting_update_log) {
            UpdateLogHelper.showUpdateLogDialog(requireContext());
        } else if (v.getId() == R.id.setting_notification_analysis_rules) {
            Intent skip2NotificationRulesActivity = new Intent(getActivity(), AnalysisRuleActivity.class);
            startActivity(skip2NotificationRulesActivity);
        } else {
            RuntimeException e = new RuntimeException("无法获取正确的视图ID");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
        }
    }

    //初始化活动启动器
    private void initActivityLauncher() {
        exportDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            Uri uri = data.getData();
                            RunningAccountDataHelper.writeJsonToFile(uri, json_str, requireContext());
                        } else {
                            NullPointerException e = new NullPointerException("无法导出数据");
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                        }
                    }
                }
        );

        importDataLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            Uri uri = data.getData();
                            RunningAccountDataHelper.readFileAndSave(uri, requireContext());

                            //清空已保存的开始记账的日期
                            BookKeepingStartDatePreference.saveStartDate("", requireContext());
                        } else {
                            NullPointerException e = new NullPointerException("无法导入数据");
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                        }
                    }
                }
        );
    }

    //导出流水账数据
    private void exportRunningAccountData() {
        try {
            //获取所有数据并序列化为JSON字符串
            RunningAccountDataHelper dataHelper = new RunningAccountDataHelper(requireContext());
            ObjectMapper mapper = new ObjectMapper();
            TotalDataMap totalDataMap = dataHelper.getAllDataInMap();
            json_str = mapper.writeValueAsString(totalDataMap);

            //获取当前日期并生成默认文件名
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("DefaultLocale") String now_date = String.format(
                    "%04d%02d%02d(%02d%02d%02d)",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)
            );
            String default_filename = String.format("RunningAccount_%s.json", now_date);

            //启动系统文件选择器(SAF)
            Intent startSAF = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            startSAF.addCategory(Intent.CATEGORY_OPENABLE);
            startSAF.setType("application/json");
            startSAF.putExtra(Intent.EXTRA_TITLE, default_filename);
            exportDataLauncher.launch(startSAF);
        } catch (JsonProcessingException e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
        }
    }

    //初始化视图
    private void initViews() {
        binding.settingAbout.setOnClickListener(this);
        binding.settingThemeMode.setOnClickListener(this);
        binding.settingExportRunningAccount.setOnClickListener(this);
        binding.settingImportRunningAccount.setOnClickListener(this);
        binding.settingClearRunningAccount.setOnClickListener(this);
        binding.settingUpdateLog.setOnClickListener(this);
        binding.settingNotificationAnalysisRules.setOnClickListener(this);

        //完成开关状态初始化
        MaterialSwitch notification_analysis_switch = binding.notificationAnalysisSwitch;
        LinearLayout notification_analysis_layout = binding.notificationAnalysisOptionLayout;
        boolean isNotificationAnalysisOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(requireActivity());
        notification_analysis_switch.setChecked(isNotificationAnalysisOpened);
        if (isNotificationAnalysisOpened) {
            notification_analysis_layout.setVisibility(View.VISIBLE);
        } else {
            notification_analysis_layout.setVisibility(View.GONE);
        }

        //设置开关按钮的监听器
        notification_analysis_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AutoBookKeepingPreference.setNotificationAnalysisOpened(isChecked, requireActivity());  //将打开状态写入文件

            //临时改为可见
            int originVisibility = notification_analysis_layout.getVisibility();
            notification_analysis_layout.setVisibility(View.VISIBLE);

            // 测量视图
            int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            notification_analysis_layout.measure(widthSpec, heightSpec);
            int layout_height = notification_analysis_layout.getMeasuredHeight();   //获得测量的高度

            notification_analysis_layout.setVisibility(originVisibility);   //恢复原来的可见性

            if (isChecked) {
                notification_analysis_layout.setVisibility(View.VISIBLE);
                AnimationHelper.animateHeight(notification_analysis_layout, 0, layout_height, null);
            } else {
                AnimationHelper.animateHeight(notification_analysis_layout, layout_height, 0, () -> notification_analysis_layout.setVisibility(View.GONE));
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