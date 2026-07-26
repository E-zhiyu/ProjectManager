package com.manager.assistant.ui.pages.budget;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
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
import com.manager.assistant.data.save.db.entities.BudgetEntity;
import com.manager.assistant.data.save.db.services.BudgetService;
import com.manager.assistant.databinding.ActivityBudgetListBinding;
import com.manager.assistant.auxiliary.enums.KeyStrings;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

import java.time.LocalDate;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BudgetListActivity extends AppCompatActivity {
    private ActivityBudgetListBinding binding;              //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //设置界面边距以防内容被小白条遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.recycler.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        addPermissionRequests();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewCompat.requestApplyInsets(getWindow().getDecorView());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        disposable.dispose();
    }

    /**
     * 申请一些必要的权限
     */
    private void addPermissionRequests() {
        PermissionHelper permissionHelper = new PermissionHelper(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.addPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                    "通知权限：预算不足时发送提醒"
            );
        }
        permissionHelper.addPermission(
                PermissionHelper.SpecialPermissionType.ALARM,
                "精确闹钟权限",
                "自动重置预算需要精确闹钟权限，以确保每天0点能够检查并重置预算"
        );
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //工具栏
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //浮动添加按钮
        binding.addFab.setOnClickListener(v -> {
            Intent skip2BudgetInput = new Intent(this, BudgetInputActivity.class);
            startActivity(skip2BudgetInput);
        });
        AppearanceHelper.attachMorphAnimation(binding.addFab);
        AppearanceHelper.setMarginToNavigation(binding.addFab, this);

        //列表
        BudgetListAdapter adapter = new BudgetListAdapter(
                (entity, anchor) -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.BUDGET_ID.v(), entity.getBudgetId());

                    Intent skip2BudgetInput = new Intent(this, BudgetInputActivity.class);
                    skip2BudgetInput.putExtras(bundle);
                    startActivity(skip2BudgetInput);
                },
                (entity, anchor) -> {
                    PopupMenu popupMenu = new PopupMenu(this, anchor, Gravity.END);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_budget_list_edit, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();
                        if (id == R.id.action_delete_budget) {
                            deleteBudget(entity);
                            return true;
                        } else if (id == R.id.action_reset_budget) {
                            resetBudget(entity);
                            return true;
                        }

                        return false;
                    });

                    popupMenu.show();
                }
        );
        binding.recycler.setAdapter(adapter);
        BookkeepingDb db = BookkeepingDb.getInstance(this);
        disposable.add(db.budgetDao().getAllBudgetFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        budgetList -> {
                            if (budgetList.isEmpty()) {
                                binding.emptyText.setVisibility(View.VISIBLE);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }

                            adapter.submitList(budgetList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(this, e)
                )
        );
    }

    /**
     * 删除预算
     *
     * @param budget 需要删除的预算
     */
    private void deleteBudget(@NonNull BudgetEntity budget) {
        String message = String.format(
                Locale.getDefault(),
                "确认删除“%s”吗？",
                budget.getName()
        );
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_budget)
                .setMessage(message)
                .setPositiveButton("确定", (dialogInterface, i) ->
                        disposable.add(BudgetService.deleteBudget(budget, this)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Toast.makeText(this, "预算删除成功", Toast.LENGTH_SHORT).show(),
                                        e -> ExceptionHelper.showExceptionDialog(this, e)
                                )
                        )
                )
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 重置预算
     *
     * @param budget 需要重置的预算
     */
    private void resetBudget(@NonNull BudgetEntity budget) {
        String message = String.format(
                Locale.getDefault(),
                "此操作会将余额重置为初始金额，并将起算日期设置为当前日期。确认重置“%s”吗？",
                budget.getName()
        );
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reset_budget)
                .setMessage(message)
                .setPositiveButton("确定", (dialogInterface, i) -> {
                    BookkeepingDb db = BookkeepingDb.getInstance(this);
                    disposable.add(db.budgetDao().resetBudgetById(budget.getBudgetId(), LocalDate.now())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> Toast.makeText(this, "预算重置成功", Toast.LENGTH_SHORT).show(),
                                    e -> ExceptionHelper.showExceptionDialog(this, e)
                            )
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }
}