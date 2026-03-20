package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.data.classes.Budget;
import com.manager.assistant.databinding.ActivityBudgetManageBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.appearence.AnimationHelper;
import com.manager.assistant.helpers.resourse.ColorHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BudgetManageActivity extends AppCompatActivity {
    private ActivityBudgetManageBinding binding;            //绑定的XML视图
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private ActivityResultLauncher<Intent> addLauncher;     //添加预算启动器
    private ActivityResultLauncher<Intent> modifyLauncher;  //修改预算启动器
    private BudgetAdapter adapter;                          //预算列表适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //初始化启动器和视图
        initViews();
        initLaunchers();

        //申请权限
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestResultCode.REQUEST_NOTIFICATION_PERMISSION.ordinal()) {
            //判断通知权限是否授予
            if (grantResults[0] == -1) {
                Toast.makeText(this, "通知权限被拒绝，请前往设置手动授予", Toast.LENGTH_SHORT).show();
            }

            //没有闹钟权限时提示授予闹钟权限
            boolean isAlarmEnable = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S || PermissionHelper.hasExactAlarmPermission(this);
            if (!isAlarmEnable) {
                Toast.makeText(this, "请再授予精确闹钟权限", Toast.LENGTH_SHORT).show();
                PermissionHelper.requestExactAlarmPermission(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;

        // 防止内存泄漏
        disposables.dispose();
    }

    /**
     * 申请一些必要的权限
     */
    private void requestPermissions() {
        boolean isAlarmEnable = Build.VERSION.SDK_INT <= Build.VERSION_CODES.S || PermissionHelper.hasExactAlarmPermission(this);
        boolean isNotificationEnable = Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU
                || PermissionHelper.isPermissionsGranted(this, Manifest.permission.POST_NOTIFICATIONS);

        String message;
        Runnable action;
        if (!isAlarmEnable && !isNotificationEnable) {
            message = "本功能需要申请以下权限：\n1.精确闹钟权限：用于定期自动重置预算\n2.通知权限：用于在预算余额不足时提醒用户\n请点击“确定”授予这些权限";
            action = () -> PermissionHelper.requestNotificationPermission(this);
        } else if (!isAlarmEnable) {
            message = "预算管理功能需要精确闹钟权限以实现自动重置预算，请点击“确定”授予该权限";
            action = () -> PermissionHelper.requestExactAlarmPermission(this);
        } else if (!isNotificationEnable) {
            message = "预算余额较低时需要发送通知提醒，请授予通知权限，请点击“确定”授予该权限";
            action = () -> PermissionHelper.requestNotificationPermission(this);
        } else {
            return;
        }

        //如果有权限缺失则弹出弹窗提示用户
        new MaterialAlertDialogBuilder(this)
                .setTitle("权限说明")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> action.run())
                .setNegativeButton("取消", null)
                .show()
                .setOnCancelListener(dialog -> finish());
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        AnimationHelper.attachMorphAnimation(binding.addFloatingBtn);

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(this);
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(this);
        binding.refreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        int colorBackground = ColorHelper.getBackgroundColor(this);
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        //浮动添加按钮
        binding.addFloatingBtn.setOnClickListener(v -> {
            Intent skip2BudgetAdd = new Intent(this, BudgetAddModifyActivity.class);
            addLauncher.launch(skip2BudgetAdd);
        });

        //设置RecyclerView
        adapter = new BudgetAdapter(this::onBudgetClicked);
        binding.budgetRecycler.setAdapter(adapter);
        refreshBudget();

        //设置刷新布局的功能
        binding.refreshLayout.setOnRefreshListener(this::refreshBudget);

        //设置浮动按钮隐藏行为
        AnimationHelper.setupFloatingBtnBehaviour(binding.budgetRecycler, binding.addFloatingBtn);
    }

    /**
     * 初始化启动器
     */
    private void initLaunchers() {
        addLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent intent = result.getData();
                    if (resultCode == Activity.RESULT_CANCELED || intent == null) {
                        return;
                    }
                    Bundle dataBundle = intent.getExtras();
                    if (dataBundle == null) {
                        return;
                    }

                    onBudgetAdded(dataBundle);
                }
        );

        modifyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent intent = result.getData();
                    if (resultCode == Activity.RESULT_CANCELED || intent == null) {
                        return;
                    }
                    Bundle dataBundle = intent.getExtras();
                    if (dataBundle == null) {
                        return;
                    }

                    if (resultCode == Activity.RESULT_OK) {
                        onBudgetModified(dataBundle);
                    } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
                        onBudgetDeleted(dataBundle);
                    }
                }
        );
    }

    /**
     * 处理预算ViewHolder点击的回调
     *
     * @param budget   点击的预算实例
     * @param position 点击的视图下标
     */
    private void onBudgetClicked(@NonNull Budget budget, int position) {
        long bno = budget.getBno();
        String name = budget.getName();
        double initAmount = budget.getInitAmount();
        double leftAmount = budget.getLeftAmount();
        String startDate = budget.getStartDate();
        ResetFrequency resetFrequency = budget.getResetFrequency();
        long[] tagNos = budget.getTagNoList().stream()
                .mapToLong(Long::longValue)
                .toArray();

        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.BNO.getValue(), bno);
        dataBundle.putString(KeyValueStrings.BUDGET_NAME.getValue(), name);
        dataBundle.putDouble(KeyValueStrings.INIT_AMOUNT.getValue(), initAmount);
        dataBundle.putDouble(KeyValueStrings.LEFT_AMOUNT.getValue(), leftAmount);
        dataBundle.putString(KeyValueStrings.START_DATE.getValue(), startDate);
        dataBundle.putString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue(), resetFrequency.toString());
        dataBundle.putLongArray(KeyValueStrings.TAG_NO.getValue(), tagNos);
        dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), position);

        Intent skip2BudgetModify = new Intent(this, BudgetAddModifyActivity.class);
        skip2BudgetModify.putExtras(dataBundle);
        modifyLauncher.launch(skip2BudgetModify);
    }

    /**
     * 刷新预算
     */
    private void refreshBudget() {
        binding.refreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> Budget.getAllBudgets(this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                adapter::refreshBudget,
                                e -> ExceptionHelper.showExceptionDialog(this, e),
                                () -> {
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.addFloatingBtn.show();
                                }
                        )
        );
    }

    /**
     * 处理预算添加的回调
     *
     * @param dataBundle 新添加的预算的数据包
     */
    private void onBudgetAdded(Bundle dataBundle) {
        adapter.addBudget(dataBundle, this);
        Toast.makeText(this, "预算添加成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 预算修改的回调
     *
     * @param dataBundle 修改后的数据包
     */
    private void onBudgetModified(Bundle dataBundle) {
        adapter.modifyBudget(dataBundle, this);
        Toast.makeText(this, "预算修改成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 预算删除的回调
     *
     * @param dataBundle 删除预算的数据包
     */
    private void onBudgetDeleted(Bundle dataBundle) {
        adapter.deleteBudget(dataBundle, this);
        Toast.makeText(this, "预算删除成功", Toast.LENGTH_SHORT).show();
    }
}