package com.project.manager.ui.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
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
        Dialog about_dialog = new Dialog(requireContext());
        about_dialog.setContentView(R.layout.dialog_about);

        MaterialTextView version_name_view = about_dialog.findViewById(R.id.version_name_view);
        version_name_view.setText(version_name);
        MaterialTextView app_name_view = about_dialog.findViewById(R.id.app_name_view);
        app_name_view.setText(app_name);

        //设置对话框高度和宽度
        Window dialog_window = about_dialog.getWindow();
        if (dialog_window != null) {
            WindowManager.LayoutParams params = dialog_window.getAttributes();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        }

        about_dialog.show();
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