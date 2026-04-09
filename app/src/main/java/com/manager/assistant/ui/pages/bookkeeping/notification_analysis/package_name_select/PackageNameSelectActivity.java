package com.manager.assistant.ui.pages.bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.manager.assistant.R;
import com.manager.assistant.databinding.ActivityPackageNameSelectBinding;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.ColorHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.AppListHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.helpers.appearence.ViewEdgeHelper;
import com.manager.assistant.ui.sync.package_name_search.AppInfoSearchViewModel;

import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PackageNameSelectActivity extends AppCompatActivity {
    private boolean isSysAppIncluded = false;                               //应用列表是否包含系统应用
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private AppInfoSearchViewModel searchViewModel;                         //搜索应用的ViewModel
    private AppListAdapter fullAppAdapter, searchAdapter;                   //完整的应用列表适配器和搜索结果适配器
    private ActivityPackageNameSelectBinding binding;                       //绑定的XML视图引用
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =  //权限申请启动器
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionResult
            );
    private final PermissionHelper permissionHelper = new PermissionHelper(    //权限申请器
            this,
            requestPermissionLauncher
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPackageNameSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //边距设置
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            binding.appListRecycler.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchResultRecycler, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, ViewEdgeHelper.dpToPx(this, 10), systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        searchViewModel = new ViewModelProvider(this).get(AppInfoSearchViewModel.class);
        searchViewModel.init();
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!String.valueOf(s).isEmpty()) {
                    binding.searchRefreshLayout.setRefreshing(true);
                }
                searchViewModel.onSearchQueryChanged(String.valueOf(s));
            }
        });
        startObserveSearchResult();

        //进入该界面时尝试申请权限
        addPermissionRequests();

        //拦截返回键功能：先关闭搜索界面再返回上一级
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.searchView.isShowing()) {
                    binding.searchView.hide();
                } else {
                    finish();
                }
            }
        });
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

    //初始化视图
    private void initViews() {
        //设置列表视图
        fullAppAdapter = new AppListAdapter(this::onAppClicked);
        binding.appListRecycler.setAdapter(fullAppAdapter);                 //打开页面时显示的完整应用列表视图
        searchAdapter = new AppListAdapter(this::onAppClicked);
        binding.searchResultRecycler.setAdapter(searchAdapter);             //搜索结果列表视图

        //SearchView和SearchBar
        binding.searchView.setupWithSearchBar(binding.searchBar);

        //开始加载应用列表
        startLoadAppList();

        //设置下拉刷新布局的监听器
        binding.appListRefreshLayout.setOnRefreshListener(this::startLoadAppList);
        binding.searchRefreshLayout.setOnRefreshListener(() -> {
            String searchViewText = binding.searchView.getText().toString();
            searchViewModel.onSearchQueryChanged(searchViewText);
        });

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(this);
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(this);
        int colorBackground = ColorHelper.getBackgroundColor(this);
        binding.appListRefreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        binding.searchRefreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        binding.appListRefreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);
        binding.searchRefreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        //设置图标按钮点击监听器
        ImageButton expandListBtn = binding.expandListBtn;
        expandListBtn.setOnClickListener(this::showPopupMenu);
    }

    /**
     * 添加权限申请
     */
    private void addPermissionRequests() {
        permissionHelper.addPermission("com.android.permission.GET_INSTALLED_APPS");
    }

    /**
     * 处理权限授予情况的方法
     *
     * @param permissions K:权限名称,V:权限是否被授予
     */
    private void onPermissionResult(@NonNull Map<String, Boolean> permissions) {
        boolean allGranted = true;
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            if (!entry.getValue()) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            startLoadAppList();
        } else {
            Toast.makeText(this, "需要应用列表权限才能选择应用", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 在IO线程开始加载应用列表
     */
    private void startLoadAppList() {
        binding.appListRefreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> AppListHelper.getInstalledApps(isSysAppIncluded, this))
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                fullAppInfoList -> {
                                    fullAppAdapter.setAppInfoList(fullAppInfoList);
                                    searchViewModel.setFullAppInfoList(fullAppInfoList);
                                },  //成功回调
                                e -> ExceptionHelper.showExceptionDialog(this, e),  //错误处理
                                () -> binding.appListRefreshLayout.setRefreshing(false)
                        )
        );
    }

    //处理应用选择的方法
    private void onAppClicked(String package_name) {
        Intent result2RuleAddActivity = new Intent();
        result2RuleAddActivity.putExtra(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        setResult(Activity.RESULT_OK, result2RuleAddActivity);
        finish();
    }

    //开始观察搜索结果变化
    private void startObserveSearchResult() {
        searchViewModel.getResultsLiveData().observe(this, result -> {
            searchAdapter.setAppInfoList(result);
            binding.searchRefreshLayout.setRefreshing(false);
        });
    }

    /**
     * 展示下拉菜单
     *
     * @param view 下拉菜单绑定的视图
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_package_select, popupMenu.getMenu());

        popupMenu.getMenu().getItem(0).setChecked(isSysAppIncluded);    //初始化复选框的状态

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.is_sys_app_included) {
                isSysAppIncluded = !isSysAppIncluded;
                item.setChecked(!item.isChecked());

                startLoadAppList();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}