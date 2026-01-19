package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.rule_edit;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.data_save.preference.AutoBookKeepingPreference;
import com.manager.assistant.databinding.ActivityAnalysisRuleManageBinding;
import com.manager.assistant.helpers.ColorHelper;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.enums.RequestResultCode;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.data.data_class.AnalysisRule;
import com.manager.assistant.ui.data_communication.tag_modify.TagRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AnalysisRuleManageActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityResultLauncher<Intent> ruleAddLauncher;     //添加规则界面的启动器
    private ActivityResultLauncher<Intent> ruleModifyLauncher;  //修改规则的启动器
    private AnalysisRuleAdapter ruleAdapter;                   //规则列表适配器
    private ActivityAnalysisRuleManageBinding binding;          //XML视图绑定引用
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAnalysisRuleManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initLaunchers();

        //第一次打开提示授予自启动权限
        if (!AutoBookKeepingPreference.getHintAutoStart(this)) {
            AutoBookKeepingPreference.setHintAutoStart(true, this);

            //弹出提示框
            new MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("由于国内厂商的电池策略，使用该功能前必须授予应用自启动权限，需要跳转到自启动权限管理界面吗？")
                    .setNegativeButton("不用了", ((dialog, which) -> dialog.dismiss()))
                    .setPositiveButton("前往授权", ((dialog, which) -> {
                        dialog.dismiss();
                        PermissionHelper.requestAutoStartPermission(this);
                    }))
                    .show();
        }

        //监听标签变化并刷新UI
        TagRepository repository = TagRepository.getInstance();
        repository.getChangedTagList().observe(this, tagList -> {
            if (tagList != null) {
                refreshUI();
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
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.rule_add_btn) {
            Intent skip2RuleAdd = new Intent(this, RuleAddModifyActivity.class);
            skip2RuleAdd.putExtra(KeyValueStrings.IS_MODIFY_MODE.getValue(), false);
            ruleAddLauncher.launch(skip2RuleAdd);
        }
    }

    private void initViews() {
        //设置标题栏的图标点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.ruleAddBtn.setOnClickListener(this);

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
            ruleList = AnalysisRule.loadAnalysisRule(this);
        } catch (SQLiteException e) {
            ruleList = new ArrayList<>();
            ExceptionHelper.showExceptionDialog(this, e);
        }
        ruleAdapter = new AnalysisRuleAdapter(ruleList, this::onRuleClicked, this);
        binding.refreshLayout.setRefreshing(false);
        binding.ruleRecycler.setAdapter(ruleAdapter);

        //设置下拉刷新布局的刷新监听器
        binding.refreshLayout.setOnRefreshListener(this::refreshUI);
    }

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

    //规则点击处理方法
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

    private void onAnalysisRuleAdded(@NonNull Intent resuleIntent) {
        Bundle dataBundle = resuleIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取新增解析规则的数据");
            ExceptionHelper.showExceptionDialog(this, e);
            return;
        }

        ruleAdapter.addRule(dataBundle);
    }

    private void onAnalysisRuleModified(@NonNull Intent resuleIntent, int resultCode) {
        Bundle dataBundle = resuleIntent.getExtras();
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

    private void refreshUI() {
        binding.refreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> AnalysisRule.loadAnalysisRule(this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                ruleList -> ruleAdapter.onListRefreshed(ruleList),
                                e -> ExceptionHelper.showExceptionDialog(this, e),
                                () -> binding.refreshLayout.setRefreshing(false)
                        )
        );
    }
}