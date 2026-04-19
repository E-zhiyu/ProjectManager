package com.manager.assistant.ui.pages.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.ManagerAssistant;
import com.manager.assistant.R;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.databinding.FragmentSettingBinding;
import com.manager.assistant.helpers.UpdateHelper;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingOptionViewBase;
import com.manager.assistant.ui.pages.setting.sub.AutoBookkeepingActivity;
import com.manager.assistant.ui.pages.setting.sub.DataManageActivity;
import com.manager.assistant.ui.pages.setting.sub.PermissionManageActivity;
import com.manager.assistant.helpers.about.AboutHelper;
import com.manager.assistant.helpers.appearence.ThemeModeHelper;
import com.manager.assistant.helpers.about.UpdateLogHelper;
import com.manager.assistant.data.save.preference.AppSettingsPreference;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingClickableTextView;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingSpinnerView;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingSwitchView;

import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class SettingFragment extends Fragment {
    private FragmentSettingBinding binding;                                         //绑定的XML视图
    private final CompositeDisposable disposables = new CompositeDisposable();      //多线程任务列表

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);

        initViews();

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
        //应用设置
        initAppSettings();

        //数据管理
        SettingClickableTextView dataManage = new SettingClickableTextView(
                requireContext(),
                binding.dataManageOption,
                R.string.data_manage,
                "点击进入数据管理设置界面",
                R.drawable.outline_database_24,
                SettingOptionViewBase.RadiusStyle.SINGLE
        );
        dataManage.setFunctionListener(view -> {
            Intent intent = new Intent(requireContext(), DataManageActivity.class);
            startActivity(intent);
        });

        //自动记账
        SettingClickableTextView autoBookkeeping = new SettingClickableTextView(
                requireContext(),
                binding.autoBookkeepingOption,
                R.string.auto_bookkeeping,
                "点击进入自动记账设置界面",
                R.drawable.outline_checkbook_24,
                SettingOptionViewBase.RadiusStyle.TOP
        );
        autoBookkeeping.setFunctionListener(view -> {
            Intent intent = new Intent(requireContext(), AutoBookkeepingActivity.class);
            startActivity(intent);
        });

        //最近任务隐藏
        SettingSwitchView hideRecentTask = new SettingSwitchView(
                requireContext(),
                binding.hideRecentTaskOption,
                R.string.hide_recent_task,
                "在最近任务列表中隐藏",
                R.drawable.outline_visibility_off_24,
                SettingOptionViewBase.RadiusStyle.BOTTOM
        );
        boolean isHidden = AutoBookKeepingPreference.getHideRecentTask(requireContext());
        hideRecentTask.setChecked(isHidden);
        hideRecentTask.setFunctionListener(
                (compoundButton, checked) ->
                        AutoBookKeepingPreference.setHideRecentTask(checked, requireContext())
        );

        //关于
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
                "切换深浅色模式",
                R.drawable.outline_dark_mode_24,
                SettingOptionViewBase.RadiusStyle.TOP
        );
        themeModeOption.setFunctionListener(v -> showThemeModeSelectDialog());

        //动态配色
        SettingSwitchView dynamicColorOption = new SettingSwitchView(
                requireContext(),
                binding.dynamicColorOption,
                R.string.dynamic_color,
                "将壁纸颜色作为APP主题色",
                R.drawable.outline_colorize_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
        );
        dynamicColorOption.setChecked(AppSettingsPreference.getDynamicColorStat(requireContext()));
        dynamicColorOption.setFunctionListener(
                (buttonView, isChecked) -> {
                    AppSettingsPreference.setDynamicColorStat(requireContext(), isChecked);

                    ManagerAssistant app = (ManagerAssistant) requireActivity().getApplication();
                    DynamicColorsOptions options;
                    if (isChecked) {
                        options = new DynamicColorsOptions.Builder()
                                .setThemeOverlay(R.style.Theme_ManagerAssistant_Dynamic)
                                .build();
                    } else {
                        options = new DynamicColorsOptions.Builder()
                                .setThemeOverlay(R.style.Theme_ManagerAssistant_Static)
                                .build();
                    }
                    DynamicColors.applyToActivitiesIfAvailable(app, options);
                    requireActivity().recreate();
                }
        );

        //首页选项
        SettingSpinnerView firstScreenOption = new SettingSpinnerView(
                requireContext(),
                binding.firstScreenOption,
                R.string.select_first_screen,
                "选择启动的第一屏",
                R.drawable.outline_mobile_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
        );
        String[] firstScreenTitles = {
                requireContext().getString(R.string.title_bookkeeping),
                requireContext().getString(R.string.title_home)
        };
        int screenCode = AppSettingsPreference.getFirstScreen(requireContext());
        firstScreenOption.setSpinnerText(firstScreenTitles[screenCode]);
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

        //权限管理
        SettingClickableTextView permissionsOption = new SettingClickableTextView(
                requireContext(),
                binding.permissionsOption,
                R.string.permissions_setting,
                "点击进入权限管理界面",
                R.drawable.outline_settings_24,
                SettingOptionViewBase.RadiusStyle.BOTTOM
        );
        permissionsOption.setFunctionListener(v -> {
            Intent skip2PermissionManage = new Intent(
                    requireContext(),
                    PermissionManageActivity.class
            );
            startActivity(skip2PermissionManage);
        });
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
                R.drawable.outline_info_24,
                SettingOptionViewBase.RadiusStyle.TOP
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
                R.drawable.outline_lab_profile_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
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
                R.drawable.outline_update_24,
                SettingOptionViewBase.RadiusStyle.BOTTOM
        );
        updateCheckOption.setFunctionListener(
                v -> {
                    Toast.makeText(requireContext(), "正在检查更新……", Toast.LENGTH_SHORT).show();
                    UpdateHelper.checkUpdate(requireContext(), disposables, true, true);
                }
        );
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
}