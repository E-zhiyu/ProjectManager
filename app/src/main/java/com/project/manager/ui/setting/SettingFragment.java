package com.project.manager.ui.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.project.manager.RequestResultCode;
import com.project.manager.databinding.FragmentSettingBinding;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.setting.flow_data.FlowDataHelper;
import com.project.manager.ui.setting.flow_data.pojo.TotalDataMap;
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
    public void onClick(View v) {
        if (v.getId() == R.id.setting_about) {
            showAboutDialog();
        } else if (v.getId() == R.id.setting_theme_mode) {
            showThemeModeSelectDialog();
        } else if (v.getId() == R.id.setting_export_flow) {
            exportFlowData();
        } else if (v.getId() == R.id.setting_import_flow) {
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
        } else if (v.getId() == R.id.setting_clear_flow) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("清除数据")
                    .setMessage("此操作将清除所有流水账数据，确认执行吗？")
                    .setPositiveButton("确认", ((dialog, which) -> {
                        FlowDataHelper.deleteAllData(requireContext());
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
                            FlowDataHelper.writeJsonToFile(uri, json_str, requireContext());
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
                            FlowDataHelper.readFileAndSave(uri, requireContext());
                        } else {
                            NullPointerException e = new NullPointerException("无法导入数据");
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                        }
                    }
                }
        );
    }

    //导出流水账数据
    private void exportFlowData() {
        try {
            //获取所有数据并序列化为JSON字符串
            FlowDataHelper dataHelper = new FlowDataHelper(requireContext());
            ObjectMapper mapper = new ObjectMapper();
            TotalDataMap totalDataMap = dataHelper.getAllDataInMap();
            json_str = mapper.writeValueAsString(totalDataMap);

            //获取当前日期并生成默认文件名
            Calendar calendar = Calendar.getInstance();
            @SuppressLint("DefaultLocale") String now_date = String.format(
                    "%04d-%02d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            String default_filename = String.format("%s_FlowData.json", now_date);

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
        binding.settingExportFlow.setOnClickListener(this);
        binding.settingImportFlow.setOnClickListener(this);
        binding.settingClearFlow.setOnClickListener(this);
    }

    //获取版本名称
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "未知版本";
        }
    }

    //获取应用名称
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "未知应用名"; // 返回默认值或处理异常
        }
    }

    //显示关于软件对话框
    private void showAboutDialog() {
        String version_name = "v" + getVersionName(requireContext());
        String app_name = getAppName(requireContext());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(app_name + " " + version_name)
                .setMessage("这是一个项目管理工具，旨在帮助用户便捷地管理工程项目")
                .setPositiveButton("确定", ((dialog, which) -> dialog.dismiss()))
                .show();
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
                .setNegativeButton("确定", ((dialog, which) -> dialog.dismiss()))
                .show();
    }
}