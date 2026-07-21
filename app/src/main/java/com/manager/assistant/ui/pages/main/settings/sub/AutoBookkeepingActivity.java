package com.manager.assistant.ui.pages.main.settings.sub;

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
import com.manager.assistant.LifecycleManager;
import com.manager.assistant.automation.broadcast.BroadcastActions;
import com.manager.assistant.auxiliary.enums.RadiusStyle;
import com.manager.assistant.data.io.helpers.AnalysisRuleDataHelper;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.databinding.ActivityAutoBookkeepingBinding;
import com.manager.assistant.auxiliary.enums.settings.NotificationCancelBehaviour;
import com.manager.assistant.auxiliary.enums.settings.NotificationClickBehaviour;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.ui.pages.notification_rule.NotificationRuleListActivity;
import com.manager.assistant.ui.pages.main.settings.components.SettingClickableTextView;
import com.manager.assistant.ui.pages.main.settings.components.SettingSpinnerView;
import com.manager.assistant.ui.pages.main.settings.components.SettingSwitchView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                    systemBars.bottom + AppearanceHelper.dpToPx(this, 15)
            );
            return insets;
        });

        initAutoBookkeepingSettings();
    }

    /**
     * 初始化自动记账设置项
     */
    private void initAutoBookkeepingSettings() {
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //通知解析自动记账
        SettingSwitchView notificationAnalysisSwitchOption = new SettingSwitchView(
                this,
                binding.notificationAnalysisSwitchOption,
                R.string.notification_analysis_mode,
                "解析通知实现自动记账",
                R.drawable.outline_notifications_active_24,
                RadiusStyle.TOP
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
            LifecycleManager.startExternalActivity(this, intent);
            return true;
        });

        //通知解析规则管理
        SettingClickableTextView ruleManageOption = new SettingClickableTextView(
                this,
                binding.ruleManageOption,
                R.string.notification_rule,
                "点击进入规则管理界面",
                R.drawable.baseline_rule_24,
                RadiusStyle.MIDDLE
        );
        ruleManageOption.setFunctionListener(
                v -> {
                    Intent intent = new Intent(this, NotificationRuleListActivity.class);
                    LifecycleManager.startExternalActivity(this, intent);
                }
        );

        //规则重置
        SettingClickableTextView resetRuleOption = new SettingClickableTextView(
                this,
                binding.resetRuleOption,
                R.string.reset_rule,
                "将现有规则重置为默认状态",
                R.drawable.outline_reset_settings_24,
                RadiusStyle.MIDDLE
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
                RadiusStyle.MIDDLE
        );
        int cancelBehaviourCode = AutoBookKeepingPreference.getNotificationCancelBehaviour(this);
        notificationCancelBehaviour.setSpinnerText(
                NotificationCancelBehaviour.values()[cancelBehaviourCode].getTitle()
        );
        notificationCancelBehaviour.setFunctionListener(v -> {
            PopupMenu behaviourMenu = new PopupMenu(this, notificationCancelBehaviour.getFunctionComponent());

            //填充选项
            for (NotificationCancelBehaviour behaviour : NotificationCancelBehaviour.values()) {
                int groupId = behaviour.getGroupId();
                int itemId = behaviour.getItemId();
                int order = behaviour.getOrder();
                String title = behaviour.getTitle();
                behaviourMenu.getMenu().add(groupId, itemId, order, title);
            }

            //设置监听
            behaviourMenu.setOnMenuItemClickListener(item -> {
                //获取选项编号列表
                List<Integer> itemIdList = Arrays.stream(NotificationCancelBehaviour.values())
                        .map(NotificationCancelBehaviour::getItemId)
                        .collect(Collectors.toList());

                //判断是否选中
                if (itemIdList.contains(item.getItemId())) {
                    int index = itemIdList.indexOf(item.getItemId());
                    AutoBookKeepingPreference.setNotificationCancelBehaviour(this, index);
                    notificationCancelBehaviour.setSpinnerText(item.getTitle());
                    return true;
                } else {
                    return false;
                }
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
                RadiusStyle.BOTTOM
        );
        int clickBehaviourCode = AutoBookKeepingPreference.getNotificationClickBehaviour(this);
        notificationClickBehaviour.setSpinnerText(
                NotificationClickBehaviour.values()[clickBehaviourCode].getTitle()
        );
        notificationClickBehaviour.setFunctionListener(v -> {
            PopupMenu behaviourMenu = new PopupMenu(this, notificationClickBehaviour.getFunctionComponent());

            //填充选项
            for (NotificationClickBehaviour behaviour : NotificationClickBehaviour.values()) {
                int groupId = behaviour.getGroupId();
                int itemId = behaviour.getItemId();
                int order = behaviour.getOrder();
                String title = behaviour.getTitle();
                behaviourMenu.getMenu().add(groupId, itemId, order, title);
            }

            //设置监听
            behaviourMenu.setOnMenuItemClickListener(item -> {
                //获取选项编号列表
                List<Integer> itemIdList = Arrays.stream(NotificationClickBehaviour.values())
                        .map(NotificationClickBehaviour::getItemId)
                        .collect(Collectors.toList());

                //判断是否选中
                if (itemIdList.contains(item.getItemId())) {
                    int index = itemIdList.indexOf(item.getItemId());
                    AutoBookKeepingPreference.setNotificationClickBehaviour(this, index);
                    notificationClickBehaviour.setSpinnerText(item.getTitle());
                    return true;
                } else {
                    return false;
                }
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
                        LifecycleManager.startExternalActivity(this, intent);
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