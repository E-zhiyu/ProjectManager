package com.manager.assistant.ui.pages.notification_rule;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;
import com.manager.assistant.databinding.ActivityNotificationRuleListBinding;
import com.manager.assistant.auxiliary.enums.KeyStrings;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NotificationRuleListActivity extends AppCompatActivity {
    private ActivityNotificationRuleListBinding binding;                                    //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();
    private final PermissionHelper permissionHelper = new PermissionHelper(this);   //权限申请帮助器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityNotificationRuleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.recycler.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        initViews();
        addPermissionRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        disposable.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionHelper.start();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //添加规则按钮
        binding.addFab.setOnClickListener(v -> {
            Intent skip2RuleInput = new Intent(this, NotificationRuleInputActivity.class);
            startActivity(skip2RuleInput);
        });
        AppearanceHelper.setMarginToNavigation(binding.addFab, this);
        AppearanceHelper.attachMorphAnimation(binding.addFab);

        //列表
        NotificationRuleListAdapter adapter = new NotificationRuleListAdapter(
                (entity, anchor) -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.NOTIFICATION_RULE_ID.v(), entity.getRuleId());

                    Intent skip2RuleInput = new Intent(this, NotificationRuleInputActivity.class);
                    skip2RuleInput.putExtras(bundle);
                    startActivity(skip2RuleInput);
                },
                (entity, anchor) -> {
                    PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_notification_rule_edit, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.action_delete_notification_rule) {
                            deleteNotificationRule(entity);
                            return true;
                        }

                        return false;
                    });

                    popupMenu.show();
                }
        );
        binding.recycler.setAdapter(adapter);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(db.ruleDao().getAllNotificationRuleFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        ruleList -> {
                            if (ruleList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }

                            adapter.submitList(ruleList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 添加权限申请
     */
    private void addPermissionRequests() {
        //添加权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.addPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "通知权限：触发自动记账后发送确认通知"
            );
        }
        permissionHelper.addPermission(
                PermissionHelper.SpecialPermissionType.AUTO_START,
                "自启动权限",
                "该功能需要在后台运行通知监听服务，如果系统中有自启动权限，请为本应用授权，否则该功能可能无法正常运行。为了进一步保障在后台正常运行，建议您在最近任务锁定本应用"
        );
        permissionHelper.addPermission(
                PermissionHelper.SpecialPermissionType.BATTERY,
                "电池优化",
                "为保证软件退出后仍然可以自动监听通知实现自动记账，请将本应用的电池优化策略改为“无限制”"
        );
    }

    /**
     * 删除通知规则
     *
     * @param rule 待删除的通知规则
     */
    private void deleteNotificationRule(@NonNull NotificationRuleEntity rule) {
        String message = String.format(
                Locale.getDefault(),
                "确认删除“%s”吗？",
                rule.getName()
        );
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_budget)
                .setMessage(message)
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    BookkeepingDb db = BookkeepingDb.getInstance(this);
                    disposable.add(db.ruleDao().deleteNotificationRule(rule)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Toast.makeText(this, "通知规则删除成功", Toast.LENGTH_SHORT).show(),
                                    e -> ExceptionHelper.showExceptionDialog(this, e)
                            )
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }
}