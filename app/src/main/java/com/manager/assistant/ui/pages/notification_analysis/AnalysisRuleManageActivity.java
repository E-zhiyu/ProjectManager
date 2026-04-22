package com.manager.assistant.ui.pages.notification_analysis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.manager.assistant.data.controllers.RuleDataController;
import com.manager.assistant.databinding.ActivityRuleManageBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.ColorHelper;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.data.classes.AnalysisRule;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;
import com.manager.assistant.ui.sync.tag.TagRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AnalysisRuleManageActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> ruleAddLauncher;     //添加规则界面的启动器
    private ActivityResultLauncher<Intent> ruleModifyLauncher;  //修改规则的启动器
    private AnalysisRuleAdapter ruleAdapter;                   //规则列表适配器
    private ActivityRuleManageBinding binding;          //XML视图绑定引用
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final PermissionHelper permissionHelper = new PermissionHelper(this);   //权限申请帮助器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRuleManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.ruleRecycler.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        ViewEdgeHelper.setMarginToNavigation(binding.addFloatingBtn, this);

        initViews();
        AppearanceAnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
        initLaunchers();
        addPermissionRequests();

        //监听标签变化并刷新UI
        TagRepository repository = TagRepository.getInstance();
        repository.getChangedTagList().observe(this, tagList -> {
            if (tagList != null) {
                refreshRuleRecycler();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        disposables.dispose();
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
        binding.addFloatingBtn.setOnClickListener(v -> {
            Intent skip2RuleAdd = new Intent(this, RuleAddModifyActivity.class);
            skip2RuleAdd.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), false);
            ruleAddLauncher.launch(skip2RuleAdd);
        });

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(this);
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(this);
        binding.refreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        int colorBackground = ColorHelper.getBackgroundColor(this);
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        //加载规则列表
        binding.refreshLayout.setRefreshing(true);
        List<AnalysisRule> ruleList;
        try {
            ruleList = new ArrayList<>(RuleDataController.loadAnalysisRule(this));
        } catch (SQLiteException e) {
            ruleList = new ArrayList<>();
            ExceptionHelper.showExceptionDialog(this, e);
        }
        ruleAdapter = new AnalysisRuleAdapter(ruleList, this::onRuleClicked);
        binding.refreshLayout.setRefreshing(false);
        binding.ruleRecycler.setAdapter(ruleAdapter);

        //设置规则列表滚动监听器
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.ruleRecycler, binding.addFloatingBtn);

        //设置下拉刷新布局的刷新监听器
        binding.refreshLayout.setOnRefreshListener(this::refreshRuleRecycler);
    }

    /**
     * 添加权限申请
     */
    private void addPermissionRequests() {
        //添加权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionHelper.addPermission(Manifest.permission.POST_NOTIFICATIONS);
        }
        permissionHelper.addPermission(
                PermissionHelper.SpecialType.AUTO_START,
                "自启动权限",
                "该功能需要在后台运行通知监听服务，如果系统中有自启动权限，请为本应用授权，否则该功能可能无法正常运行。为了进一步保障在后台正常运行，建议您在最近任务锁定本应用"
        );
        permissionHelper.addPermission(
                PermissionHelper.SpecialType.BATTERY,
                "电池优化",
                "为保证软件退出后仍然可以自动监听通知实现自动记账，请将本应用的电池优化策略改为“无限制”"
        );
    }

    /**
     * 初始化启动器
     */
    private void initLaunchers() {
        ruleAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            onAnalysisRuleAdded(data);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                    }
                }
        );

        ruleModifyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode != Activity.RESULT_CANCELED) {
                        if (data != null) {
                            onAnalysisRuleModified(data, resultCode);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
                            ExceptionHelper.showExceptionDialog(this, e);
                        }
                    }
                }
        );
    }

    /**
     * 规则视图被点击的回调
     *
     * @param position 被点击的视图的下标
     * @param rule     规则实例
     */
    private void onRuleClicked(int position, @NonNull AnalysisRule rule) {
        Intent skip2RuleModify = new Intent(this, RuleAddModifyActivity.class);
        Bundle dataBundle = new Bundle();

        String rule_name = rule.getRuleName();                          //规则名称
        long rule_no = rule.getRuleNo();                                //规则编号
        String account_type = rule.getType().toString();                //流水记录类型
        String package_name = rule.getPackageName();                    //目标包名
        String notification_title = rule.getNotificationTitle();        //通知标题
        String notification_content = rule.getNotificationContent();    //通知内容

        dataBundle.putString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue(), rule_name);
        dataBundle.putLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue(), rule_no);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), account_type);
        dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), position);
        dataBundle.putString(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_TITLE.getValue(), notification_title);
        dataBundle.putString(KeyValueStrings.NOTIFICATION_CONTENT.getValue(), notification_content);

        skip2RuleModify.putExtras(dataBundle);
        skip2RuleModify.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), true);
        ruleModifyLauncher.launch(skip2RuleModify);
    }

    /**
     * 新规则添加的回调
     *
     * @param resultIntent 带有新规则数据包的意图
     */
    private void onAnalysisRuleAdded(@NonNull Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        ruleAdapter.addRule(dataBundle);
    }

    /**
     * 规则编辑完成的回调
     *
     * @param resultIntent 带有编辑完成的数据包的意图
     * @param resultCode   标识结果的代码
     */
    private void onAnalysisRuleModified(@NonNull Intent resultIntent, int resultCode) {
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            ruleAdapter.modifyRule(dataBundle);
        } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
            int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
            ruleAdapter.deleteRule(position);
        }
    }

    /**
     * 刷新UI方法
     */
    private void refreshRuleRecycler() {
        binding.refreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> RuleDataController.loadAnalysisRule(this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                ruleList -> {
                                    ruleAdapter.refreshRuleList(ruleList);

                                    if (ruleList.isEmpty()) {
                                        binding.emptyTipText.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.emptyTipText.setVisibility(View.GONE);
                                    }
                                },
                                e -> ExceptionHelper.showExceptionDialog(this, e),
                                () -> {
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.addFloatingBtn.show();
                                }
                        )
        );
    }
}