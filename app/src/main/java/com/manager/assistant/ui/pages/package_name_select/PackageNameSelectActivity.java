package com.manager.assistant.ui.pages.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.search.SearchView;
import com.manager.assistant.R;
import com.manager.assistant.data.classes.AppInfo;
import com.manager.assistant.data.save.preference.SearchHistoryPreference;
import com.manager.assistant.databinding.ActivityPackageNameSelectBinding;
import com.manager.assistant.helpers.PermissionHelper;
import com.manager.assistant.helpers.appearence.ColorHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.AppListHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.ui.others.adapters.SearchHistoryAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PackageNameSelectActivity extends AppCompatActivity {
    private boolean isSysAppIncluded = false;                               //应用列表是否包含系统应用
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private AppListAdapter appListAdapter;                                  //应用列表适配器
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
    private String searchText = "";                                         //搜索文本
    private final List<AppInfo> fullAppInfoList = new ArrayList<>();            //完整的应用列表

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

        initViews();

        //进入该界面时尝试申请权限
        addPermissionRequests();
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
        appListAdapter = new AppListAdapter(this::onAppClicked);
        binding.appListRecycler.setAdapter(appListAdapter);                 //打开页面时显示的完整应用列表视图

        //SearchView和SearchBar
        initSearchViewAndSearchBar();

        //开始加载应用列表并显示
        refreshFullAppInfoList();

        //设置下拉刷新布局的监听器
        binding.appListRefreshLayout.setOnRefreshListener(this::refreshFullAppInfoList);

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(this);
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(this);
        int colorBackground = ColorHelper.getBackgroundColor(this);
        binding.appListRefreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        binding.appListRefreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        //设置图标按钮点击监听器
        ImageButton expandListBtn = binding.expandListBtn;
        expandListBtn.setOnClickListener(this::showPopupMenu);
    }

    /**
     * 初始化SearchView和SearchBar
     */
    private void initSearchViewAndSearchBar() {
        //绑定SearchView和SearchBar
        binding.searchView.setupWithSearchBar(binding.searchBar);

        //实例化搜索历史适配器
        SearchHistoryAdapter historyAdapter = new SearchHistoryAdapter(keyWord -> {
            searchText = keyWord;
            binding.searchBar.setText(keyWord);

            //刷新UI
            refreshAppListRecycler();

            //隐藏SearchView
            binding.searchView.hide();

            //保存关键词
            if (!keyWord.isEmpty()) {
                List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                        SearchHistoryPreference.KEY_APP_NAME,
                        this
                );
                searchHistoryList.remove(searchText);       //移除已存在的项
                searchHistoryList.add(0, searchText);   //添加新项
                if (searchHistoryList.size() > 15) {        //限制15条记录
                    searchHistoryList = searchHistoryList.subList(0, 14);
                }
                SearchHistoryPreference.setHistory(
                        SearchHistoryPreference.KEY_APP_NAME,
                        searchHistoryList,
                        this
                );
            }
        });
        binding.searchHistoryRecycler.setAdapter(historyAdapter);

        //先刷新一下内容，以应对SearchView展开时的界面重建
        List<String> historyList = SearchHistoryPreference.getHistory(
                SearchHistoryPreference.KEY_APP_NAME,
                this
        );
        historyAdapter.refreshSearchHistory(historyList);

        //SearchView显示监听
        binding.searchView.addTransitionListener((searchView, transitionState, newState) -> {
            //刷新一下历史记录
            if (newState == SearchView.TransitionState.SHOWING) {
                List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                        SearchHistoryPreference.KEY_APP_NAME,
                        this
                );
                historyAdapter.refreshSearchHistory(searchHistoryList);
            }
        });

        //设置清除搜索历史按钮点击监听
        binding.clearHistoryBtn.setOnClickListener(v -> {
            SearchHistoryPreference.setHistory(
                    SearchHistoryPreference.KEY_APP_NAME,
                    new ArrayList<>(),
                    this
            );
            historyAdapter.refreshSearchHistory(new ArrayList<>());
        });

        //设置搜索监听
        binding.searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //获取输入文本
                searchText = binding.searchView.getText().toString();

                //将文本显示在SearchBar上
                binding.searchBar.setText(searchText);

                //保存搜索关键词
                if (!searchText.isEmpty()) {
                    List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                            SearchHistoryPreference.KEY_APP_NAME,
                            this
                    );
                    searchHistoryList.remove(searchText);       //移除已存在的项
                    searchHistoryList.add(0, searchText);   //添加新项
                    if (searchHistoryList.size() > 15) {        //限制15条记录
                        searchHistoryList = searchHistoryList.subList(0, 14);
                    }
                    SearchHistoryPreference.setHistory(
                            SearchHistoryPreference.KEY_APP_NAME,
                            searchHistoryList,
                            this
                    );
                }

                //刷新视图
                refreshAppListRecycler();

                //隐藏SearchView
                binding.searchView.hide();

                return true;
            } else {
                return false;
            }
        });

        //添加文本变化监听器，用于在清空文本后自动清除搜索并刷新视图
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    searchText = "";
                    binding.searchBar.setText("");
                    refreshAppListRecycler();
                }
            }
        });
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
            refreshFullAppInfoList();
        } else {
            Toast.makeText(this, "需要应用列表权限才能选择应用", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 刷新完整的应用列表
     */
    private void refreshFullAppInfoList() {
        binding.appListRefreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> AppListHelper.getInstalledApps(isSysAppIncluded, this))
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                appInfoList -> {
                                    this.fullAppInfoList.clear();
                                    this.fullAppInfoList.addAll(appInfoList);
                                },
                                e -> ExceptionHelper.showExceptionDialog(this, e),
                                this::refreshAppListRecycler
                        )
        );
    }

    /**
     * 刷新应用列表视图
     */
    private void refreshAppListRecycler() {
        binding.appListRefreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> {
                            //获取数据
                            List<AppInfo> appInfoList;
                            if (searchText.isEmpty()) {
                                appInfoList = new ArrayList<>(fullAppInfoList);
                            } else {
                                appInfoList = fullAppInfoList.stream()
                                        .filter(appInfo -> appInfo.getAppName().toLowerCase()
                                                .contains(searchText.toLowerCase())
                                        )
                                        .collect(Collectors.toList());
                            }

                            //排序后再返回
                            appInfoList.sort(Comparator.comparing(AppInfo::getAppName));
                            return appInfoList;
                        })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                appInfoList -> {
                                    appListAdapter.setAppInfoList(appInfoList);

                                    if (appInfoList.isEmpty()) {
                                        binding.emptyTipText.setVisibility(View.VISIBLE);
                                    } else {
                                        binding.emptyTipText.setVisibility(View.GONE);
                                    }
                                },
                                e -> {
                                    ExceptionHelper.showExceptionDialog(this, e);
                                    binding.appListRefreshLayout.setRefreshing(false);
                                },
                                () -> binding.appListRefreshLayout.setRefreshing(false)
                        )
        );
    }

    /**
     * 应用条目点击回调
     *
     * @param packageName 点击的应用的包名
     */
    private void onAppClicked(String packageName) {
        Intent result2RuleAddActivity = new Intent();
        result2RuleAddActivity.putExtra(KeyValueStrings.PACKAGE_NAME.getValue(), packageName);
        setResult(Activity.RESULT_OK, result2RuleAddActivity);
        finish();
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

                //刷新应用列表并刷新视图
                refreshFullAppInfoList();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}