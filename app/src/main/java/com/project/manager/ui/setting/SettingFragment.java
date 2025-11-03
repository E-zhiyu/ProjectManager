package com.project.manager.ui.setting;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.project.manager.R;
import com.project.manager.databinding.FragmentSettingBinding;

public class SettingFragment extends Fragment implements View.OnClickListener {

    private FragmentSettingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
        }
    }

    //初始化视图
    private void initViews() {
        binding.settingAbout.setOnClickListener(this);  //关于软件视图
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
}