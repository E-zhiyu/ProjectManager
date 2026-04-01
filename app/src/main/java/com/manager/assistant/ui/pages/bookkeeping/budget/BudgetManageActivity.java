package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.manager.assistant.data.classes.Budget;
import com.manager.assistant.data.controllers.BudgetDataController;
import com.manager.assistant.databinding.ActivityBudgetManageBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.PermissionHelper;
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
    private final PermissionHelper permissionHelper = new PermissionHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.budgetRecycler.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        //初始化启动器和视图
        initViews();
        initLaunchers();

        //申请权限
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

        // 防止内存泄漏
        disposables.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissionHelper.start();
    }

    /**
     * 申请一些必要的权限
     */
    private void addPermissionRequests() {
        permissionHelper.addPermission("android.permission.POST_NOTIFICATIONS");
        permissionHelper.addPermission(
                PermissionHelper.SpecialType.ALARM,
                "精确闹钟权限",
                "自动重置预算需要精确闹钟权限，以确保每天0点能够检查并重置预算"
        );
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
                Observable.fromCallable(() -> BudgetDataController.getAllBudgets(this))
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