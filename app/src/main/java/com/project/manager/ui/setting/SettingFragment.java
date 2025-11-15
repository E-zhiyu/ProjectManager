package com.project.manager.ui.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.project.manager.R;
import com.project.manager.databinding.FragmentSettingBinding;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.setting.running_account_data.RunningAccountDataHelper;
import com.project.manager.ui.setting.running_account_data.pojo.TotalDataMap;
import com.project.manager.ui.setting.theme_mode.ThemeModeHelper;
import com.project.manager.ui.setting.theme_mode.ThemePreference;

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
    }

    //显示主题模式选择对话框
    private void showThemeModeSelectDialog() {
        String[] themeModeStr = {"浅色模式", "深色模式", "跟随系统"};
        int theme_mode = ThemePreference.getThemeMode(requireContext());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("主题模式")
                .setSingleChoiceItems(themeModeStr, theme_mode, ((dialog, which) -> {
                    ThemeModeHelper.applyTheme(which);
                    ThemePreference.saveThemeMode(requireContext(), which);
                    dialog.dismiss();
                }))
                .setNegativeButton("关闭", (dialog, which) -> dialog.dismiss())
                .show();
    }
}