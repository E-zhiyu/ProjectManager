package com.sly.coffer.ui.pages.main.settings.sub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sly.coffer.SlyCoffer;
import com.sly.coffer.R;
import com.sly.coffer.auxiliary.enums.RadiusStyle;
import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.preference.AutoBookKeepingPreference;
import com.sly.coffer.databinding.ActivityAutoBookkeepingBinding;
import com.sly.coffer.auxiliary.enums.settings.NotificationCancelBehaviour;
import com.sly.coffer.auxiliary.enums.settings.NotificationClickBehaviour;
import com.sly.coffer.helpers.ExceptionHelper;
import com.sly.coffer.helpers.PermissionHelper;
import com.sly.coffer.helpers.appearence.AppearanceHelper;
import com.sly.coffer.ui.pages.notification.rule.NotificationRuleListActivity;
import com.sly.coffer.ui.pages.main.settings.components.SettingClickableTextView;
import com.sly.coffer.ui.pages.main.settings.components.SettingSpinnerView;
import com.sly.coffer.ui.pages.main.settings.components.SettingSwitchView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AutoBookkeepingActivity extends AppCompatActivity {
    private ActivityAutoBookkeepingBinding binding; //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAutoBookkeepingBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposable.dispose();
        binding = null;
    }

    /**
     * 初始化自动记账设置项
     */
    private void initAutoBookkeepingSettings() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        //通知解析自动记账
        SettingSwitchView notificationAnalysisSwitchOption = new SettingSwitchView(
                this,
                binding.notificationAnalysisSwitchOption,
                R.string.notification_analysis,
                "通过应用发送的通知来记账",
                R.drawable.outline_notifications_active_24,
                RadiusStyle.TOP
        );
        boolean isNotificationAnalysisOpened = AutoBookKeepingPreference.getSwitchStat(this);
        if (isNotificationAnalysisOpened && PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.isGranted(this)) {
            notificationAnalysisSwitchOption.setChecked(true);
        } else {
            notificationAnalysisSwitchOption.setChecked(false);

            //考虑到无授权情况下自动关闭通知解析功能
            AutoBookKeepingPreference.setSwitchStat(false, this);
        }
        notificationAnalysisSwitchOption.setFunctionListener((buttonView, isChecked) -> {
            //没有权限时提示授权
            if (!PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.isGranted(this) && isChecked) {
                AutoBookKeepingPreference.setSwitchStat(false, this);   //将打开状态写入文件
                buttonView.setChecked(false);
                new MaterialAlertDialogBuilder(this)
                        .setTitle("权限申请说明")
                        .setMessage("此功能需要使用“通知使用权”权限，该权限允许应用读取其他软件发送的通知内容。是否为本应用授权？")
                        .setPositiveButton("确认", (dialog, which) -> {
                            SlyCoffer.lockLifecycleObserver();
                            Intent intent = PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.getIntent(this);
                            startActivity(intent);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                AutoBookKeepingPreference.setSwitchStat(isChecked, this);   //将打开状态写入文件
            }
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
        ruleManageOption.setFunctionListener(v -> {
            Intent intent = new Intent(this, NotificationRuleListActivity.class);
            startActivity(intent);
        });

        //直接入账开关
        SettingSwitchView directDepositSwitch = new SettingSwitchView(
                this,
                binding.directDeposit,
                R.string.direct_deposit,
                "无需确认是否保留而直接入账",
                R.drawable.outline_notifications_off_24,
                RadiusStyle.BOTTOM
        );
        directDepositSwitch.setChecked(AutoBookKeepingPreference.getDirectDeposit(this));
        directDepositSwitch.setFunctionListener((compoundButton, b) ->
                AutoBookKeepingPreference.setDirectDeposit(b, this)
        );

        //通知取消行为
        SettingSpinnerView notificationCancelBehaviour = new SettingSpinnerView(
                this,
                binding.notificationCancelBehaviour,
                R.string.notification_cancel_behaviour,
                "划走确认通知后执行的操作",
                R.drawable.outline_comments_disabled_24,
                RadiusStyle.TOP
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

        //通知捕获开关
        SettingSwitchView captureNotificationSwitch = new SettingSwitchView(
                this,
                binding.notificationCaptureSwitch,
                R.string.capture_notification,
                "保存通知以便添加通知规则",
                R.drawable.outline_download_24,
                RadiusStyle.TOP
        );
        captureNotificationSwitch.setChecked(AutoBookKeepingPreference.getNotificationCapture(this));
        captureNotificationSwitch.setFunctionListener((compoundButton, b) -> {
            if (b) {
                if (PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.isGranted(this)) {
                    AutoBookKeepingPreference.setNotificationCapture(this, true);
                    Toast.makeText(this, "一天后自动关闭通知捕获以节省性能", Toast.LENGTH_SHORT).show();
                } else {
                    compoundButton.setChecked(false);
                    String message = "通知捕获依赖通知监听服务，请授权后再启用通知捕获功能。";
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("权限申请说明")
                            .setMessage(message)
                            .setPositiveButton("去授权", (dialogInterface, i) -> {
                                Intent intent = PermissionHelper.SpecialPermissionType.NOTIFICATION_LISTENER.getIntent(this);
                                startActivity(intent);
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            } else {
                AutoBookKeepingPreference.setNotificationCapture(this, b);
            }
        });

        //清空捕获通知
        SettingClickableTextView clearCapturedNotification = new SettingClickableTextView(
                this,
                binding.capturedNotificationClearOption,
                R.string.clear_captured_notification,
                "清空数据库中捕获的通知",
                R.drawable.baseline_clear_24,
                RadiusStyle.BOTTOM
        );
        clearCapturedNotification.setFunctionListener(view -> {
            BookkeepingDb db = BookkeepingDb.getInstance(this);
            disposable.add(db.capturedNotificationDao().clearCapturedNotification()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            () -> Toast.makeText(this, "捕获的通知已清除", Toast.LENGTH_SHORT).show(),
                            e -> ExceptionHelper.showExceptionDialog(this, e)
                    )
            );
        });
    }
}