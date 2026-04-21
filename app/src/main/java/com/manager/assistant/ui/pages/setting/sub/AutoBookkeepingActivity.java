package com.manager.assistant.ui.pages.setting.sub;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.RecentTaskManager;
import com.manager.assistant.automation.broadcast.BroadcastActions;
import com.manager.assistant.data.io.helpers.AnalysisRuleDataHelper;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.databinding.ActivityAutoBookkeepingBinding;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;
import com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit.AnalysisRuleManageActivity;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingClickableTextView;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingOptionViewBase;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingSpinnerView;
import com.manager.assistant.ui.pages.setting.setting_option_views.SettingSwitchView;

public class AutoBookkeepingActivity extends AppCompatActivity {
    private ActivityAutoBookkeepingBinding binding; //绑定的XML布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAutoBookkeepingBinding.inflate(getLayoutInflater());

        //设置边距
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.scrollView.setPadding(
                    0,
                    0,
                    0,
                    systemBars.bottom + ViewEdgeHelper.dpToPx(this, 15)
            );
            return insets;
        });

        initAutoBookkeepingSettings();
    }

    /**
     * 初始化自动记账设置项
     */
    private void initAutoBookkeepingSettings() {
        //通知解析自动记账
        SettingSwitchView notificationAnalysisSwitchOption = new SettingSwitchView(
                this,
                binding.notificationAnalysisSwitchOption,
                R.string.notification_analysis_mode,
                "解析通知实现自动记账",
                R.drawable.outline_notifications_active_24,
                SettingOptionViewBase.RadiusStyle.TOP
        );
        notificationAnalysisSwitchOption.setDividerVisibility(true);
        boolean isNotificationAnalysisOpened = AutoBookKeepingPreference.getSwitchStat(this);
        if (isNotificationAnalysisOpened && PermissionHelper.isNotificationServiceEnabled(this)) {
            notificationAnalysisSwitchOption.setChecked(true);
        } else {
            notificationAnalysisSwitchOption.setChecked(false);

            //考虑到无授权情况下自动关闭通知解析功能
            AutoBookKeepingPreference.setSwitchStat(false, this);
        }
        notificationAnalysisSwitchOption.setFunctionListener(
                (buttonView, isChecked) -> onNotificationAnalysisSwitchChanged(notificationAnalysisSwitchOption, isChecked)
        );

        //开关左侧文本长按功能
        notificationAnalysisSwitchOption.setOnLongClickListener(v -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            RecentTaskManager.startExternalActivity(this, intent);
            return true;
        });

        //通知解析规则管理
        SettingClickableTextView ruleManageOption = new SettingClickableTextView(
                this,
                binding.ruleManageOption,
                R.string.notification_analysis_rules_manage,
                "点击进入规则管理界面",
                R.drawable.baseline_rule_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
        );
        ruleManageOption.setFunctionListener(
                v -> {
                    Intent intent = new Intent(this, AnalysisRuleManageActivity.class);
                    RecentTaskManager.startExternalActivity(this, intent);
                }
        );

        //规则重置
        SettingClickableTextView resetRuleOption = new SettingClickableTextView(
                this,
                binding.resetRuleOption,
                R.string.reset_rule,
                "将现有规则重置为默认状态",
                R.drawable.outline_reset_settings_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
        );
        resetRuleOption.setFunctionListener(
                v -> new MaterialAlertDialogBuilder(this)
                        .setTitle("重置规则")
                        .setMessage("此操作将删除现有的规则并替换为默认规则，确认继续吗？")
                        .setPositiveButton("确认", (dialog, which) -> {
                            dialog.dismiss();
                            AnalysisRuleDataHelper.resetRule(this);
                        })
                        .setNegativeButton("取消", null)
                        .show()
        );

        //通知取消行为
        SettingSpinnerView notificationCancelBehaviour = new SettingSpinnerView(
                this,
                binding.notificationCancelBehaviour,
                R.string.notification_cancel_behaviour,
                "划走确认通知后执行的操作",
                R.drawable.outline_comments_disabled_24,
                SettingOptionViewBase.RadiusStyle.MIDDLE
        );
        int[] cancelTitleResId = {
                R.string.keep_account,
                R.string.delete_account
        };
        int cancelBehaviourCode = AutoBookKeepingPreference.getNotificationCancelBehaviour(this);
        notificationCancelBehaviour.setSpinnerText(cancelTitleResId[cancelBehaviourCode]);
        notificationCancelBehaviour.setFunctionListener(v -> {
            PopupMenu behaviourMenu = new PopupMenu(this, notificationCancelBehaviour.getFunctionComponent());
            behaviourMenu.getMenuInflater().inflate(R.menu.popup_menu_notification_cancel_behaviour, behaviourMenu.getMenu());

            behaviourMenu.setOnMenuItemClickListener(item -> {
                boolean isItemClicked = false;
                if (item.getItemId() == R.id.action_keep) {
                    AutoBookKeepingPreference.setNotificationCancelBehaviour(0, this);
                    notificationCancelBehaviour.setSpinnerText(cancelTitleResId[0]);
                    isItemClicked = true;
                } else if (item.getItemId() == R.id.action_delete) {
                    AutoBookKeepingPreference.setNotificationCancelBehaviour(1, this);
                    notificationCancelBehaviour.setSpinnerText(cancelTitleResId[1]);
                    isItemClicked = true;
                }

                return isItemClicked;
            });

            behaviourMenu.show();
        });

        //通知点击行为
        SettingSpinnerView notificationClickBehaviour = new SettingSpinnerView(
                this,
                binding.notificationClickBehaviour,
                R.string.notification_click_behaviour,
                "点击确认通知后执行的操作",
                R.drawable.outline_ads_click_24,
                SettingOptionViewBase.RadiusStyle.BOTTOM
        );
        int[] clickTitleResId = {
                R.string.none,
                R.string.keep_account,
                R.string.delete_account
        };
        int clickBehaviourCode = AutoBookKeepingPreference.getNotificationClickBehaviour(this);
        notificationClickBehaviour.setSpinnerText(clickTitleResId[clickBehaviourCode]);
        notificationClickBehaviour.setFunctionListener(v -> {
            PopupMenu behaviourMenu = new PopupMenu(this, notificationClickBehaviour.getFunctionComponent());
            behaviourMenu.getMenuInflater().inflate(R.menu.popup_menu_notification_click_behaviour, behaviourMenu.getMenu());

            behaviourMenu.setOnMenuItemClickListener(item -> {
                boolean isItemClicked = false;
                if (item.getItemId() == R.id.action_none) {
                    AutoBookKeepingPreference.setNotificationClickBehaviour(0, this);
                    notificationClickBehaviour.setSpinnerText(clickTitleResId[0]);
                    isItemClicked = true;
                } else if (item.getItemId() == R.id.action_keep) {
                    AutoBookKeepingPreference.setNotificationClickBehaviour(1, this);
                    notificationClickBehaviour.setSpinnerText(clickTitleResId[1]);
                    isItemClicked = true;
                } else if (item.getItemId() == R.id.action_delete) {
                    AutoBookKeepingPreference.setNotificationClickBehaviour(2, this);
                    notificationClickBehaviour.setSpinnerText(clickTitleResId[2]);
                    isItemClicked = true;
                }

                return isItemClicked;
            });

            behaviourMenu.show();
        });
    }

    /**
     * 通知解析开关状态变更调用的方法
     *
     * @param switchView 开关视图
     * @param isChecked  开关状态
     */
    private void onNotificationAnalysisSwitchChanged(
            SettingSwitchView switchView,
            boolean isChecked
    ) {
        AutoBookKeepingPreference.setSwitchStat(isChecked, this);   //将打开状态写入文件

        //开启开关时检测是否没有权限，如果没有则提示用户授权
        if (!PermissionHelper.isNotificationServiceEnabled(this) && isChecked) {
            switchView.setChecked(false);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("权限申请说明")
                    .setMessage("此功能需要使用“通知使用权”权限，该权限允许应用读取其他软件发送的通知内容。本应用不会也无法使用该权限获取用户隐私信息，仅用于解析通知中可能出现的流水账信息，请您放心使用。\n\n是否为本应用授权？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        //申请通知监听权限
                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        RecentTaskManager.startExternalActivity(this, intent);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            //发送功能开关变更广播
            Intent functionSwitched = new Intent(BroadcastActions.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString());
            sendBroadcast(functionSwitched);
        }
    }
}