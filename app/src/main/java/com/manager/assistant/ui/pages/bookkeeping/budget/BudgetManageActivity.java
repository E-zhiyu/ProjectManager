package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.manager.assistant.databinding.ActivityBudgetManageBinding;
import com.manager.assistant.enums.RequestResultCode;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.ColorHelper;

public class BudgetManageActivity extends AppCompatActivity {
    private ActivityBudgetManageBinding binding;            //绑定的XML视图
    private ActivityResultLauncher<Intent> addLauncher;     //添加预算启动器
    private ActivityResultLauncher<Intent> modifyLauncher;  //修改预算启动器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initLaunchers();
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

                    if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
                        onBudgetModified(dataBundle);
                    } else if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
                        onBudgetDeleted(dataBundle);
                    }
                }
        );
    }

    /**
     * 处理预算添加的回调
     *
     * @param dataBundle 新添加的预算的数据包
     */
    private void onBudgetAdded(Bundle dataBundle) {
        //TODO:完成该方法
    }

    /**
     * 预算修改的回调
     *
     * @param dataBundle 修改后的数据包
     */
    private void onBudgetModified(Bundle dataBundle) {
        //TODO:完成该方法
    }

    /**
     * 预算删除的回调
     *
     * @param dataBundle 删除预算的数据包
     */
    private void onBudgetDeleted(Bundle dataBundle) {
        //TODO:完成该方法
    }
}