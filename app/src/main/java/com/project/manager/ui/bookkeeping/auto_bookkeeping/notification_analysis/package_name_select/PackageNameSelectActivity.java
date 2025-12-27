package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.search.SearchView;
import com.project.manager.R;
import com.project.manager.databinding.ActivityPackageNameSelectBinding;
import com.project.manager.helpers.PermissionHelper;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.helpers.PackageNameHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.view_model.package_name_search.AppInfoSearchViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PackageNameSelectActivity extends AppCompatActivity {
    private boolean isSysAppIncluded = false;                               //应用列表是否包含系统应用
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private SearchView searchView;                                          //搜索视图
    private AppInfoSearchViewModel searchViewModel;                         //搜索应用的ViewModel
    private AppListAdapter fullAppAdapter, searchAdapter;                   //完整的应用列表适配器和搜索结果适配器
    private SwipeRefreshLayout appListRefreshLayout, searchRefreshLayout;   //下拉刷新布局
    private ActivityPackageNameSelectBinding binding;                       //绑定的XML视图引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPackageNameSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        searchViewModel = new ViewModelProvider(this).get(AppInfoSearchViewModel.class);
        searchViewModel.init();
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!String.valueOf(s).isEmpty()) {
                    searchRefreshLayout.setRefreshing(true);
                }
                searchViewModel.onSearchQueryChanged(String.valueOf(s));
            }
        });
        startObserveSearchResult();

        //进入该界面时尝试申请权限
        PermissionHelper.getAppListPermission(this);

        //拦截返回键功能：先关闭搜索界面再返回上一级
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView.isShowing()) {
                    searchView.hide();
                } else {
                    finish();
                }
            }
        });
    }

    //处理动态权限申请结果的方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestResultCode.REQUEST_GET_PERMISSION.ordinal()) {
            if (grantResults[0] == 0) {
                appListRefreshLayout.setRefreshing(true);
                startLoadAppList();
            } else if (grantResults[0] == -1) {
                Toast.makeText(this, "权限申请被拒绝，请手动授予应用列表权限并重启应用", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
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

    //初始化视图
    private void initViews() {
        searchView = binding.searchView;

        RecyclerView full_app_list_recycler = binding.appListRecycler;         //打开页面时显示的完整应用列表视图
        fullAppAdapter = new AppListAdapter(this::onAppClicked, this);
        full_app_list_recycler.setAdapter(fullAppAdapter);
        RecyclerView search_result_recycler = binding.searchResultRecycler;    //搜索结果列表视图
        searchAdapter = new AppListAdapter(this::onAppClicked, this);
        search_result_recycler.setAdapter(searchAdapter);

        //开始加载应用列表
        appListRefreshLayout = binding.appListRefreshLayout;
        appListRefreshLayout.setRefreshing(true);
        startLoadAppList();

        //设置下拉刷新布局的监听器
        appListRefreshLayout.setOnRefreshListener(this::startLoadAppList);
        searchRefreshLayout = binding.searchRefreshLayout;
        searchRefreshLayout.setOnRefreshListener(() -> {
            String searchViewText = searchView.getText().toString();
            searchViewModel.onSearchQueryChanged(searchViewText);
        });

        //获取主题色并将其设置为刷新箭头颜色
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimaryDark, typedValue, true);
        int colorPrimary = typedValue.data;
        appListRefreshLayout.setColorSchemeColors(colorPrimary);
        searchRefreshLayout.setColorSchemeColors(colorPrimary);

        //设置图标按钮点击监听器
        ImageButton expandListBtn = binding.expandListBtn;
        expandListBtn.setOnClickListener(this::showPopupMenu);
    }

    /**
     * 在IO线程开始加载应用列表
     */
    private void startLoadAppList() {
        disposables.add(
                Observable.fromCallable(() -> PackageNameHelper.getInstalledApps(isSysAppIncluded, this))
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                fullAppInfoList -> {
                                    fullAppAdapter.setAppInfoList(fullAppInfoList);
                                    searchViewModel.setFullAppInfoList(fullAppInfoList);
                                },  //成功回调
                                e -> ExceptionHelper.showExceptionDialog(this, e),  //错误处理
                                () -> appListRefreshLayout.setRefreshing(false)
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
            searchRefreshLayout.setRefreshing(false);
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

                appListRefreshLayout.setRefreshing(true);
                startLoadAppList();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }
}