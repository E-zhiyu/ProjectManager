package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.package_name_select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.project.manager.R;
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
    private SearchView searchView;                          //搜索视图
    private AppInfoSearchViewModel searchViewModel;         //搜索应用的ViewModel
    private ProgressBar progressBar, searchProgressBar;     //加载列表时的进度条
    private AppListAdapter fullAppAdapter, searchAdapter;   //完整的应用列表适配器和搜索结果适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_name_select);

        initViews();
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
        progressBar = findViewById(R.id.progress_bar);
        searchProgressBar = findViewById(R.id.search_progress_bar);

        //绑定SearchView弹出逻辑
        SearchBar searchBar = findViewById(R.id.search_bar);
        searchBar.setOnClickListener(v -> searchView.show());

        RecyclerView full_app_list_recycler = findViewById(R.id.app_list_recycler);         //打开页面时显示的完整应用列表视图
        fullAppAdapter = new AppListAdapter(this::onAppClicked, this);
        full_app_list_recycler.setAdapter(fullAppAdapter);
        RecyclerView search_result_recycler = findViewById(R.id.search_result_recycler);    //搜索结果列表视图
        searchAdapter = new AppListAdapter(this::onAppClicked, this);
        search_result_recycler.setAdapter(searchAdapter);

        //使用多线程实现应用列表加载
        disposables.add(
                Observable.fromCallable(() -> {
                            progressBar.setVisibility(View.VISIBLE);    //显示不确定进度的进度条
                            return PackageNameHelper.getInstalledApps(false, this);
                        })
                        .subscribeOn(Schedulers.io()) //在 IO 线程执行查询
                        .observeOn(AndroidSchedulers.mainThread()) //切换到主线程更新 UI
                        .subscribe(
                                this::onAppListLoadSuccessfully,  //成功回调
                                e -> {
                                    fullAppAdapter = new AppListAdapter(this::onAppClicked, this);
                                    full_app_list_recycler.setAdapter(fullAppAdapter);
                                    progressBar.setVisibility(View.GONE);
                                    ExceptionHelper.showExceptionDialog(this, e);
                                }   //错误处理
                        )
        );
    }

    //应用列表加载成功的回调方法
    private void onAppListLoadSuccessfully(List<AppInfo> fullAppInfoList) {
        fullAppAdapter.setAppInfoList(fullAppInfoList);
        progressBar.setVisibility(View.GONE);

        searchViewModel = new ViewModelProvider(this).get(AppInfoSearchViewModel.class);
        searchViewModel.setFullAppInfoList(fullAppInfoList);
        searchViewModel.init();
        startObserveSearchResult();

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
                    searchProgressBar.setVisibility(View.VISIBLE);
                }
                searchViewModel.onSearchQueryChanged(String.valueOf(s));
            }
        });
    }

    //处理应用选择的方法
    private void onAppClicked(String package_name) {
        Intent result2RuleAddActivity = new Intent();
        result2RuleAddActivity.putExtra(KeyValueStrings.PACKAGE_NAME.getValue(), package_name);
        setResult(Activity.RESULT_OK, result2RuleAddActivity);
        finish();
    }

    private void startObserveSearchResult() {
        searchViewModel.getResultsLiveData().observe(this, result -> {
            searchAdapter.setAppInfoList(result);
            searchProgressBar.setVisibility(View.GONE);
        });
    }
}