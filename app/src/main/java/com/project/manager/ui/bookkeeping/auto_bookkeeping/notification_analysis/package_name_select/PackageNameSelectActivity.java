package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.project.manager.R;
import com.project.manager.data.data_class.AppInfo;
import com.project.manager.helpers.PermissionHelper;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.helpers.PackageNameHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.view_model.package_name_search.AppInfoSearchViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PackageNameSelectActivity extends AppCompatActivity {
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private SearchView searchView;                                          //搜索视图
    private AppInfoSearchViewModel searchViewModel;                         //搜索应用的ViewModel
    private AppListAdapter fullAppAdapter, searchAdapter;                   //完整的应用列表适配器和搜索结果适配器
    private SwipeRefreshLayout appListRefreshLayout, searchRefreshLayout;   //下拉刷新布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_name_select);

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

        PermissionHelper.getAppListPermission(this);
    }

    //处理动态权限申请结果的方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestResultCode.REQUEST_GET_PERMISSION.ordinal()) {
            if (grantResults[0] == 0) {
                appListRefreshLayout.setRefreshing(true);

                disposables.add(
                        Observable.fromCallable(() -> PackageNameHelper.getInstalledApps(false, this))
                                .subscribeOn(Schedulers.io())               //在IO线程执行查询
                                .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                                .subscribe(
                                        this::onAppListLoadFinished,        //成功回调
                                        e -> {
                                            appListRefreshLayout.setRefreshing(false);
                                            ExceptionHelper.showExceptionDialog(this, e);
                                        }   //错误处理
                                )
                );
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

        // 防止内存泄漏
        disposables.dispose();
    }

    //初始化视图
    private void initViews() {
        searchView = findViewById(R.id.search_view);

        RecyclerView full_app_list_recycler = findViewById(R.id.app_list_recycler);         //打开页面时显示的完整应用列表视图
        fullAppAdapter = new AppListAdapter(this::onAppClicked, this);
        full_app_list_recycler.setAdapter(fullAppAdapter);
        RecyclerView search_result_recycler = findViewById(R.id.search_result_recycler);    //搜索结果列表视图
        searchAdapter = new AppListAdapter(this::onAppClicked, this);
        search_result_recycler.setAdapter(searchAdapter);

        SearchBar searchBar = findViewById(R.id.search_bar);
        searchBar.setOnClickListener(v -> {
            full_app_list_recycler.setPadding(0, 0, 0, 35);
            searchView.show();
        });

        //设置下拉刷新布局的监听器
        appListRefreshLayout = findViewById(R.id.app_list_refresh_layout);
        appListRefreshLayout.setOnRefreshListener(() -> disposables.add(
                Observable.fromCallable(() -> {
                            appListRefreshLayout.setRefreshing(true);
                            return PackageNameHelper.getInstalledApps(false, this);
                        })
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                this::onAppListLoadFinished,        //成功回调
                                e -> {
                                    appListRefreshLayout.setRefreshing(false);
                                    ExceptionHelper.showExceptionDialog(this, e);
                                }   //错误处理
                        )
        ));
        searchRefreshLayout = findViewById(R.id.search_refresh_layout);
        searchRefreshLayout.setOnRefreshListener(() -> {
            String searchViewText = searchView.getText().toString();
            searchViewModel.onSearchQueryChanged(searchViewText);
        });

        //使用多线程实现应用列表加载
        disposables.add(
                Observable.fromCallable(() -> {
                            appListRefreshLayout.setRefreshing(true);
                            return PackageNameHelper.getInstalledApps(false, this);
                        })
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                this::onAppListLoadFinished,        //成功回调
                                e -> {
                                    appListRefreshLayout.setRefreshing(false);
                                    ExceptionHelper.showExceptionDialog(this, e);
                                }   //错误处理
                        )
        );
    }

    /**
     * 应用列表加载完成回调
     *
     * @param fullAppInfoList 加载得到的应用列表
     */
    private void onAppListLoadFinished(List<AppInfo> fullAppInfoList) {
        fullAppAdapter.setAppInfoList(fullAppInfoList);
        searchViewModel.setFullAppInfoList(fullAppInfoList);
        appListRefreshLayout.setRefreshing(false);
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
}